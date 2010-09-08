package com.runwalk.video;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.runwalk.video.entities.Recording;

/**
 * Retrieves a video file's parent folder by parsing the analysis' creation date using a given format string.
 * 
 * @author Jeroen Peelaerts
 */
public class DateVideoFolderRetrievalStrategy implements VideoFolderRetrievalStrategy {

	private final static String FOLDER_SEPARATOR = "/";
	
	private final String dateFormatString;
	
	public DateVideoFolderRetrievalStrategy(String dateFormatString) {
		this.dateFormatString = dateFormatString;
	}
	
	public File getVideoFolder(File parentFolder, Recording recording) {
		File result = parentFolder;
		Date recordingDate = recording.getAnalysis().getCreationDate();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getDateFormatString());
		String parsedDate = simpleDateFormat.format(recordingDate);
		String[] splittedDate = parsedDate.split(FOLDER_SEPARATOR);
		for(String folderName : splittedDate) {
			result = new File(result, folderName);
		}
		return result;
	}

	public String getDateFormatString() {
		return dateFormatString;
	}
	
}
