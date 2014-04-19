package com.runwalk.video.dao.jpa;

import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.SuspendedSale;

public class SuspendedSaleDao extends JpaDao<SuspendedSale> {

	public SuspendedSaleDao(EntityManagerFactory entityManagerFactory) {
		super(SuspendedSale.class, entityManagerFactory);
	}

	public SuspendedSale getSuspendedSaleByClient(Client client) {
		TypedQuery<SuspendedSale> query = createEntityManager().createQuery(
				"SELECT suspendedSale from " + getTypeParameter().getSimpleName() + " suspendedSale WHERE "
						+ "suspendedSale.client.id = :clientId AND suspendedSale.saleTime = :saleTime", SuspendedSale.class);
		query.setParameter("clientId", client.getId());
		query.setParameter("saleTime", new Date(), TemporalType.DATE);
		return query.getSingleResult();
	}
	
	
}
