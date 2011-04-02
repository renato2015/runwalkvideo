package com.runwalk.video.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
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

	private final EntityManagerFactory entityManagerFactory;

	public JpaDao(Class<E> typeParameter, EntityManagerFactory entityManagerFactory) {
		super(typeParameter);
		this.entityManagerFactory = entityManagerFactory;
	}
	
	private EntityManagerFactory getEntityManagerFactory() {
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
	protected EntityManager createEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}

	public void deleteById(Long id) {
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

	@SuppressWarnings("unchecked")
	public List<E> getAll() {
		EntityManager entityManager = createEntityManager();
		Query query = entityManager.createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e ")
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}

	public E getById(long id) {
		E result = null;
		EntityTransaction tx = null;
		EntityManager entityManager = createEntityManager();
		try {
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

	@SuppressWarnings("unchecked")
	public List<E> getByIds(Set<Long> ids) {
		EntityManager entityManager = createEntityManager();
		Query query = entityManager.createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id IN " + Iterables.toString(ids).replace("[", "(").replace("]", ")"))
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}

	public E merge(E item) {
		EntityTransaction tx = null;
		E result = null;
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			Logger.getLogger(getClass()).log(Level.INFO, "Merging " + item.toString());
			result = entityManager.merge(item);
			// force the version field to increment
			entityManager.lock(result, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
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
				Logger.getLogger(getClass()).log(Level.INFO, "Merging " + item.toString());
				E mergedItem = entityManager.merge(item);
				// force the version field to increment
				entityManager.lock(mergedItem, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
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

	public void persist(E item) {
		EntityTransaction tx = null;
		EntityManager entityManager = createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			entityManager.persist(item);
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

}
