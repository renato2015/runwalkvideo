package com.runwalk.video.util;

import java.io.File;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.swing.ActionMap;

import org.apache.log4j.Logger;
import org.jdesktop.application.ResourceMap;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.SerializableEntity;

import de.humatic.dsj.DSJUtils;

public class AppUtil {
	//duration formats
	public static final SimpleDateFormat DURATION_FORMATTER = new SimpleDateFormat("mm:ss");
	public static final SimpleDateFormat EXTENDED_DURATION_FORMATTER = new SimpleDateFormat("mm:ss.SSS");
	//date formats
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MM-yy");
	public static final SimpleDateFormat EXTENDED_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	public static final SimpleDateFormat FILENAME_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss");

	private AppUtil() { }

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
	
	/**
	 * This method will merge all the keys in a new ActionMap
	 * @param map1 The first map 
 	 * @param map2 The second map
	 * @return The map with both the first second map's keys
	 */
	public static ActionMap mergeActionMaps(ActionMap map1, ActionMap map2) {
		ActionMap result = new ActionMap();
		for (Object key : map1.allKeys()) {
			result.put(key, map1.get(key));
		}
		for (Object key : map2.allKeys()) {
			result.put(key, map2.get(key));
		}
		return result;
	}

	public static <T> void persistEntity(SerializableEntity<T> item) {
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			em.persist(item);
			tx.commit();
			Logger.getLogger(AppUtil.class).debug(item.getClass().getSimpleName() + " with ID " + item.getId() + " was persisted.");
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error("Exception thrown while persisting entity." , e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
	}

	public static <T> void deleteEntity(SerializableEntity<T> detachedItem) {
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			SerializableEntity<T> mergedItem = em.merge(detachedItem);
			em.remove(mergedItem);
			tx.commit();
			Logger.getLogger(AppUtil.class).debug(detachedItem.getClass().getSimpleName() + " with ID " + detachedItem.getId() + " removed from persistence.");
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error("Exception thrown while deleting entity.", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
	}
}
