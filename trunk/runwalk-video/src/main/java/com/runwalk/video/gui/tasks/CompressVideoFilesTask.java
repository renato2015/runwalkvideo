package com.runwalk.video.gui.tasks;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.dsj.DSJPlayer;

import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMovie;

public class CompressVideoFilesTask extends AbstractTask<Boolean, Void> implements PropertyChangeListener { 
	private int errorCount = 0;
	private int conversionCounter, conversionCount;
	private double part;
	private DSJPlayer exporter;
	private Recording recording;
	private final List<Recording> recordings;
	private final DSFilterInfo transcoder;
	private final VideoFileManager videoFileManager;
	/** volatile so changes to this field can be seen directly by all threads */
	private volatile boolean finished = false;
	private final Component parentComponent;

	public CompressVideoFilesTask(Component parentComponent, VideoFileManager videoFileManager, 
			List<Recording> recordings, String transcoder) {
		super("compressVideoFiles");
		this.parentComponent = parentComponent;
		this.videoFileManager = videoFileManager;
		this.recordings = recordings;
		this.transcoder = DSFilterInfo.filterInfoForName(transcoder);
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
		message("startMessage");
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
				if (exporter == null) {
					exporter = new DSJPlayer(sourceFile.getAbsolutePath(), /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/ DSFiltergraph.RENDER_NATIVE, this);
				} else {
					exporter.loadVideo(sourceFile.getAbsolutePath(), /*DSFiltergraph.HEADLESS | DSFiltergraph.NO_AMW*/ DSFiltergraph.RENDER_NATIVE, this);
				}
				setProgress((int) (conversionCounter * part));
				message("progressMessage", conversionCounter + 1, conversionCount);
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
				getLogger().error("Compression error for file " + sourceFile.getAbsolutePath(), thr);
				errorCount++;
			} finally {
				if (exporter != null && exporter.getFiltergraph().getActive()) {
					exporter.getFiltergraph().stop();
				}
				recording.setRecordingStatus(statusCode);
				// video file needs to be refreshed in the cache
				getVideoFileManager().refreshCache(recording);
			}
		}
		if (exporter != null) {
			exporter.dispose();
		}
		message("endMessage", getExecutionDuration(TimeUnit.SECONDS));
		return (errorCount == 0);
	}

	@Override
	protected void cancelled() {
		if (exporter != null) {
			exporter.pause();
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
			String dlogTitle = getResourceString("endMessage", getExecutionDuration(TimeUnit.SECONDS));
			if (get()) {
				JOptionPane.showMessageDialog(getParentComponent(), dlogMessage, 
						dlogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(getParentComponent(), dlogMessage + 
						getResourceString("errorMessage", errorCount), dlogTitle, JOptionPane.WARNING_MESSAGE); 					
			}
		} catch (Exception e) {
			errorMessage("endErrorMessage", errorCount);
		}
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public Component getParentComponent() {
		return parentComponent;
	}

}
