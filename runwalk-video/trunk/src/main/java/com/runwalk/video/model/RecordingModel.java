package com.runwalk.video.model;

import java.io.File;
import java.util.Date;

import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.io.VideoFileManager;

public class RecordingModel extends AbstractEntityModel<Recording> {
	
	public static final String KEYFRAME_COUNT = "keyframeCount";
	public static final String DURATION = "duration";
	
	private Date creationDate;
	
	private RecordingStatus recordingStatus;
	
	private Long keyframeCount;
	
	public RecordingModel(Recording entity, Date creationDate, Long keyframeCount) {
		super(entity);
		this.creationDate = creationDate;
		this.keyframeCount = keyframeCount;
	}

	// add plain properties for list view..
	public RecordingModel(Recording entity, String firstName, String lastName) {
		super(entity);
		Integer statusCode = entity.getStatusCode();
		if (statusCode != null) {
			recordingStatus = RecordingStatus.getByCode(statusCode);
		}
	}
	
	public String getVideoFileName() {
		return getEntity().getVideoFileName();
	}
	
	/**
	 * Add a recording to the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to add
	 */
	public boolean addKeyFrame(Keyframe keyframe) {
		boolean result = getEntity().getKeyframes().add(keyframe);
		firePropertyChange(KEYFRAME_COUNT, keyframeCount++, keyframeCount);
		return result;
	}	
	
	/**
	 * Remove a {@link Recording} from the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to remove
	 * @return <code>true</code> if the recording was removed
	 */
	public boolean removeKeyframe(Keyframe keyframe) {
		boolean result = getEntity().getKeyframes().remove(keyframe);
		firePropertyChange(KEYFRAME_COUNT, keyframeCount--, keyframeCount);
		return result;
	}

	public Long getKeyframeCount() {
		return keyframeCount;
	}
	
	public Long getDuration() {
		return getEntity().getDuration();
	}
	
	public void setDuration(Long duration) {
		firePropertyChange(DURATION, getEntity().getDuration(), duration);
		getEntity().setDuration(duration);
	}

	public Date getCreationDate() {
		return creationDate;
	}
	
	/**
	 * This method should set the right {@link RecordingStatus} constant for this {@link Recording} 
	 * according to the {@link File} returned by {@link VideoFileManager#getVideoFile(Recording)}.
	 * The status code will not be persisted to the database if it is found to be erroneous, because
	 * the video files are always local to an application's file system.
	 * 
	 * @param recordingStatus The status to be applied
	 * @see VideoFileManager#getVideoFile(Recording)
	 */
	public void setRecordingStatus(RecordingStatus recordingStatus) {
		if (recordingStatus != RecordingStatus.NON_EXISTANT_FILE && this.recordingStatus != null) {
			if (!recordingStatus.isErroneous()) {
				// don't change the statuscode if it is erroneous.
				getEntity().setStatusCode(recordingStatus.getCode());
			}
		} else {
			this.recordingStatus = recordingStatus;
		}
	}
	
	public RecordingStatus getRecordingStatus() {
		return recordingStatus;
	}
	
	public boolean isCompressable() {
		return getRecordingStatus() != RecordingStatus.COMPRESSED && 
		getRecordingStatus() != RecordingStatus.FILE_NOT_ACCESSIBLE &&
		getRecordingStatus() != RecordingStatus.READY && 
//		getRecordingStatus() != RecordingStatus.NON_EXISTANT_FILE &&
		getRecordingStatus() != RecordingStatus.DSJ_ERROR;
	}
	
	public  boolean isUncompressed() {
		return getRecordingStatus() == RecordingStatus.UNCOMPRESSED;
	}

	public boolean isCompressed() {
		return getRecordingStatus() == RecordingStatus.COMPRESSED;
	}

	public boolean isRecorded() {
		return isUncompressed() || isCompressed();
	}

}
