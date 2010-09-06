package com.runwalk.video;

import java.io.File;

import com.runwalk.video.entities.Recording;

/**
 * This strategy can be implemented to structure the storage of video files within a parent folder.
 * Creation of the actual folder is managed at recording or compressing time, so this should not be handled here.
 * 
 * @author Jeroen Peelaerts
 *
 */
public interface VideoFolderRetrievalStrategy {

	public File getVideoFolder(File parentFolder, Recording recording);
}
