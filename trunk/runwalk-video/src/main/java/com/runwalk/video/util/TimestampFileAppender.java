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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Viktor Bresan
 *
 */
public class TimestampFileAppender extends FileAppender {

	private static final String TARGET = "\\{timestamp\\}";

	protected String timestampPattern = null;

	/**
	 * 
	 */
	public void setFile(String file) {
		if (timestampPattern != null) {
			file = file.replaceAll(TARGET, new SimpleDateFormat(timestampPattern).format(Calendar.getInstance().getTime()));
			super.setFile(file);
		} else {
			super.setFile(file);
		}
	}

	/**
	 * 
	 * @param fileName
	 * @param append
	 */
	public void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
		if (timestampPattern != null) {
			fileName = fileName.replaceAll(TARGET, new SimpleDateFormat(timestampPattern).format(Calendar.getInstance().getTime()));
			super.setFile(fileName, append, bufferedIO, bufferSize);		
		} else {
			super.setFile(fileName, append, bufferedIO, bufferSize);
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getTimestampPattern() {
		return timestampPattern;
	}

	/**
	 * 
	 * @param timestampPattern
	 */
	public void setTimestampPattern(String timestampPattern) {
		this.timestampPattern = timestampPattern;
	}

}