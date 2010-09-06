package com.runwalk.video;

import java.io.File;
import java.text.AttributedCharacterIterator;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

import com.google.common.collect.Maps;
import com.runwalk.video.entities.Recording;

/**
 * Retrieves a video file's parent folder by parsing the analysis' creation date using a given format string.
 * 
 * @author Jeroen Peelaerts
 */
public class DateVideoFolderRetrievalStrategy implements VideoFolderRetrievalStrategy {

	private final String dateFormatString ;
	
	public DateVideoFolderRetrievalStrategy(String dateFormatString) {
		this.dateFormatString = dateFormatString;
	}
	
	public File getVideoFolder(File parentFolder, Recording recording) {
		File result = parentFolder;
		Date recordingDate = recording.getAnalysis().getCreationDate();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(this.dateFormatString);
		// what happens here?
		AttributedCharacterIterator formatToCharacterIterator = simpleDateFormat.formatToCharacterIterator(recordingDate);
		// create a tree map with a comparator that sorts according to the fieldposition's begin index in the format string
		TreeMap<FieldPosition, String> map = Maps.newTreeMap(new Comparator<FieldPosition>() {

			public int compare(FieldPosition o1, FieldPosition o2) {
				return o1.getBeginIndex() < o2.getBeginIndex() ? -1 : 1;
			}
			
		});
		for( Attribute attribute : formatToCharacterIterator.getAllAttributeKeys()) {
			if (attribute instanceof DateFormat.Field) {
				DateFormat.Field dateField = (DateFormat.Field) attribute;
				FieldPosition position = new FieldPosition(dateField);
				StringBuffer buffer = new StringBuffer();
				simpleDateFormat.format(recordingDate, buffer, position);
				String substring = buffer.substring(position.getBeginIndex(), position.getEndIndex());
				map.put(position, substring);
			}
		}
		// create folders using the imposed fieldposition order in the treemap
		for (String folderName : map.values()) {
			result = new File(result, folderName);
		}
		return result;
	}

}
