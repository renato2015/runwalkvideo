package com.runwalk.video.dao;

import java.util.List;
import java.util.Set;

public interface Dao<E> {

	List<E> getAll();

	E getById(Object id);

	List<E> getByIds(Set<?> ids);

	E merge(E e);
	
	List<E> merge(Iterable<E> items);

	void delete(E e);
	
	void persist(E e);

	Class<E> getTypeParameter();

	void deleteById(Object id);

	<T> List<E> getNewEntities(T id);

}
