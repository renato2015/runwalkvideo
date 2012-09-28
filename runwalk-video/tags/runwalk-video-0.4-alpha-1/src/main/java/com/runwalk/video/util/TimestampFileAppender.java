package com.runwalk.video.util;

/* 
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;

import org.apache.log4j.FileAppender;
import org.apache.log4j.helpers.LogLog;

/**
 * @author Viktor Bresan
 * @author Jeroen Peelaerts
 *
 */
public class TimestampFileAppender extends FileAppender {

	private static final String TARGET = "\\{timestamp\\}";

	protected String timestampPattern = null;

	/**
	 * {@inheritDoc}
	 */
	public void setFile(String fileName) {
		fileName = fileName.replaceAll("(?<!^)(\\\\|/){1,}", Matcher.quoteReplacement(File.separator));
		TimestampFileAppender.Helper.makePath(fileName);
		if (timestampPattern != null) {
			fileName = fileName.replaceAll(TARGET, new SimpleDateFormat(timestampPattern).format(Calendar.getInstance().getTime()));
			super.setFile(fileName); 
		} else {
			super.setFile(fileName);
		}
	}

	/**
	 * {@inheritDoc} 
	 */
	public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
		fileName = fileName.replaceAll("(?<!^)(\\\\|/){1,}", Matcher.quoteReplacement(File.separator));
		TimestampFileAppender.Helper.makePath(fileName);
		if (timestampPattern != null) {
			fileName = fileName.replaceAll(TARGET, new SimpleDateFormat(timestampPattern).format(Calendar.getInstance().getTime()));
			super.setFile(fileName, append, bufferedIO, bufferSize);		
		} else {
			super.setFile(fileName, append, bufferedIO, bufferSize);
		}
	}

	public String getTimestampPattern() {
		return timestampPattern;
	}

	public void setTimestampPattern(String timestampPattern) {
		this.timestampPattern = timestampPattern;
	}
	
	/**
	 * A helper for FileAppenders.
	 */
	public static class Helper
	{
		public static void makePath(String filename)
		{
			File dir;
			try {
				URL url = new URL(filename.trim());
				dir = new File(url.getFile()).getParentFile();
			}
			catch (MalformedURLException e) {
				dir = new File(filename.trim()).getParentFile();
			}

			if (!dir.exists()) {
				boolean success = dir.mkdirs();
				if (!success) {
					LogLog.error("Failed to create directory structure: " + dir);
				}
			}
		}
	}

}