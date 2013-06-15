package com.runwalk.video.io;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.runwalk.video.entities.Recording;

/**
 * Retrieves a video file's parent folder by parsing the analysis' creation date using a given date format string.
 * 
 * @author Jeroen Peelaerts
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DateVideoFolderRetrievalStrategy extends AbstractVideoFolderRetrievalStrategy {

	private final static String FOLDER_SEPARATOR = "/";
	
	private String dateFormatString;
	
	protected DateVideoFolderRetrievalStrategy() {	}
	
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

	public String getDisplayString() {
		return getDateFormatString();
	}
	
}
