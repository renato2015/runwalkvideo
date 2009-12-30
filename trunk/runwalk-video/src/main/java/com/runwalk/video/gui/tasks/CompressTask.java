package com.runwalk.video.gui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.VideoFile;
import com.runwalk.video.gui.actions.RecordingStatus;
import com.runwalk.video.util.ApplicationUtil;

import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
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

	public CompressTask() {
		super("compress");
		setUserCanCancel(true);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		switch(DSJUtils.getEventType(evt)) {
		case DSMovie.EXPORT_DONE : {
			logger.debug("Export completed.."); 
			finished = true;
			break;
		} case DSMovie.EXPORT_PROGRESS : {
			progress = DSJUtils.getEventValue_int(evt);
			int totalProgress = conversionCounter * part + (progress / conversionCount);
			logger.debug("progress:" + progress + " total progress: " + totalProgress); 
			setProgress(totalProgress);
			break;
		} case DSMovie.EXPORT_STARTED: {
			logger.debug("Export started");
			finished = false;
			break;
		}
		}
	}

	@Override
	protected Boolean doInBackground() {
		message(getResourceString("startMessage"));
		List<Recording> conversionList = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().getUncompressedRecordings();
		conversionCount = conversionList.size();
		part = 100 / conversionCount;
		for (conversionCounter = 0; conversionCounter < conversionCount; conversionCounter++) {
			recording = conversionList.get(conversionCounter);
			RecordingStatus statusCode = recording.getRecordingStatus();
			VideoFile sourceFile = recording.getUncompressedVideoFile();
			VideoFile newFile = recording.getCompressedVideoFile();
			try {
				recording.setRecordingStatus(RecordingStatus.READY);
				graph = new DSMovie(sourceFile.getAbsolutePath(), DSFiltergraph.RENDER_NATIVE, this);
				DSFilterInfo filterInfo = RunwalkVideoApp.getApplication().getPlayerEngine().getTranscoder();
				setProgress(conversionCounter * part);
				message("progressMessage",  conversionCounter + 1, conversionCount);
				graph.export(newFile.getAbsolutePath(), filterInfo, DSFilterInfo.doNotRender());
				recording.setRecordingStatus(RecordingStatus.COMPRESSING);
				while(!finished) {
					Thread.yield();
				}
				recording.setRecordingStatus(RecordingStatus.COMPRESSED);
			} catch(Throwable thr) {
				finished = true;
				recording.setRecordingStatus(RecordingStatus.DSJ_ERROR);
				logger.error(statusCode.getDescription() + ": Compression error for file " + sourceFile.getAbsolutePath(), thr);
				errorCount++;
			} finally {
				ApplicationUtil.disposeDSGraph(graph);
			}
		}
		return (errorCount == 0);
	}

	@Override
	protected void cancelled() {
		super.cancelled();
		if (graph != null) {
			graph.cancelExport();
			ApplicationUtil.disposeDSGraph(graph);
		}
		if (recording != null) {
			if (recording.getCompressedVideoFile().exists()) {
				recording.getCompressedVideoFile().delete();
			}
			recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);
			RunwalkVideoApp.getApplication().setSaveNeeded(true);
		}
	}

	@Override
	protected void finished() {
		try {
			RunwalkVideoApp.getApplication().setSaveNeeded(true);
			message("endMessage"); //$NON-NLS-1$
			String dlogMessage = getResourceString("finishedMessage", conversionCount);
			String dlogTitle = getResourceString("endMessage");
			if (get()) {
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
						dlogMessage, dlogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),dlogMessage + 
						getResourceString("errorMessage", errorCount), dlogTitle, JOptionPane.WARNING_MESSAGE); 					
			}
			RunwalkVideoApp.getApplication().getTableActions().setCleanupEnabled(true);
		} catch (Exception e) {
			errorMessage("endErrorMessage", errorCount); //$NON-NLS-1$
			RunwalkVideoApp.getApplication().getTableActions().setCompressionEnabled(true);
		} finally {
			String syncMsg = getResourceString("lastSyncMessage", ApplicationUtil.formatDate(new Date(), ApplicationUtil.DATE_FORMAT)); 
			RunwalkVideoApp.getApplication().getAnalysisOverviewTable().setStatusMessage(syncMsg);
		}
	}
}
