package com.runwalk.video;

import java.io.File;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.util.AppSettings;

/**
 * This strategy can be implemented to structure the storage of video files within a parent folder.
 * Creation of the actual folder is managed at recording or compressing time, so this should not be handled here.
 * 
 * @author Jeroen Peelaerts
 *
 */
@XmlJavaTypeAdapter(MarshallableVideoFolderRetrievalStrategy.Adapter.class)
public interface VideoFolderRetrievalStrategy {

	/**
	 * Returns the video folder for a given {@link Recording} and video folder. 
	 * The video folder {@link AppSettings#getVideoDir()} or 
	 * {@link AppSettings#getUncompressedVideoDir()} when the recording is 
	 * in the {@link RecordingStatus#COMPRESSED} or 
	 * {@link RecordingStatus#UNCOMPRESSED} state respectively.
	 * 
	 * @param videoDir The parent video folder
	 * @param recording The recording to get the folder for
	 * @return The folder
	 */
	public File getVideoFolder(File videoDir, Recording recording);
	
	/**
	 * Return a display string to show the user how this strategy exactly structures the folder tree.
	 * The '/' character is used as a folder separator. For example getting '/' as a result from this method
	 * would mean that the video files are all stored in the same folder.
	 * 
	 * @return A human understandable string
	 */
	public String getDisplayString();
	
}
