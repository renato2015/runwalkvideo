package com.runwalk.video.gui;

import javax.persistence.Query;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Client;
import com.runwalk.video.util.ApplicationUtil;

@SuppressWarnings("serial")
public class ClientTableModel extends AbstractTableModel<Client> {

//	private HashMap<Long, String> creationDateMap;

	public ClientTableModel() {
		super("clientTableModel", 4);
		update();
//		cacheLastTimestamps();
	}
	
	/*private void cacheLastTimestamps() {
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		Query query = em.createQuery("SELECT an.client.id, an.creationDate FROM Client c, IN(c.analyses) an ORDER BY an.creationDate");
		creationDateMap = new HashMap<Long, String>();
		for(Object obj : query.getResultList()) {
			if (obj instanceof Object[]) {
				Object[] objects = (Object[]) obj;
				String timestamp = ApplicationUtil.formatDate((Date) objects[1], ApplicationUtil.EXTENDED_DATE_FORMATTER);
				Long clientKey = (Long) objects[0];
				creationDateMap.put(clientKey, timestamp);
//				Client cachedClient = em.getReference(Client.class, clientKey);
//				cachedClient.setFormattedLastAnalysisDate(timestamp);
			}
		}
	}
	
	public void updateTimestampCache() {
		//changes should be made to the cache..
		int analysisCount = getSelectedItem().getAnalyses().size();
		if (analysisCount > 0) {
			Analysis lastItem = getSelectedItem().getAnalyses().get(analysisCount - 1);
			creationDateMap.put(lastItem.getClient().getId(), lastItem.getTimeStamp());
//			lastItem.getClient().setFormattedLastAnalysisDate(lastItem.getTimeStamp());
		} else {
			creationDateMap.remove(getSelectedItem().getId());
//			getSelectedItem().setFormattedLastAnalysisDate(null);
		}
		updateSelectedRow();
	}
	
	public String getCachedTimestamp(Client client) {
		return creationDateMap.get(client.getId());
	}*/
	
	@Override
	public void refreshSelectedItem() {
		super.refreshSelectedItem();
		updateSelectedRow();
		RunwalkVideoApp.getApplication().getClientTablePanel().makeRowVisible(getSelectedIndex());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findAllClients"); // NOI18N
/*		List<Client> clientList = new ArrayList<Client>();
		for (Object o : query.getResultList()) {
			if (o instanceof Object[] && ((Object[]) o).length == 2) {
				Object[] result = (Object[]) o;
				Client client = (Client) result[0];
				Object object = result[1];
				if(object != null) {
					long time = ((Timestamp) object).getTime();
					Date lastAnalysisDate = new Date(time);
					client.setLastAnalysisDate(lastAnalysisDate);
				}
				clientList.add(client);
			}
		}*/
		setItemList(query.getResultList());
		sortItemList();
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return RunwalkVideoApp.getApplication().getClientTable().isFocusable() && column == 1 || column == 2;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		switch(column) {
		case 1: {
			getSelectedItem().setFirstname(value.toString());
			RunwalkVideoApp.getApplication().getClientInfoPanel().setFirstname(value.toString());
			break;
		}
		case 2: {
			if (getSelectedItem().getName() != null && getSelectedItem().getOrganization() != null) {
				if (getSelectedItem().getName().equals("") && !getSelectedItem().getOrganization().equals("")) {
					getSelectedItem().setOrganization(value.toString());
					return;
				}
				getSelectedItem().setName(value.toString());
			}
			RunwalkVideoApp.getApplication().getClientInfoPanel().setClientName(value.toString());
			break;
		}
		}
	}

	public int getColumnCount() {
		return colNames.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Client theClient = getItem(rowIndex);
		if (theClient != null) {
			switch(columnIndex) {
			case 0: return theClient.getId();
			case 1: return theClient.getFirstname();
			case 2: {
				if (theClient.getName() != null && theClient.getOrganization() != null) {
					if (theClient.getName().equals("") && !theClient.getOrganization().equals(""))
						return theClient.getOrganization();
				}
				return theClient.getName();
			}
			case 3:	return ApplicationUtil.formatDate(theClient.getLastAnalysisDate(), ApplicationUtil.EXTENDED_DATE_FORMATTER);
			};
		}
		return "";
	}
}
