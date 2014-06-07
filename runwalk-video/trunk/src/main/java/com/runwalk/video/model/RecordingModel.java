package com.runwalk.video.model;

import java.util.Date;

import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;

public class RecordingModel extends AbstractEntityModel<Recording> {
	
	public static final String KEYFRAME_COUNT = "keyframeCount";
	public static final String DURATION = "duration";
	
	private Date creationDate;
	
	private Long keyframeCount;
	
	public RecordingModel(Recording entity, Date creationDate, Long keyframeCount) {
		super(entity);
		this.creationDate = creationDate;
		this.keyframeCount = keyframeCount;
	}

	// add plain properties for list view..
	public RecordingModel(Recording entity, String firstName, String lastName) {
		super(entity);
	}
	
	public RecordingModel(Recording entity) {
		super(entity);
		this.creationDate = entity.getAnalysis().getCreationDate();
		this.keyframeCount = Integer.valueOf(entity.getKeyframes().size()).longValue();
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
	
}
