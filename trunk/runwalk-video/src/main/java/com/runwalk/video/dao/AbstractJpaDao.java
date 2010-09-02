package com.runwalk.video.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.google.common.collect.Iterables;
import com.runwalk.video.util.AppUtil;

public class AbstractJpaDao<E> extends AbstractDAO<E> {

	public AbstractJpaDao(Class<E> typeParameter) {
		super(typeParameter);
	}

	public void delete(E item) {
		EntityTransaction tx = null;
		try {
			tx = getEntityManager().getTransaction();
			tx.begin();
			getEntityManager().remove(item);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			getEntityManager().close();
		}
	}

	public void deleteItemById(Long id) {
		EntityTransaction tx = null;
		try {
			tx = getEntityManager().getTransaction();
			tx.begin();
			E item = getEntityManager().find(getTypeParameter(), id);
			getEntityManager().remove(item);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			getEntityManager().close();
		}
	}

	@SuppressWarnings("unchecked")
	public List<E> getAll() {
		Query query = getEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e ");
		return query.getResultList();
	}

	public E getById(long id) {
		E result = null;
		EntityTransaction tx = null;
		try {
			tx = getEntityManager().getTransaction();
			tx.begin();
			result = getEntityManager().find(getTypeParameter(), id);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			getEntityManager().close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<E> getByIds(Set<Long> ids) {
		Query query = getEntityManager().createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e " +
				"WHERE e.id IN " + Iterables.toString(ids).replace("[", "(").replace("]", ")"));
		return query.getResultList();
	}

	public E merge(E item) {
		EntityTransaction tx = null;
		E result = null;
		try {
			tx = getEntityManager().getTransaction();
			tx.begin();
			result = getEntityManager().merge(item);
			tx.commit();
		} catch(Exception e) {
			Logger.getLogger(AppUtil.class).error(e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			getEntityManager().close();
		}
		return result;
	}

}
