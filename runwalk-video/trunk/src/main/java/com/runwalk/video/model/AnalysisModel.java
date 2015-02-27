package com.runwalk.video.model;

import java.util.Date;
import java.util.List;

import com.google.common.collect.Iterables;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Analysis.Progression;
import com.runwalk.video.entities.Item;
import com.runwalk.video.entities.Recording;

public class AnalysisModel extends AbstractEntityModel<Analysis> {
	
	public final static String RECORDING_COUNT = "recordingCount";
	
	public final static String ARTICLE = "article";

	public static final String CREATION_DATE = "creationDate";

	public static final String START_DATE = "startDate";
	
	public static final String PROGRESSION = "progression";
	
	public static final String COMMENTS = "comments";

	private CustomerModel customerModel;
	
	public AnalysisModel(CustomerModel customerModel, Analysis entity) {
		super(entity);
		this.customerModel = customerModel;
	}
	
	/**
	 * Add a recording to the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to add
	 */
	public boolean addRecording(Recording recording) {
		int oldSize = getRecordingCount();
		boolean result = getEntity().addRecording(recording);
		firePropertyChange(RECORDING_COUNT, oldSize, getRecordingCount());
		return result;
	}	
	
	/**
	 * Remove a {@link Recording} from the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to remove
	 * @return <code>true</code> if the recording was removed
	 */
	public boolean removeRecording(Recording recording) {
		int oldSize = getRecordingCount();
		boolean result = getEntity().removeRecording(recording);
		firePropertyChange(RECORDING_COUNT, oldSize, getRecordingCount());
		return result;
	}
	
	public int getRecordingCount() {
		return getEntity().getRecordings().size();
	}
	
	public boolean isRecordingsEmpty() {
		return getEntity().getRecordings() != null && !getEntity().getRecordings().isEmpty();
	}
	
	public List<Recording> getRecordings() {
		return getEntity().getRecordings();
	}

	public Date getCreationDate() {
		return getEntity().getCreationDate();
	}
	
	public Date getFeedbackDate() {
		return getEntity().getFeedbackDate();
	}
	
	public Date getStartDate() {
		return getEntity().getStartDate();
	}

	public String getComments() {
		return getEntity().getComments();
	}

	public Item getItem() {
		return getEntity().getItem();
	}

	public Long getFeedbackId() {
		return getEntity().getFeedbackId();
	}
	
	public boolean isFeedbackRecord() {
		return getFeedbackId() != null;
	}

	public Progression getProgression() {
		return getEntity().getProgression();
	}

	public String getTokenId() {
		return getEntity().getTokenId();
	}

	public void setItem(Item article) {
		firePropertyChange(ARTICLE, getEntity().getItem(), article);
		getEntity().setItem(article);
	}

	public void setCreationDate(Date creationDate) {
		firePropertyChange(CREATION_DATE, getEntity().getCreationDate(), creationDate);
		getEntity().setCreationDate(creationDate);
	}
	
	public void setStartDate(Date startDate) {
		firePropertyChange(START_DATE, getEntity().getStartDate(), startDate);
		getEntity().setStartDate(startDate);
	}

	public void setProgression(Progression progression) {
		firePropertyChange(PROGRESSION, getEntity().getProgression(), progression);
		getEntity().setProgression(progression);
	}
	
	public void setComments(String comments) {
		firePropertyChange(COMMENTS, getEntity().getComments(), comments);
		getEntity().setComments(comments);
	}
	
	public Long getDuration() {
		Recording recording = null;
		if (!getRecordings().isEmpty()) {
			recording = Iterables.getLast(getRecordings());
			return recording.getDuration();
		}
		return isFeedbackRecord() ? null : 0L;
	}
	
	@Override
	public void setDirty(boolean dirty) {
		customerModel.setDirty(dirty);
	}

	@Override
	public boolean isDirty() {
		return customerModel.isDirty();
	}

	@Override
	public String toString() {
		return getEntity().toString();
	}

}
