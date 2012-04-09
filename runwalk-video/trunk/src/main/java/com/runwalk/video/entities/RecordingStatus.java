package com.runwalk.video.entities;

public enum RecordingStatus {

	/**
	 * All error constants are denoted by negative numbers (except for fileNotFound)
	 */
	FILE_NOT_ACCESSIBLE(-2, "status.fileNotAccessible"),
	
	DSJ_ERROR(-1, "status.dsjError"),
	
	COMPRESSED(0, true, "status.compressed"),
	
	READY (1, "status.ready"),

	NON_EXISTANT_FILE(4, "status.fileNotFound"),
	
	UNCOMPRESSED(6, true, "status.uncompressed"),

	RECORDED(7,"status.recorded"),

	RECORDING(8, "status.recording"),

	COMPRESSING(9,"status.compressing"),
	
	NONE(10, "status.none");
	
	private int code;
	
	private boolean refreshNeeded = false;
	
	private String resourceKey; 

	public String getResourceKey() {
		return resourceKey;
	}

	private RecordingStatus(int code, String resourceKey) {
		this.code = code;
		this.resourceKey = resourceKey;
	}
	
	private RecordingStatus(int code, boolean refreshNeeded, String resourceKey) {
		this(code, resourceKey);
		this.refreshNeeded = refreshNeeded;
	}
	
	public int getCode() {
		return code;
	}
	
	public static RecordingStatus getByCode(int code) {
		for (RecordingStatus statusCode : values()) {
			if (statusCode.getCode() == code) {
				return statusCode;
			}
		}
		return null;
	}
	
	public boolean isErroneous() {
		return getCode() < 0 || getCode() == 4;
	}
	
	public boolean refreshNeeded() {
		return isErroneous() || refreshNeeded;
	}
	
}
