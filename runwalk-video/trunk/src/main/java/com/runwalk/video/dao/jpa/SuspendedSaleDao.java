package com.runwalk.video.dao.jpa;

import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.jdesktop.swingx.calendar.DateUtils;

import com.runwalk.video.entities.Customer;
import com.runwalk.video.entities.SuspendedSale;

public class SuspendedSaleDao extends JpaDao<SuspendedSale> {

	public SuspendedSaleDao(EntityManagerFactory entityManagerFactory) {
		super(SuspendedSale.class, entityManagerFactory);
	}

	public SuspendedSale getSuspendedSaleByCustomer(Customer customer) {
		TypedQuery<SuspendedSale> query = createEntityManager().createQuery(
				"SELECT suspendedSale from " + getTypeParameter().getSimpleName() + " suspendedSale WHERE "
						+ "suspendedSale.customer.id = :customerId AND suspendedSale.saleTime >= :minSaleTime", SuspendedSale.class);
		query.setParameter("customerId", customer.getId());
		query.setParameter("minSaleTime", DateUtils.startOfDay(new Date()));
		return query.getSingleResult();
	}
	
	
}
