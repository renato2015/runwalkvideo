package com.runwalk.video;

import java.io.File;

import com.runwalk.video.entities.Recording;

/**
 * This is the default video retrieval strategy, all the video files are stored in the root of the chosen folder.
 * 
 * @author Jeroen Peelaerts
 */
public class DefaultVideoFolderRetrievalStrategy implements VideoFolderRetrievalStrategy {

	public File getVideoFolder(File parentFolder, Recording recording) {
		return parentFolder;
	}

}
