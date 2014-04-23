package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Recording;
import com.runwalk.video.model.RecordingModel;

public class RecordingDao extends JpaDao<Recording> {

	public RecordingDao(EntityManagerFactory entityManagerFactory) {
		super(Recording.class, entityManagerFactory);
	}
	
	public List<RecordingModel> getRecordingsAsModelByStatusCode(int statusCode) {
		TypedQuery<RecordingModel> query = createEntityManager().createQuery(
				"SELECT NEW com.runwalk.video.model.RecordingModel(recording, recording.analysis.creationDate, COUNT(recording.keyframes)) from " + 
						getTypeParameter().getSimpleName() + " recording WHERE recording.statusCode = :statusCode", RecordingModel.class);
		query.setParameter("statusCode", statusCode);
		return query.getResultList();
	}

}
