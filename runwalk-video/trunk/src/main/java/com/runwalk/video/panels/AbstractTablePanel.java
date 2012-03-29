package com.runwalk.video.panels;

import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Level;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.google.common.collect.Iterables;
import com.runwalk.video.core.AppComponent;
import com.runwalk.video.core.IAppComponent;
import com.runwalk.video.settings.SettingsManager;

@SuppressWarnings("serial")
@AppComponent
// TODO why do we need to implement the IAppComponent interface here beside the annotation?
public abstract class AbstractTablePanel<T extends Comparable<? super T>> extends JPanel implements IAppComponent {

	public static final String ROW_SELECTED = "rowSelected";
	public static final String CLIENT_SELECTED = "clientSelected";

	private static final String SELECTED_ITEM = "selectedItem";
	private static final String EVENT_LIST = "itemList";
	
	private static final String DIRTY = "dirty";
	
	private final JTable table;
	private final JTableMouseListener jTableMouseListener = new JTableMouseListener();
	
    private JButton firstButton, secondButton;
	private Boolean rowSelected = false;
	private EventList<T> sourceList;
	private EventList<T> itemList;
	private EventSelectionModel<T> eventSelectionModel;
	private T selectedItem;
	private TableFormat<T> tableFormat;
	private EventTableModel<T> eventTableModel;
	
	private Boolean dirty;

	protected AbstractTablePanel(LayoutManager mgr) {
		setLayout(mgr);
		table = new JTable();
		getTable().getTableHeader().setFont(SettingsManager.MAIN_FONT);
		getTable().setShowGrid(false);
		getTable().setFont(SettingsManager.MAIN_FONT);
	}

	public AbstractTablePanel() {
		this(null);
	}
	
	abstract void initialiseTableColumnModel();

	public boolean isRowSelected() {
		return rowSelected;
	}

	public void setRowSelected(boolean rowSelected) {
		firePropertyChange(ROW_SELECTED, this.rowSelected, this.rowSelected = rowSelected);
	}

	public Boolean isDirty() {
		return dirty;
	}

	public void setDirty(Boolean dirty) {
		firePropertyChange(DIRTY, this.dirty, this.dirty = dirty);
	}

	/**
	 * Verwijder het huidig geselecteerde item.
	 */
	public void clearSelectedItem() {
		getEventSelectionModel().clearSelection();
	}

	public void setSelectedItemRow(T selectedItem) {
		int rowIndex = getItemList().indexOf(selectedItem);
		if (rowIndex > -1) {
			getEventSelectionModel().getTogglingSelected().add(selectedItem);
			getTable().scrollRectToVisible(getTable().getCellRect(rowIndex, 0, true));
		}
	}

	public void setSelectedItemRow(int row) {
		T item = getItemList().get(row);
		if (row > -1 && item != null) {
			setSelectedItemRow(getItemList().get(row));
		}
	}
	
	/**
	 * Overwrite the selected item field by force and fire a PCE.
	 * A PCE should almost always be fired, except in the case 
	 * where both the old and new objects are exactly the same in memory.
	 * 
	 * TODO maybe there is a more elegant way to solve this problem?
	 * 
	 * @param selectedItem the selected item 
	 */
	protected void setSelectedItem(T selectedItem) {
		if (selectedItem != this.selectedItem && selectedItem != null && selectedItem.equals(this.selectedItem)) {
			this.selectedItem = null;
		}
		firePropertyChange(SELECTED_ITEM, this.selectedItem, this.selectedItem = selectedItem);
	}
	
	public T getSelectedItem() {
		return selectedItem;
	}

	public JTable getTable() {
		return table;
	}

	/**
	 * This method will add a {@link MouseListener} to the contained {@link JTable} in case it wasn't already added.
	 */
	public void registerClickHandler(ClickHandler<T> clickHandler) {
		List<MouseListener> mouseListeners = Arrays.asList(getTable().getMouseListeners());
		if (!mouseListeners.contains(jTableMouseListener)) {
			getTable().addMouseListener(jTableMouseListener);
		}
		jTableMouseListener.setClickHandler(clickHandler);
	}

	public JButton getFirstButton() {
		return firstButton;
	}
	
	public void setFirstButton(JButton deleteButton) {
		this.firstButton = deleteButton;
	}

	public JButton getSecondButton() {
		return secondButton;
	}

	public void setSecondButton(JButton newButton) {
		this.secondButton = newButton;
	}

