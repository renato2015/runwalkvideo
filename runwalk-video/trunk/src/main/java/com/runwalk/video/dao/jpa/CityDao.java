package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.City;

/**
 * This {@link Dao} defines some specialized behavior for the {@link City} entity.
 * 
 * @author Jeroen Peelaerts
 */

public class CityDao extends JpaDao<City> {
	public CityDao(EntityManagerFactory entityManagerFactory) {
		super(City.class, entityManagerFactory);
	}

	@Override
	public List<City> getAll() {
		TypedQuery<City> query = createEntityManager().createQuery("SELECT person.address.city FROM Person person", City.class);
		return query.getResultList();
	}
	
}
