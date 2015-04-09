package com.runwalk.video.dao.jpa;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.model.AnalysisModel;

public class AnalysisDao extends JpaDao<Analysis> {

	public AnalysisDao(EntityManagerFactory entityManagerFactory) {
		super(Analysis.class, entityManagerFactory);
	}
	
	public List<Analysis> getAnalysesByCustomer(Customer customer) {
		TypedQuery<Analysis> query = createEntityManager().createQuery(
				"SELECT analysis FROM " + getTypeParameter().getSimpleName() + " analysis "
					+ "LEFT JOIN FETCH analysis.recordings WHERE analysis.customer = :customer ", Analysis.class);
		query.setParameter("customer", customer);
		return query.getResultList();
	}
	
	public List<AnalysisModel> getAnalysesAfterStartDateAsModels(Date startDate) {
		TypedQuery<AnalysisModel> query = createEntityManager().createQuery(
				"SELECT NEW com.runwalk.video.model.AnalysisModel(analysis.customer, analysis) FROM " + getTypeParameter().getSimpleName() + " analysis "
					+ "WHERE analysis.startDate >= :startDate AND analysis.feedbackId IS NULL AND analysis.appointmentExtref IS NOT NULL", AnalysisModel.class);
		query.setParameter("startDate", startDate);
		query.setHint(QueryHints.REFRESH, HintValues.TRUE);
		return query.getResultList();
	}

}
