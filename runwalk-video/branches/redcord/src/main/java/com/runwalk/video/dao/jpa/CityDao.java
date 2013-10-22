package com.runwalk.video.dao.jpa;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.eclipse.persistence.config.QueryHints;

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
		TypedQuery<City> query = createEntityManager().createQuery("SELECT city FROM " + getTypeParameter().getSimpleName() + " city", City.class)
				.setHint(QueryHints.LEFT_FETCH, "city.state");
		return query.getResultList();
	}
	
}
