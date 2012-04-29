package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.QueryHints;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.Client;

/**
 * This {@link Dao} defines some specialized behavior for the {@link Client} entity.
 * 
 * @author Jeroen Peelaerts
 */
public class ClientDao extends JpaDao<Client> {

	public ClientDao(Class<Client> typeParameter, EntityManagerFactory entityManagerFactory) {
		super(typeParameter, entityManagerFactory);
	}

	@Override
	public List<Client> getAll() {
		TypedQuery<Client> query = createEntityManager().createQuery("SELECT client FROM " + getTypeParameter().getSimpleName() + " client", Client.class)
		.setHint(QueryHints.LEFT_FETCH, "client.analyses.recordings")
		.setHint(QueryHints.REFRESH, Boolean.TRUE.toString());
		return query.getResultList();
	}

	@Override
	public void persist(Client item) {
		super.persist(item);
		// remove client from second level cache
		getEntityManagerFactory().getCache().evict(Client.class, item.getId());
	}

	/**
	 * Need to override the default way of working here, as it seems that one to many
	 * collections are not always loaded correctly.
	 * 
	 * @param id The id to search for
	 * @return The returned client
	 */
	@Override
	public Client getById(Object id) {
		TypedQuery<Client> query = createEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id = :id", getTypeParameter())
				.setParameter("id", id)
		.setHint(QueryHints.LEFT_FETCH, "client.analyses.recordings")
		.setHint(QueryHints.REFRESH, Boolean.TRUE.toString());
		return query.getSingleResult();
	}
	
}
