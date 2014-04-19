package com.runwalk.video.tasks;

import java.util.Collections;

import javax.persistence.NoResultException;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.SuspendedSaleDao;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Item;
import com.runwalk.video.entities.SuspendedSale;
import com.runwalk.video.entities.SuspendedSaleItem;
import com.runwalk.video.entities.SuspendedSaleItemTax;

public class CreateOrUpdateSuspendedSaleTask extends AbstractTask<Void, Void> {

	private final DaoService daoService;
	private final Item item;
	private final Client client;
	
	public CreateOrUpdateSuspendedSaleTask(DaoService daoService, Client client, Item item) {
		super("createOrUpdateSuspendedSale");
		this.daoService = daoService;
		this.client = client;
		this.item = item;
	}

	@Override
	protected Void doInBackground() throws Exception {
		message("startMessage");
		SuspendedSale suspendedSale = findOrCreateSuspendedSale(getClient());
		SuspendedSaleItem suspendedSaleItem = new SuspendedSaleItem(suspendedSale, getItem(), getClient());
		SuspendedSaleItemTax suspendedSaleItemTax = new SuspendedSaleItemTax(getItem(), suspendedSale);
		suspendedSale.setSaleItems(Collections.singletonList(suspendedSaleItem));
		SuspendedSaleDao suspendedSaleDao = getDaoService().getDao(SuspendedSale.class);
		suspendedSaleDao.merge(suspendedSale);
		Dao<SuspendedSaleItemTax> suspendedsSaleItemTaxDao = getDaoService().getDao(SuspendedSaleItemTax.class);
		suspendedsSaleItemTaxDao.persist(suspendedSaleItemTax);
		message("endMessage");
		return null;
	}
	
	private SuspendedSale findOrCreateSuspendedSale(Client client) {
		SuspendedSaleDao suspendedSaleDao = getDaoService().getDao(SuspendedSale.class);
		try {
			return suspendedSaleDao.getSuspendedSaleByClient(client);
		} catch (NoResultException e) {
			SuspendedSale suspendedSale = new SuspendedSale(client);
			suspendedSaleDao.persist(suspendedSale);
			return suspendedSale;
		}
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public Client getClient() {
		return client;
	}

	public Item getItem() {
		return item;
	}
	
}