	/**
	 * The {@link EventList} will be injected from the outside. This method will further prepare it to 
	 * use it with a {@link JTable}.
	 * 
	 * @param itemList The list
	 * @param itemConnector The connector that will forward changeEvents to the list.
	 */
	public void setItemList(EventList<T> itemList, ObservableElementList.Connector<? super T> itemConnector) {
		if (sourceList != null && itemList != null) {
			// dispose the current list, so it can be garbage collected
			sourceList.dispose();
		}
		sourceList = itemList;
		EventList<T> observedItems = new ObservableElementList<T>(itemList, itemConnector);
		SortedList<T> sortedItems = SortedList.create(observedItems);
		sortedItems.setMode(SortedList.AVOID_MOVING_ELEMENTS);
		EventList<T> specializedList = specializeItemList(sortedItems);
		firePropertyChange(EVENT_LIST, this.itemList, this.itemList = specializedList); 
		eventSelectionModel = new EventSelectionModel<T>(specializedList);
		eventSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		eventSelectionModel.getTogglingSelected().addListEventListener(new ListEventListener<T>() {

			public void listChanged(ListEvent<T> listChanges) {

				while(listChanges.next()) {
					int changeType = listChanges.getType();
					if (changeType == ListEvent.DELETE) {
						setRowSelected(!eventSelectionModel.getSelected().isEmpty());
					} else if (changeType == ListEvent.INSERT) {
						T newValue = null;
						EventList<T> sourceList = listChanges.getSourceList();
						if (!sourceList.isEmpty()) {
							newValue = Iterables.getOnlyElement(sourceList);
						}
						setSelectedItem(newValue);
						getLogger().log(Level.DEBUG, "Selected " + selectedItem.toString());
						setRowSelected(!eventSelectionModel.getSelected().isEmpty());
					}
				}
			}
		});
		eventTableModel = new EventTableModel<T>(specializedList, getTableFormat());
		getTable().setModel(eventTableModel);
		TableComparatorChooser.install(getTable(), sortedItems, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);
		getTable().setSelectionModel(eventSelectionModel);
		getTable().setColumnSelectionAllowed(false);
		initialiseTableColumnModel();
	}

	public void setItemList(EventList<T> itemList, Class<T> itemClass) {
		Connector<T> beanConnector = new BeanConnector<T>(itemClass);
		setItemList(itemList, beanConnector);
	}
	
	/**
	 * Persist the panel's dirty state. Override this method if you need to do something special to 
	 * have the entities' state persisted to the database.
	 * @return <code>true</code> if saving succeeded
	 */
	public boolean save() {
		return true;
	}

	/**
	 * Specialization hook for the set {@link ObservableElementList}. 
	 * You can override the exact type of the set {@link EventList} by implementing this method.
	 * 
	 * @param eventList The observable eventlist
	 * @return A specialized version of the observable eventlist
	 */
	protected EventList<T> specializeItemList(EventList<T> eventList) {
		return eventList;
	}

	public TableFormat<T> getTableFormat() {
		return tableFormat;
	}

	public void setTableFormat(TableFormat<T> tableFormat) {
		this.tableFormat = tableFormat;
	}

	public EventList<T> getItemList() {
		return itemList;
	}
	
	public EventList<T> getSourceList() {
		return sourceList;
	}

	public EventSelectionModel<T> getEventSelectionModel() {
		return eventSelectionModel;
	}
	
	protected EventTableModel<T> getEventTableModel() {
		return eventTableModel;
	}

	public interface ClickHandler<E> {
		
		void handleClick(E element);
		
	}

	private class JTableMouseListener extends MouseAdapter {
		
		private ClickHandler<T> clickHandler;

		public JTableMouseListener() { }
		
		protected void setClickHandler(ClickHandler<T> clickHandler) {
			this.clickHandler = clickHandler;
		}

		public void mouseClicked(MouseEvent e) {
			TableColumnModel columnModel = getTable().getColumnModel();
			int column = columnModel.getColumnIndexAtX(e.getX());
			int row    = e.getY() / getTable().getRowHeight();

			if(row >= getTable().getRowCount() || row < 0 ||
					column >= getTable().getColumnCount() || column < 0)
				return;
			// clicks will be handled if a jbutton renderer is installed on the column
			TableCellRenderer cellRenderer = getTable().getColumnModel().getColumn(column).getCellRenderer();
			if (cellRenderer instanceof AbstractButton) {
				clickHandler.handleClick(getEventTableModel().getElementAt(row));
			}
		}

	}


}