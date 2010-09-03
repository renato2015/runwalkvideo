package com.runwalk.video.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.entities.Client;

/**
 * This {@link Dao} defines some specialized behavior for the {@link Client} entity.
 * 
 * @author Jeroen Peelaerts
 */
public class JpaClientDao extends JpaDao<Client> {

	public JpaClientDao(Class<Client> typeParameter, EntityManagerFactory entityManagerFactory) {
		super(typeParameter, entityManagerFactory);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Client> getAll() {
		EntityManager entityManager = getEntityManagerFactory().createEntityManager();
		Query query = entityManager.createQuery("SELECT DISTINCT e FROM " + getTypeParameter().getSimpleName() + " e ")
		.setHint("eclipselink.left-join-fetch", "c.analyses.recordings")
		.setHint("toplink.refresh", "true")
		.setHint("oracle.toplink.essentials.config.CascadePolicy", "CascadePrivateParts");
		return query.getResultList();
	}
	
	
}
