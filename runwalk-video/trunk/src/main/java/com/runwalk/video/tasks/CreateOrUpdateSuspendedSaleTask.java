package com.runwalk.video.tasks;

import java.util.Iterator;

import javax.persistence.NoResultException;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.SuspendedSaleDao;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Item;
import com.runwalk.video.entities.SuspendedSale;
import com.runwalk.video.entities.SuspendedSaleItem;
import com.runwalk.video.entities.SuspendedSaleItemTax;

public class CreateOrUpdateSuspendedSaleTask extends AbstractTask<Void, Void> {

	private final DaoService daoService;
	private final Item oldItem;
	private final Item newItem;
	private final Client client;
	private final Long employeeId;
	
	public CreateOrUpdateSuspendedSaleTask(DaoService daoService, Client client, Item oldItem, Item newItem, Long employeeId) {
		super("createOrUpdateSuspendedSale");
		this.daoService = daoService;
		this.client = client;
		this.oldItem = oldItem;
		this.newItem = newItem;
		this.employeeId = employeeId;
	}

	@Override
	protected Void doInBackground() throws Exception {
		message("startMessage");
		SuspendedSale suspendedSale = findOrCreateSuspendedSale(getClient());
		replaceSuspendedSaleItems(suspendedSale);
		if (getNewItem() != null) {
			SuspendedSaleItem suspendedSaleItem = new SuspendedSaleItem(suspendedSale, getNewItem(), getClient());
			SuspendedSaleItemTax suspendedSaleItemTax = new SuspendedSaleItemTax(suspendedSale, getNewItem());
			suspendedSale.getSaleItems().add(suspendedSaleItem);
			suspendedSale.getSaleItemTaxes().add(suspendedSaleItemTax);
		}
		SuspendedSaleDao suspendedSaleDao = getDaoService().getDao(SuspendedSale.class);
		suspendedSaleDao.merge(suspendedSale);
		message("endMessage");
		return null;
	}

	private void replaceSuspendedSaleItems(SuspendedSale suspendedSale) {
		Iterator<SuspendedSaleItem> iterator = suspendedSale.getSaleItems().iterator();
		while (oldItem != null && iterator.hasNext()) {
			SuspendedSaleItem suspendedSaleItem = iterator.next();
			if (suspendedSaleItem.getItemId().equals(getOldItem().getId())) {
				getDaoService().getDao(SuspendedSaleItem.class).delete(suspendedSaleItem);
				iterator.remove();
			}
		}
	}
	
	private SuspendedSale findOrCreateSuspendedSale(Client client) {
		SuspendedSaleDao suspendedSaleDao = getDaoService().getDao(SuspendedSale.class);
		try {
			return suspendedSaleDao.getSuspendedSaleByClient(client);
		} catch (NoResultException e) {
			SuspendedSale suspendedSale = new SuspendedSale(client, employeeId);
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

	public Item getNewItem() {
		return newItem;
	}

	public Item getOldItem() {
		return oldItem;
	}

	public Long getEmployeeId() {
		return employeeId;
	}
	
}
