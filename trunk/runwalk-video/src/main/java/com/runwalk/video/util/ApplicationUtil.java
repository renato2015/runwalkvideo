package com.runwalk.video.util;

import java.awt.Frame;
import java.io.File;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Action;
import javax.swing.ActionMap;

import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;

import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJUtils;

public class ApplicationUtil {
	//duration formats
	public static final SimpleDateFormat DURATION_FORMATTER = new SimpleDateFormat("mm:ss");
	public static final SimpleDateFormat EXTENDED_DURATION_FORMATTER = new SimpleDateFormat("mm:ss.SSS");
	//date formats
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yy");
	public static final SimpleDateFormat EXTENDED_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	public static final SimpleDateFormat FILENAME_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss");

	private ApplicationUtil() {

	}

	public static File getCompressedVideoFile(String fileName) {
		File file = new File(ApplicationSettings.getInstance().getVideoDir(), fileName);
		return file.exists() ? file : null;
	}

	//methods for parsing the video directory
	static File parseDir(String path) {
		File newDir = null;
		if (path != null) {
			newDir = new File(path);
		}
		return parseDir(newDir) ? newDir : null;
	}

	public static boolean parseDir(File dir) {
		boolean parsed = false;
		if (dir != null) {
			parsed = dir.canRead() && dir.exists() && dir.isDirectory() && dir.canWrite();
		}
		return parsed;
	}
	
	public static String[] splitString(String str, String delimiter) {
		String[] result = null;
		int indexOf = str.lastIndexOf(delimiter);
		if (indexOf != -1) {
			result = new String[] {str.substring(0, indexOf), str.substring(indexOf+1)};
		} else {
			result = new String[] {str, null};
		}
		return result;
	}
	
	public static Date granularity(Date date, int precision) {
		Date result = null;
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(precision, 0);
			result = cal.getTime();
		}
		return result;
	}

	public static float round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}

	public static String formatDate(Date date, SimpleDateFormat formatter) {
		StringBuffer result = new StringBuffer("");
		if (date != null) {
			formatter.format(date, result, new FieldPosition(DateFormat.DAY_OF_WEEK_IN_MONTH_FIELD));
		}
		return result.toString();
	}
	
	public static String firstLettersToUpperCase(String s) {
		return (s.length()>0)? Character.toUpperCase(s.charAt(0))+s.substring(1) : s;
	}

	public static int getMovieDuration(String path) {
		int duration = 0;
		if (new File(path).exists()) {
			duration = DSJUtils.getBasicFileStats(path)[0];
		}
		return duration;
	}
	
	public static String getFormattedMovieDuration(long duration) {
		return formatDate(new Date(duration), DURATION_FORMATTER);
	}
	
	public static ActionMap getActionMap(Object obj ) {
		return RunwalkVideoApp.getApplication().getActionMap(obj);
	}
	
	public static Action getAction(Object obj, String key) {
		return getActionMap(obj).get(key);
	}
	
	public static ResourceMap getResourceMap(Class<?> theClass) {
		return RunwalkVideoApp.getApplication().getContext().getResourceMap(theClass);
	}

	public static String getString(Class<?> theClass, String key) {
		return getResourceMap(theClass).getString(key);
	}
	
	public static String getString(Object obj, String key) {
		return getResourceMap(obj.getClass()).getString(key);
	}

	public static void disposeDSGraph(DSFiltergraph graph) {
		if (graph != null) {
			Frame fullscreenFrame = graph.getFullScreenWindow();
			if (fullscreenFrame != null) {
				RunwalkVideoApp.getApplication().getMenuBar().removeWindow(fullscreenFrame);
				fullscreenFrame.dispose();
			}
			graph.dispose();
		}
	}
}
