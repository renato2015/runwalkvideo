package com.runwalk.video.gui.panels;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.google.common.collect.Iterables;
import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.gui.AppComponent;
import com.runwalk.video.util.AppSettings;

@SuppressWarnings("serial")
public abstract class AbstractTablePanel<T extends SerializableEntity<T>> extends AppPanel implements AppComponent {

	private static final String EVENT_LIST = "itemList";
	protected static final String ROW_SELECTED = "rowSelected";
	private JTable table;
	private JButton firstButton, secondButton;
	private Boolean rowSelected = false;
	private EventList<T> itemList;
	private EventSelectionModel<T> eventSelectionModel;
	
	public AbstractTablePanel(LayoutManager mgr) {
		setLayout(mgr);
		table = new JTable();
		getTable().getTableHeader().setFont(AppSettings.MAIN_FONT);
		getTable().setShowGrid(false);
		getTable().setFont(AppSettings.MAIN_FONT);
	}
	
	public AbstractTablePanel() {
		this(null);
	}
	
	public boolean isRowSelected() {
		return this.rowSelected;
	}
	
	public void setRowSelected(boolean rowSelected) {
		this.firePropertyChange(ROW_SELECTED, this.rowSelected, this.rowSelected = rowSelected);
	}
	
	/**
	 * Verwijder het huidig geselecteerde item.
	 */
	public void clearSelectedItem() {
		getEventSelectionModel().clearSelection();
	}
	
	public T getSelectedItem() {
		T selectedItem = null;
		if (getEventSelectionModel() != null) {
			EventList<T> selected = getEventSelectionModel().getTogglingSelected();
			if (!selected.isEmpty()) {
				selectedItem = Iterables.getOnlyElement(selected);
			}
		}
		return selectedItem;
	}
	
	public JTable getTable() {
		return table;
	}
	
	public void setSelectedItem(T item) {
		getEventSelectionModel().getTogglingSelected().add(item);
	}

	public void setSelectedItem(int row) {
		if (row > -1) {
			setSelectedItem(getItemList().get(row));
		}
	}

	public JButton getFirstButton() {
		return firstButton;
	}

	public JButton getSecondButton() {
		return secondButton;
	}

	public void setFirstButton(JButton deleteButton) {
		this.firstButton = deleteButton;
	}

	public void setSecondButton(JButton newButton) {
		this.secondButton = newButton;
	}
	
	/**
	 * TODO dit kan efficienter!!
	 * 
	 * er zijn blijkbaar issues als je de referentie naar de gebruikte list veranderd.. 
	 * de JTable lijkt daar niet goed op te reageren
	 * @param newList
	 */
	public void setItemList(EventList<T> itemList, ObservableElementList.Connector<T> itemConnector) {
        EventList<T> observedItems = new ObservableElementList<T>(itemList, itemConnector);
        EventList<T> specializedList = specializeItemList(observedItems);
        SortedList<T> sortedItems = SortedList.create(specializedList);
        this.firePropertyChange(EVENT_LIST, this.itemList, this.itemList = sortedItems); 
		
        eventSelectionModel = new EventSelectionModel<T>(sortedItems);
        eventSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
        eventSelectionModel.getTogglingSelected().addListEventListener(new ListEventListener<T>() {
        	
        	public void listChanged(ListEvent<T> listChanges) {
        		 firePropertyChange("selectedItem", getSelectedItem(), listChanges.getSource());
        		 setRowSelected(!eventSelectionModel.getSelected().isEmpty());
        	}
        });
        EventTableModel<T> dataModel = new EventTableModel<T>(sortedItems, getTableFormat());
		getTable().setModel(dataModel);
        TableComparatorChooser.install(getTable(), sortedItems, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
        getTable().setSelectionModel(eventSelectionModel);
        getTable().setColumnSelectionAllowed(false);
	}
	
	public void setItemList(EventList<T> itemList, Class<T> itemClass) {
		setItemList(itemList, GlazedLists.beanConnector(itemClass));
	}
	
	/**
	 * Specialization hook for the set {@link ObservableElementList}. You can override the exact type of the set {@link EventList} by implementing this method.
	 * @param eventList The observable eventlist
	 * @return A specialized version of the observable eventlist
	 */
	protected EventList<T> specializeItemList(EventList<T> eventList) {
		return eventList;
	}
	
	public abstract TableFormat<T> getTableFormat();
	
	public EventList<T> getItemList() {
		return itemList;
	}
	
	public EventSelectionModel<T> getEventSelectionModel() {
		return eventSelectionModel;
	}
	
	public javax.swing.Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public ApplicationContext getContext() {
		return getApplication().getContext();
	}

	public Logger getLogger() {
		return Logger.getLogger(getClass());
	}

	public ResourceMap getResourceMap() {
		return getContext().getResourceMap(getClass(), AbstractTablePanel.class);
	}
	
	public ActionMap getApplicationActionMap() {
		return getContext().getActionMap(AbstractTablePanel.class, this);
	}

	
	protected class CustomJTableRenderer implements TableCellRenderer {
		private TableCellRenderer __defaultRenderer;

		public CustomJTableRenderer(TableCellRenderer renderer) {
			__defaultRenderer = renderer;
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected,
				boolean hasFocus,
				int row, int column)
		{
			if(value instanceof Component)
				return (Component)value;
			return __defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}
	
	class JTableButtonMouseListener extends MouseAdapter {

		  private void __forwardEventToButton(MouseEvent e) {
		    TableColumnModel columnModel = getTable().getColumnModel();
		    int column = columnModel.getColumnIndexAtX(e.getX());
		    int row    = e.getY() / getTable().getRowHeight();
		    Object value;
		    JButton button;
		    MouseEvent buttonEvent;

		    if(row >= getTable().getRowCount() || row < 0 ||
		       column >= getTable().getColumnCount() || column < 0)
		      return;

		    value = getTable().getValueAt(row, column);

		    if(!(value instanceof JButton))
		      return;

		    button = (JButton)value;

		    buttonEvent =
		      (MouseEvent)SwingUtilities.convertMouseEvent(getTable(), e, button);
		    button.dispatchEvent(buttonEvent);
		    // This is necessary so that when a button is pressed and released
		    // it gets rendered properly.  Otherwise, the button may still appear
		    // pressed down when it has been released.
		    getTable().repaint();
		  }

		  public void mouseClicked(MouseEvent e) {
		    __forwardEventToButton(e);
		  }
		  
		}


}