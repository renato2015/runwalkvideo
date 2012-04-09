package com.runwalk.video.entities;

public enum RecordingStatus {

	/**
	 * All error constants are denoted by negative numbers (except for fileNotFound)
	 */
	FILE_NOT_ACCESSIBLE(-2, "recordingStatus.fileNotAccessible"),
	
	DSJ_ERROR(-1, "recordingStatus.dsjError"),
	
	COMPRESSED(0, true, "recordingStatus.compressed"),
	
	READY (1, "recordingStatus.ready"),

	NON_EXISTANT_FILE(4, "recordingStatus.fileNotFound"),
	
	UNCOMPRESSED(6, true, "recordingStatus.uncompressed"),

	RECORDED(7,"recordingStatus.recorded"),

	RECORDING(8, "recordingStatus.recording"),

	COMPRESSING(9,"recordingStatus.compressing"),
	
	NONE(10, "recordingStatus.none");
	
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
