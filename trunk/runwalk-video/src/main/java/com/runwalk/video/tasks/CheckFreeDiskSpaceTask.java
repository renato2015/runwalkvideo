package com.runwalk.video.tasks;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileSystemUtils;
import org.jdesktop.application.Task;

import com.runwalk.video.io.VideoFileManager;

/**
 * This {@link Task} will check the available free disk space on the {@link VideoFileManager}'s current
 * uncompressed video folder path. 
 *
 * @author Jeroen Peelaerts
 */
public class CheckFreeDiskSpaceTask extends AbstractTask<Long, Void> {

	public static final Long MIN_FREE_SPACE = 1024L;
	
	private final VideoFileManager videoFileManager;

	private final Component parentComponent;

	public CheckFreeDiskSpaceTask(Component parentComponent, VideoFileManager videoFileManager) {
		super("checkFreeDiskSpace");
		this.parentComponent = parentComponent;
		this.videoFileManager = videoFileManager;
	}

	@Override
	protected Long doInBackground() throws Exception {
		message("startMessage");
		String recordingPath = getVideoFileManager().getUncompressedVideoDir().getAbsolutePath();
		return FileSystemUtils.freeSpaceKb(recordingPath) / 1024;
	}

	@Override
	protected void succeeded(Long result) {
		if (result < MIN_FREE_SPACE) {
			JOptionPane.showMessageDialog(getParentComponent(), getResourceString("warningMessage", result),
					getResourceString("title"), JOptionPane.WARNING_MESSAGE);
		}
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public Component getParentComponent() {
		return parentComponent;
	}
	

}
