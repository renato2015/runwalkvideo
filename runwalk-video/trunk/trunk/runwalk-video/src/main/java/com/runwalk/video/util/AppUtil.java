package com.runwalk.video.util;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class AppUtil {
	//duration formats
	public static final SimpleDateFormat DURATION_FORMATTER = new SimpleDateFormat("mm:ss");
	public static final SimpleDateFormat EXTENDED_DURATION_FORMATTER = new SimpleDateFormat("mm:ss.SSS");
	//date formats
	public static final SimpleDateFormat FILENAME_DATE_FORMATTER = new SimpleDateFormat("dd-MM-yy");
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
	public static final SimpleDateFormat EXTENDED_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	private AppUtil() { }
	
	/**
	 * This method checks for existence of the given directory and will create it when it doesn't exist. 
	 * If creation of the first directory fails, then the method will return the second specified directory, 
	 * which will also be created if necessary.
	 * 
	 * @param directory The first directory 
	 * @param defaultDir The second directory to return when creation of the first one fails
	 * @return The resulting directory
	 */
	public static File createDirectories(File directory, File defaultDir) {
		if (directory != null && !directory.exists()) {
			try {
				FileUtils.forceMkdir(directory);
			} catch(IOException exception) {
				Logger.getLogger(AppUtil.class).error("Directory " + directory.getAbsolutePath() + " couldn't be created.", exception);
				return createDirectories(defaultDir, null);
			}
		} else if (directory == null && defaultDir != null) {
			return createDirectories(defaultDir, null);
		}
		return directory;
	}

	public static float round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return tmp/p;
	}

	public static String formatDate(Date date, SimpleDateFormat formatter) {
		StringBuffer result = new StringBuffer("");
		if (date != null) {
			synchronized(formatter) {
				formatter.format(date, result, new FieldPosition(DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD));
			}
		}
		return result.toString();
	}

}
