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
	
	public List<RecordingModel> getAllAsModels() {
		TypedQuery<RecordingModel> query = createEntityManager().createQuery(
				"SELECT NEW com.runwalk.video.model.RecordingModel(recording, analysis.creationDate, COUNT(recording.keyframes)) from " + 
						getTypeParameter().getSimpleName() + " recording LEFT JOIN FETCH recording.analysis analysis GROUP BY analysis.id", RecordingModel.class);
		return query.getResultList();
	}
	
}
