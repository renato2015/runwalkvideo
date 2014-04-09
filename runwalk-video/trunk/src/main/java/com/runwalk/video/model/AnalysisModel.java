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

	private static final String PROGRESSION = "progression";

	public AnalysisModel(Analysis entity) {
		super(entity);
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
	
	public boolean isRecorded() {
		for (Recording recording : getRecordings()) {
			if (recording.isRecorded()) {
				return true;
			}
		}
		return false;
	}

	public List<Recording> getRecordings() {
		return getEntity().getRecordings();
	}

	public Date getCreationDate() {
		return getEntity().getCreationDate();
	}

	public String getComments() {
		return getEntity().getComments();
	}

	public Item getArticle() {
		return getEntity().getArticle();
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

	public void setArticle(Item article) {
		firePropertyChange(ARTICLE, getEntity().getArticle(), article);
		getEntity().setArticle(article);
	}

	public void setCreationDate(Date creationDate) {
		firePropertyChange(CREATION_DATE, getEntity().getCreationDate(), creationDate);
		getEntity().setCreationDate(creationDate);
	}

	public void setProgression(Progression progression) {
		firePropertyChange(PROGRESSION, getEntity().getProgression(), progression);
		getEntity().setProgression(progression);
	}

	public Long getDuration() {
		Recording recording = null;
		if (!getRecordings().isEmpty()) {
			recording = Iterables.getLast(getRecordings());
			return recording.getDuration();
		}
		return isFeedbackRecord() ? null : 0L;
	}
	
}
