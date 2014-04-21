package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Recording;

public class RecordingDao extends JpaDao<Recording> {

	public RecordingDao(EntityManagerFactory entityManagerFactory) {
		super(Recording.class, entityManagerFactory);
	}
	
	public List<Recording> getRecordingsByStatusCode(int statusCode) {
		TypedQuery<Recording> query = createEntityManager().createQuery(
				"SELECT NEW com.runwalk.video.model.RecordingModel(recording, recording.analysis.creationDate) from " + 
						getTypeParameter().getSimpleName() + " recording WHERE recording.statusCode = :statusCode", Recording.class);
		query.setParameter("statusCode", statusCode);
		return query.getResultList();
	}

}
