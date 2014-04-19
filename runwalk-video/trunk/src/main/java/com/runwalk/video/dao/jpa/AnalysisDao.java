package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;

public class AnalysisDao extends JpaDao<Analysis> {

	public AnalysisDao(EntityManagerFactory entityManagerFactory) {
		super(Analysis.class, entityManagerFactory);
	}
	
	public List<Analysis> getAnalysesByClient(Client client) {
		TypedQuery<Analysis> query = createEntityManager().createQuery(
				"SELECT analysis FROM " + getTypeParameter().getSimpleName() + " analysis "
					+ "LEFT JOIN FETCH analysis.recordings WHERE analysis.client = :client ", Analysis.class);
		query.setParameter("client", client);
		return query.getResultList();
	}

}
