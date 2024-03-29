/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.runwalk.video.util;

import java.io.File;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.log4j.helpers.LogLog;

/** 
 * An extention of the default Log4j FileAppender which
 * will make the directory structure for the set log file. 
 *
 * @version <tt>$Revision: 1.1.28.1 $</tt>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class FileAppender extends org.apache.log4j.FileAppender
{
	public void setFile(final String filename)
	{
		FileAppender.Helper.makePath(filename);
		super.setFile(filename);
	}

	/**
	 * A helper for FileAppenders.
	 */
	public static class Helper
	{
		public static void makePath(final String filename)
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