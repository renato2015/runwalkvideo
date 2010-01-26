package com.runwalk.video.entities;

import de.humatic.dsj.DSJException;

public interface VideoFile {

	public abstract int getDuration() throws DSJException;

	public abstract boolean canReadAndExists();

	public abstract boolean canRead();

	public abstract String getAbsolutePath();

	public abstract boolean exists();

	public abstract boolean delete();

	public abstract String getName();

}