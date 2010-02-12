package com.runwalk.video.gui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.entities.VideoFile;
import com.runwalk.video.util.AppUtil;

import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMovie;

public class CompressTask extends AbstractTask<Boolean, Void> implements PropertyChangeListener { 
	private int errorCount = 0;
	private int conversionCounter, conversionCount;
	private int part;
	private int progress = 0;
	private boolean finished = false;
	private DSMovie graph;
	private Recording recording;
	private List<Recording> recordings;
	private DSFilterInfo transcoder;

	public CompressTask(List<Recording> recordings, DSFilterInfo transcoder) {
		super("compress");
		this.transcoder = transcoder;
		this.recordings = recordings;
		setUserCanCancel(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		switch(DSJUtils.getEventType(evt)) {
		case DSMovie.EXPORT_DONE : {
			getLogger().debug("Export completed.."); 
			finished = true;
			break;
		} case DSMovie.EXPORT_PROGRESS : {
			progress = DSJUtils.getEventValue_int(evt);
			int totalProgress = conversionCounter * part + (progress / conversionCount);
			getLogger().debug("progress:" + progress + " total progress: " + totalProgress); 
			setProgress(totalProgress);
			break;
		} case DSMovie.EXPORT_STARTED: {
			getLogger().debug("Export started");
			finished = false;
			break;
		}
		}
	}
	
	@Override
	protected Boolean doInBackground() {
		message(getResourceString("startMessage"));
		conversionCount = recordings.size();
		part = 100 / conversionCount;
		for (conversionCounter = 0; conversionCounter < conversionCount; conversionCounter++) {
			recording = recordings.get(conversionCounter);
			RecordingStatus statusCode = recording.getRecordingStatus();
			VideoFile sourceFile = recording.getUncompressedVideoFile();
			VideoFile newFile = recording.getCompressedVideoFile();
			try {
				recording.setRecordingStatus(RecordingStatus.READY);
				int loadResult = 0;
				if (graph == null) {
					graph = new DSMovie(sourceFile.getAbsolutePath(), DSFiltergraph.JAVA_POLL /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/, this);
				} else {
					try {
						loadResult = graph.loadFile(sourceFile.getAbsolutePath(), 0);
					} catch(DSJException exc) {
						getLogger().debug("Rebuilding graph for file " + sourceFile.getName());
						AppUtil.disposeDSGraph(graph);
						graph = new DSMovie(sourceFile.getAbsolutePath(), DSFiltergraph.JAVA_POLL /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/, this);
					}
				}
				setProgress(conversionCounter * part);
				message("progressMessage",  conversionCounter + 1, conversionCount);
				int result = graph.export(newFile.getAbsolutePath(), transcoder, DSFilterInfo.doNotRender());
				if (result < 0 || loadResult < 0) {
					//reconnect failed.. exception will be thrown here in future versions..
					getLogger().error("graph reconnect failed!");
					errorCount++;
					finished = true;
				}
				recording.setRecordingStatus(RecordingStatus.COMPRESSING);
				while(!finished) {
					Thread.yield();
				}
				statusCode = RecordingStatus.COMPRESSED;
			} catch(Throwable thr) {
				finished = true;
				statusCode = RecordingStatus.DSJ_ERROR;
				getLogger().error(statusCode.getDescription() + ": Compression error for file " + sourceFile.getAbsolutePath(), thr);
				errorCount++;
			} finally {
				graph.stop();
				recording.setRecordingStatus(statusCode);
			}
		}
		if (graph != null) {
			AppUtil.disposeDSGraph(graph);
		}
		message(getResourceString("endMessage"));
		return (errorCount == 0);
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		if (graph != null) {
			graph.cancelExport();
			AppUtil.disposeDSGraph(graph);
		}
		if (recording != null) {
			if (recording.getCompressedVideoFile().exists()) {
				recording.getCompressedVideoFile().delete();
			}
			recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);
		}
	}

	@Override
	protected void finished() {
		try {
			String dlogMessage = getResourceString("finishedMessage", conversionCount);
			String dlogTitle = getResourceString("endMessage");
			if (get()) {
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
						dlogMessage, dlogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),dlogMessage + 
						getResourceString("errorMessage", errorCount), dlogTitle, JOptionPane.WARNING_MESSAGE); 					
			}
		} catch (Exception e) {
			errorMessage("endErrorMessage", errorCount);
		} finally {
			String syncMsg = getResourceString("lastSyncMessage", AppUtil.formatDate(new Date(), AppUtil.DATE_FORMATTER)); 
			RunwalkVideoApp.getApplication().getStatusPanel().showMessage(syncMsg);
		}
	}
	
	
}
