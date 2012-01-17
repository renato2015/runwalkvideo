package com.runwalk.video.dao;

import java.util.List;
import java.util.Set;

public interface Dao<E> {

	List<E> getAll();

	E getById(long id);

	List<E> getByIds(Set<Long> ids);

	E merge(E e);
	
	List<E> merge(Iterable<E> items);

	void delete(E e);
	
	void persist(E e);

	Class<E> getTypeParameter();

	void deleteById(Long id);

	List<E> getNewEntities(long id);

}
