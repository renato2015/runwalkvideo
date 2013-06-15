package com.runwalk.video.io;

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.entities.Recording;

/**
 * This is the default video retrieval strategy, all the video files are stored in the root of the chosen folder.
 * 
 * @author Jeroen Peelaerts
 */
@XmlRootElement
public class DefaultVideoFolderRetrievalStrategy extends AbstractVideoFolderRetrievalStrategy {

	public File getVideoFolder(File parentFolder, Recording recording) {
		return parentFolder;
	}

	public String getDisplayString() {
		return "/";
	}

}
