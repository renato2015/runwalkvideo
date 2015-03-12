package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.Customer;
import com.runwalk.video.model.CustomerModel;

/**
 * This {@link Dao} defines some specialized behavior for the {@link Customer} entity.
 * 
 * @author Jeroen Peelaerts
 */
public class CustomerDao extends JpaDao<Customer> {

	public CustomerDao(EntityManagerFactory entityManagerFactory) {
		super(Customer.class, entityManagerFactory);
	}
	
	public List<CustomerModel> getAllAsModels() {
		TypedQuery<CustomerModel> query = createEntityManager().createQuery("SELECT NEW "
				+ "com.runwalk.video.model.CustomerModel(customer, MAX(analyses.startDate)) FROM " + 
				getTypeParameter().getSimpleName() + " customer LEFT OUTER JOIN customer.analyses analyses "
						+ "WHERE analyses.feedbackId IS NULL GROUP BY customer.id", CustomerModel.class);
		return query.getResultList();
	}

	@Override
	public List<Customer> getAll() {
		TypedQuery<Customer> query = createEntityManager().createQuery("SELECT customer FROM " + getTypeParameter().getSimpleName() + " customer", Customer.class)
		.setHint(QueryHints.REFRESH, HintValues.TRUE);
		return query.getResultList();
	}

	@Override
	public void persist(Customer item) {
		super.persist(item);
		// remove customer from second level cache
		evictFromCache(item.getId());
	}

	/**
	 * Need to override the default way of working here, as it seems that one to many
	 * collections are not always loaded correctly.
	 * 
	 * @param id The id to search for
	 * @return The returned customer
	 */
	@Override
	public Customer getById(Object id) {
		evictFromCache(id);
		TypedQuery<Customer> query = createEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id = :id", getTypeParameter())
				.setParameter("id", id)
		.setHint(QueryHints.LEFT_FETCH, "customer.analyses.recordings")
		.setHint(QueryHints.REFRESH, HintValues.TRUE)
		//.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts)
		.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
		.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
		return query.getSingleResult();
	}
	
}
