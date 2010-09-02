package com.runwalk.video.dao;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public interface DAO<E> {

	public List<E> getAll();

	public E getById(long id);
	
	public List<E> getByIds(Set<Long> ids);
	
	public E merge(E e);

	public void delete(E e);
	
	public Class<E> getTypeParameter();
	
	public abstract void deleteItemById(Long id);
	
	/**
	 * Set an {@link EntityManager} for the scope of an atomic transactional operation. 
	 */
	public abstract void setEntityManager(EntityManager entityManager);

}
