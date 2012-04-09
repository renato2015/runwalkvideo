package com.runwalk.video.entities;

public enum CalendarSlotStatus {

	NEW("calendarSlotStatus.new"),
	
	MODIFIED("calendarSlotStatus.modified"),
	
	SYNCHRONIZED("calendarSlotStatus.synchronized");
	
	private final String resourceKey;
	
	private CalendarSlotStatus(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getResourceKey() {
		return resourceKey;
	}
	
}
