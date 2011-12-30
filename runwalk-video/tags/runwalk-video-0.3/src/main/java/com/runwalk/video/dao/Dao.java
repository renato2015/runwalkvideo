package com.runwalk.video.dao;

import java.util.List;
import java.util.Set;

public interface Dao<E> {

	public List<E> getAll();

	public E getById(long id);

	public List<E> getByIds(Set<Long> ids);

	public E merge(E e);
	
	public List<E> merge(Iterable<E> items);

	public void delete(E e);
	
	public void persist(E e);

	public Class<E> getTypeParameter();

	public abstract void deleteById(Long id);

}
