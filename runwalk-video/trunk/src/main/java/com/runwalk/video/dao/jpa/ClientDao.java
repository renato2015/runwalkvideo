package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.HintValues;
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
		/*TypedQuery<Client> query = createEntityManager().createQuery(
				"SELECT DISTINCT NEW com.runwalk.video.entities.Client(c, MAX(a.creationDate)) " +
				"FROM Client c " +
				"LEFT OUTER JOIN c.analyses a GROUP BY c", Client.class)*/
		.setHint(QueryHints.REFRESH, HintValues.TRUE);
		return query.getResultList();
	}

	@Override
	public void persist(Client item) {
		super.persist(item);
		// remove client from second level cache
		evictFromCache(item.getId());
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
		evictFromCache(id);
		TypedQuery<Client> query = createEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id = :id", getTypeParameter())
				.setParameter("id", id)
		.setHint(QueryHints.LEFT_FETCH, "client.analyses.recordings")
		.setHint(QueryHints.LEFT_FETCH, "client.redcordSessions.redcordExercises")
		.setHint(QueryHints.REFRESH, HintValues.TRUE)
		//.setHint(QueryHints.REFRESH_CASCADE, CascadePolicy.CascadeAllParts)
		.setHint("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS)
		.setHint("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
		return query.getSingleResult();
	}
	
}
