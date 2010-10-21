package com.runwalk.video.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.swing.ActionMap;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.runwalk.video.gui.AppWindowWrapper;

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
		return tmp/p;
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

	/**
	 * This method will merge all the keys in a new ActionMap
	 * @param map1 The first map 
 	 * @param map2 The second map
	 * @return The map with both the first second map's keys
	 */
	public static ActionMap mergeActionMaps(ActionMap map1, ActionMap map2) {
//		  ActionMap lastActionMap = map1;
//		  while (lastActionMap.getParent() != null) {
//              lastActionMap = lastActionMap.getParent();
//          }
//          lastActionMap.setParent(map2);
		
		ActionMap result = new ActionMap();
		for (Object key : map1.allKeys()) {
			result.put(key, map1.get(key));
		}
		for (Object key : map2.allKeys()) {
			result.put(key, map2.get(key));
		}
		return result;
	}
	
	/**
	 * Find an {@link AppWindowWrapper} in a given {@link Collection} for 
	 * which {@link AppWindowWrapper#getHolder()} equals {@link Component}.
	 * 
	 * @param <T> The concrete type of the {@link AppWindowWrapper}
	 * @param windowWrappers The {@link Collection} to look in 
	 * @param component The current visible {@link Component}
	 * @return The found {@link AppWindowWrapper}
	 * 
	 * @see AppWindowWrapper#getHolder()
	 */
	public static <T extends AppWindowWrapper> T getWindowWrapper(Iterable<T> windowWrappers, final Component component) {
		T result = null;
		Iterator<T> iterator = windowWrappers.iterator();
		while(iterator.hasNext() && result == null) {
			T next = iterator.next();
			if (next.getHolder() == component) {
				result = next;
			}
		}
		return result;
	}


}
