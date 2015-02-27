package com.runwalk.video.test;

import java.io.File;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.DateVideoFolderRetrievalStrategy;

public class DateVideoFolderRetrievalTestCase extends TestCase {
	
	private final static File VIDEO_FOLDER = new File("C:\\");
	private final static String DATE_FORMAT_STRING = "yyyy/MMMMM/dd";

	public void testDateVideoFolderStructure() throws Exception {
		DateVideoFolderRetrievalStrategy dateVideoFolderRetrievalStrategy = new DateVideoFolderRetrievalStrategy(DATE_FORMAT_STRING);
		// create entities
		Customer testCustomer = new Customer("vermeulen", "joske");
		Analysis testAnalysis = new Analysis(testCustomer, new Date());
		Recording testRecording = new Recording(testAnalysis);
		// retrieve video folder for the created recording
		File videoFolder = dateVideoFolderRetrievalStrategy.getVideoFolder(VIDEO_FOLDER, testRecording);
		// most nested folder should be the folder with the day as name
		videoFolder = testFolderName(videoFolder, testAnalysis, new FieldPosition(DateFormat.Field.DAY_OF_MONTH));
		// second folder should be the folder with the month number as name
		videoFolder = testFolderName(videoFolder, testAnalysis, new FieldPosition(DateFormat.MONTH_FIELD));
		// third folder should be the folder with the year as name
		testFolderName(videoFolder, testAnalysis, new FieldPosition(DateFormat.YEAR_FIELD));
	}
	
	private File testFolderName(File folder, Analysis testAnalysis, FieldPosition fieldPosition) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);
		StringBuffer buffer = new StringBuffer();
		simpleDateFormat.format(testAnalysis.getCreationDate(), buffer, fieldPosition);
		String folderName = buffer.substring(fieldPosition.getBeginIndex(), fieldPosition.getEndIndex());
		assertEquals(folder.getName(), folderName);
		return folder.getParentFile();
	}
	
}
