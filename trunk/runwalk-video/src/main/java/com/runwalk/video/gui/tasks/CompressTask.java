package com.runwalk.video.gui.tasks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import com.runwalk.video.VideoFileManager;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
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
	private final List<Recording> recordings;
	private final DSFilterInfo transcoder;
	private final VideoFileManager videoFileManager;
	/** volatile flag so changes can be seen directly by all threads */
	private volatile boolean finished = false;

	public CompressTask(VideoFileManager videoFileManager, List<Recording> recordings, String transcoder) {
		super("compress");
		this.transcoder = DSFilterInfo.filterInfoForName(transcoder);
		this.recordings = recordings;
		this.videoFileManager = videoFileManager;
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
//			getLogger().debug("progress:" + progress + " total progress: " + totalProgress); 
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
			File sourceFile = getVideoFileManager().getUncompressedVideoFile(recording);
			File destinationFile = getVideoFileManager().getCompressedVideoFile(recording);
			try {
				// create parent folder if it doesn't exist yet
				File parentFolder = destinationFile.getParentFile();
				if (!parentFolder.exists()) {
					FileUtils.forceMkdir(parentFolder);
				}
				recording.setRecordingStatus(RecordingStatus.READY);
				if (exporter == null) {
					exporter = new DSJPlayer(sourceFile, /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/ DSFiltergraph.RENDER_NATIVE, this);
				} else {
					exporter.loadFile(sourceFile, /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/ DSFiltergraph.RENDER_NATIVE, this);
				}
				setProgress((int) (conversionCounter * part));
				message("progressMessage",  conversionCounter + 1, conversionCount);
				int result = exporter.getFiltergraph().export(destinationFile.getAbsolutePath(), transcoder, DSFilterInfo.doNotRender());
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
				getLogger().error(statusCode + ": Compression error for file " + sourceFile.getAbsolutePath(), thr);
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
			//TODO clean up file that failed to convert..
			recording.setRecordingStatus(RecordingStatus.UNCOMPRESSED);
		}
	}

	@Override
	protected void finished() {
		try {
			String dlogMessage = getResourceString("finishedMessage", conversionCount);
			String dlogTitle = getResourceString("endMessage");
			if (get()) {
				JOptionPane.showMessageDialog(null, dlogMessage, dlogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, dlogMessage + 
						getResourceString("errorMessage", errorCount), dlogTitle, JOptionPane.WARNING_MESSAGE); 					
			}
		} catch (Exception e) {
			errorMessage("endErrorMessage", errorCount);
		} finally {
			String formattedDate = AppUtil.formatDate(new Date(), AppUtil.DATE_FORMATTER);
			String syncMsg = getResourceString("lastSyncMessage", formattedDate); 
			setMessage(syncMsg);
		}
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}
	

}
