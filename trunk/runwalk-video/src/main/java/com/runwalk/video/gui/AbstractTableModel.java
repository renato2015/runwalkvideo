package com.runwalk.video.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;

import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.observablecollections.ObservableCollections;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.SerializableEntity;

public abstract class AbstractTableModel<T extends SerializableEntity<T>> extends javax.swing.table.AbstractTableModel {

	private static final long serialVersionUID = 1L;

	protected List<T> itemList;
	protected int selectedIndex = -1;
	protected List<String> colNames;

	private ResourceMap modelResourceMap;

	protected final Logger logger;

	protected final List<T> emptyList = Collections.emptyList();
	
	public static <T> void persistEntity(SerializableEntity<T> item) {
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			em.persist(item);
			tx.commit();
			Logger.getLogger(AbstractTableModel.class).debug(item.getClass().getSimpleName() + " with ID " + item.getId() + " was persisted.");
		}
		catch(Exception e) {
			Logger.getLogger(AbstractTableModel.class).error("Exception thrown while persisting entity." , e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
	}

	public static <T> void deleteEntity(SerializableEntity<T> detachedItem) {
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			SerializableEntity<T> mergedItem = em.merge(detachedItem);
			em.remove(mergedItem);
			tx.commit();
			Logger.getLogger(AbstractTableModel.class).debug(detachedItem.getClass().getSimpleName() + " with ID " + detachedItem.getId() + " removed from persistence.");
		}
		catch(Exception e) {
			Logger.getLogger(AbstractTableModel.class).error("Exception thrown while deleting entity.", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();
		}
	}

	public AbstractTableModel(String name, int colCount) {
		logger = Logger.getLogger(getClass());
		setModelResourceMap(Application.getInstance().getContext().getResourceMap(AbstractTableModel.class));

		colNames = new ArrayList<String>();
		for (int i = 1; i <= colCount; i ++) {
			colNames.add(getModelResourceMap().getString(name + ".columnModel.col" + i));
		}
	}
	
	public void sortItemList() {
		Collections.sort(itemList);
	}

	abstract public void update();

	public int getRowCount() {
		if (itemList == null) return 0;
		return getItemCount();
	}

	public int getColumnCount() {
		return colNames.size();
	}

	@Override
	public String getColumnName(int column) {
		return colNames.get(column);
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	protected List<T> getItemList() {
		return Collections.unmodifiableList(itemList);
	}

	public void setItemList(Collection<T> resultList) {
		itemList = ObservableCollections.observableList(new ArrayList<T>(resultList != null ? resultList : emptyList));
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	protected ResourceMap getModelResourceMap() {
		return modelResourceMap;
	}

	private void setModelResourceMap(ResourceMap modelResourceMap) {
		this.modelResourceMap = modelResourceMap;
	}

	public int addItem(T item ) {
		itemList.add(item);
		fireTableRowsInserted(getItemCount() - 1, getItemCount() - 1);
		return getItemCount() - 1;
	}
	
	/**
	 * Delete the entity by ID. Then find it's position in the list for this model.
	 * @param item The entity to be deleted
	 * @return The position of the deleted entity in the model's list.
	 */
	public int deleteItem(T item) {
		int index = getItemIndex(item);
		if (index != -1) {
			itemList.remove(index);
		}
		fireTableRowsDeleted(index, index);
		if (getSelectedIndex() == index) {
			clearItemSelection();
		}
		return index;
	}
	
	public int getItemCount() {
		if (itemList == null) {
			return 0;
		}
		return itemList.size();
	}
	
	public int getItemIndex(T item) {
		for (int i = 0; i < getItemCount(); i++) {
			T loopItem = getItem(i);
			if (item.getId() == loopItem.getId()) {
				return i;
			}
		}
		return -1;
	}
	
	public T getLastItem() {
		T result = null;
		if (itemList != null && !itemList.isEmpty()) {
			result = itemList.get(getItemCount() - 1);
		}
		return result;
	}

	public T getItem(int index) {
		if (index == -1 || itemList == null || getItemCount() == 0) {
			return null;
		}
		return itemList.get(index);
	}

	public void setItem(int index, T item) {
		itemList.set(index, item);
	}

	public void setSelectedItem(T item) {
		if (item == null) {
			clearItemSelection();			
		} else {
			setItem(getSelectedIndex(), item);
		}
	}

	/**
	 * Verwijder het huidig geselecteerde item.
	 */
	public void clearItemSelection() {
		setSelectedIndex(-1);
		TableModelListener[] tableModelListeners = getTableModelListeners();
		for (TableModelListener tableModelListener : tableModelListeners) {
			if (tableModelListener instanceof JTable) {
				JTable table = (JTable) tableModelListener;
				table.getSelectionModel().clearSelection();
				table.setEnabled(true);
			}
		}
	}

	public T getSelectedItem() {
		return getItem(getSelectedIndex());
	}
	
	public boolean isItemSelected() {
		return getSelectedIndex() == -1;
	}

	public void updateSelectedRow() {
		RunwalkVideoApp.getApplication().setSaveNeeded(true);
		fireTableRowsUpdated(getSelectedIndex(), getSelectedIndex());
	}
	
	public void saveSelectedItem() {
		saveItem(getSelectedIndex());
	}
	
	/**
	 * Save item by ID
	 * @param id the ID of the item
	 * @return the merged item
	 */
	public T saveItem(T item) {
		return saveItem(getItemIndex(item));
	}

	public T saveItem(int row) {
		T item = getItem(row);
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			T merged = em.merge(item);
			if (merged != null) {
				setItem(row, merged);
			}
			em.flush();
			tx.commit();
			return merged;
		} catch(Exception e) {
			logger.error("Exception thrown while saving item.", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			return null;
		} finally {
			em.close();
		}
	}

	public void refreshSelectedItem() {
		refreshItem(getSelectedIndex());
	}

	public void refreshItem(int row) {
		T item = getItem(row);
		EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
		EntityTransaction tx = null;
		try {
			tx = em.getTransaction();
			tx.begin();
			T merged = em.merge(item);
			em.refresh(merged);
			tx.commit();
			setItem(row, merged);
		}
		catch(Exception e) {
			logger.error("Exception thrown while refreshing item " + item.getId() + ".", e);
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
		} finally {
			em.close();		
		}
	}


}