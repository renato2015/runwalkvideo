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
import com.runwalk.video.gui.media.DSJPlayer;
import com.runwalk.video.util.AppUtil;

import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMovie;

public class CompressTask extends AbstractTask<Boolean, Void> implements PropertyChangeListener { 
	private int errorCount = 0;
	private int conversionCounter, conversionCount;
	private double part;
	private DSJPlayer exporter;
	private Recording recording;
	private List<Recording> recordings;
	private DSFilterInfo transcoder;
	private volatile boolean finished = false;

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
			synchronized(recording) {
				finished = true;
				recording.notifyAll();
			}
			break;
		} case DSMovie.EXPORT_PROGRESS : {
			int progress = DSJUtils.getEventValue_int(evt);
			double totalProgress = conversionCounter * part + ((double) progress / (double) conversionCount);
			getLogger().debug("progress:" + progress + " total progress: " + totalProgress); 
			setProgress((int) totalProgress);
			break;
		} case DSMovie.EXPORT_STARTED: {
			getLogger().debug("Export started");
			break;
		}
		}
	}

	@Override
	protected Boolean doInBackground() {
		message(getResourceString("startMessage"));
		conversionCount = recordings.size();
		part = 100 / (double) conversionCount;
		for (conversionCounter = 0; conversionCounter < conversionCount; conversionCounter++) {
			recording = recordings.get(conversionCounter);
			RecordingStatus statusCode = recording.getRecordingStatus();
			VideoFile sourceFile = recording.getUncompressedVideoFile();
			VideoFile newFile = recording.getCompressedVideoFile();
			try {
				recording.setRecordingStatus(RecordingStatus.READY);
				if (exporter == null) {
					exporter = new DSJPlayer(sourceFile, /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/ DSFiltergraph.RENDER_NATIVE, this);
				} else {
					exporter.loadFile(sourceFile, /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/ DSFiltergraph.RENDER_NATIVE, this);
				}
				setProgress((int) (conversionCounter * part));
				message("progressMessage",  conversionCounter + 1, conversionCount);
				int result = exporter.getFiltergraph().export(newFile.getAbsolutePath(), transcoder, DSFilterInfo.doNotRender());
				if (result < 0) {
					//reconnect failed.. exception will be thrown here in future versions..
					getLogger().error("graph reconnect failed!");
					errorCount++;
				} else {
					recording.setRecordingStatus(RecordingStatus.COMPRESSING);
					synchronized(recording) {
						while (!finished) {
							recording.wait(2000);
						}
						finished = false;
					}
				}
				statusCode = RecordingStatus.COMPRESSED;
			} catch(Throwable thr) {
				statusCode = RecordingStatus.DSJ_ERROR;
				getLogger().error(statusCode.getDescription() + ": Compression error for file " + sourceFile.getAbsolutePath(), thr);
				errorCount++;
			} finally {
				if (exporter != null) {
					exporter.getFiltergraph().stop();
				}
				recording.setRecordingStatus(statusCode);
			}
		}
		if (exporter != null) {
			exporter.dispose();
		}
		message(getResourceString("endMessage"));
		return (errorCount == 0);
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		if (exporter != null) {
			exporter.getFiltergraph().cancelExport();
			exporter.dispose();
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
