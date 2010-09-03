package com.runwalk.video.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
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

	protected EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public void delete(E item) {
		EntityTransaction tx = null;
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			entityManager.remove(item);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			entityManager.close();
		}
	}

	public void deleteById(Long id) {
		EntityTransaction tx = null;
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			E item = entityManager.find(getTypeParameter(), id);
			entityManager.remove(item);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			entityManager.close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<E> getAll() {
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		Query query = entityManager.createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e ")
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}

	public E getById(long id) {
		E result = null;
		EntityTransaction tx = null;
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			result = entityManager.find(getTypeParameter(), id);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			entityManager.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<E> getByIds(Set<Long> ids) {
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		Query query = entityManager.createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id IN " + Iterables.toString(ids).replace("[", "(").replace("]", ")"))
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}

	public E merge(E item) throws OptimisticLockException {
		EntityTransaction tx = null;
		E result = null;
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			Logger.getLogger(getClass()).log(Level.INFO, "Merging " + item.toString());
			result = entityManager.merge(item);
			// force the version field to increment
			entityManager.lock(result, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
			tx.commit();
		} catch (OptimisticLockException exc) {
			throw exc;
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			entityManager.close();
		}
		return result;
	}
	
	public List<E> merge(Iterable<E> items) throws OptimisticLockException {
		EntityTransaction tx = null;
		List<E> result = new ArrayList<E>();
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
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
		} catch (OptimisticLockException exc) {
			throw exc;
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			entityManager.close();
		}
		return result;
	}

	public void persist(E item) {
		EntityTransaction tx = null;
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		try {
			tx = entityManager.getTransaction();
			tx.begin();
			entityManager.persist(item);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(getClass()).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			entityManager.close();
		}
	}

}
