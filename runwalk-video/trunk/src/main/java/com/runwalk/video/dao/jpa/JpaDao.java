package com.runwalk.video.dao.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;

import com.runwalk.video.dao.AbstractDao;
import com.runwalk.video.dao.Dao;

/**
 * This is a default {@link Dao} implementation for a J2SE application managed persistence context. 
 * This involves managing all transactions to the database manually.
 * 
 * @author Jeroen Peelaerts
 *
 * @param <E> class which this DAO will serve
 */
public class JpaDao<E> extends AbstractDao<E> {

	public static final int MAX_ROLLBACKS = 2;
	
	private final EntityManagerFactory entityManagerFactory;
	
	public JpaDao(Class<E> typeParameter, EntityManagerFactory entityManagerFactory) {
		super(typeParameter);
		this.entityManagerFactory = entityManagerFactory;
	}
	
	protected final EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}
	
	public void delete(E item) {
		EntityTransaction tx = null;
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			E mergedItem = entityManager.merge(item);
			entityManager.remove(mergedItem);
			tx.commit();
		} catch(PersistenceException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
	}

	/**
	 * Creates an {@link EntityManager} for use with the persistence operations in this class.
	 * 
	 * @return The result
	 */
	protected final EntityManager createEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}

	public void deleteById(Object id) {
		EntityTransaction tx = null;
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			E item = entityManager.find(getTypeParameter(), id);
			entityManager.remove(item);
			tx.commit();
		} catch(PersistenceException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
	}

	public List<E> getAll() {
		TypedQuery<E> query = createEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() +  " e", getTypeParameter())
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}
	
	public <T> List<E> getNewEntities(T id) {
		TypedQuery<E> query = createEntityManager().createQuery("SELECT e FROM " + getTypeParameter().getSimpleName() + " e WHERE e.id > :id", getTypeParameter())
		.setParameter("id", id).setHint(QueryHints.REFRESH, Boolean.TRUE.toString());
		return query.getResultList();
	}

	public E getById(Object id) {
		E result = null;
		EntityTransaction tx = null;
		EntityManager entityManager = createEntityManager();
		try {
			getEntityManagerFactory().getCache().evict(getTypeParameter(), id);
			tx = entityManager.getTransaction();
			tx.begin();
			result = entityManager.find(getTypeParameter(), id);
			tx.commit();
		} catch(PersistenceException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
		return result;
	}

	public List<E> getByIds(Set<?> ids) {
		TypedQuery<E> query = createEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id IN :ids", getTypeParameter())
				.setParameter("ids", ids)
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}

	public E merge(E entity) {
		EntityTransaction tx = null;
		E result = null;
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			result = entityManager.merge(entity);
			tx.commit();
		} catch(PersistenceException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
		return result;
	}
	
	public List<E> merge(Iterable<E> items) {
		EntityTransaction tx = null;
		List<E> result = new ArrayList<E>();
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			for(E item : items) {
				E mergedItem = entityManager.merge(item);
				result.add(mergedItem);
			}
			tx.commit();
		} catch(PersistenceException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
		return result;
	}
	
	private void persist(E item, int retryNumber) {
		EntityTransaction tx = null;
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			entityManager.persist(item);
			tx.commit();
		} catch(RollbackException e) {
			if (retryNumber < MAX_ROLLBACKS) {
				Logger.getLogger(getClass()).info("Retrying persist for " + item, e);
				persist(item, ++retryNumber);
			} else {
				throw e;
			}
		} catch(PersistenceException e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			throw e;
		} finally {
			entityManager.close();
		}
	}

	public void persist(E item) {
		persist(item, 0);
	}

}
