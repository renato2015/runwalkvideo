package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.swingbinding.JTableBinding;

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

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.util.AppSettings;

@SuppressWarnings("serial")
public abstract class AbstractTablePanel<T extends SerializableEntity<T>> extends AppPanel implements AppComponent {

	private static final String EVENT_LIST = "itemList";
	private static final String SELECTED_ITEM = "selectedItem";
	protected static final String ROW_SELECTED = "rowSelected";
	private JTable table;
	private JButton firstButton, secondButton;
	private Boolean rowSelected = false;
	private EventList<T> itemList;
	private EventSelectionModel<T> eventSelectionModel;
	private T selectedItem;
	protected JTableBinding<Analysis, ?, JTable> jTableSelectionBinding;
	//	private JTableBinding<Analysis, List<Analysis>, JTable> jTableSelectionBinding;
	
	public  AbstractTablePanel(LayoutManager mgr) {
		setLayout(mgr);
		table = new JTable();
		getTable().getTableHeader().setFont(AppSettings.MAIN_FONT);
		getTable().setShowGrid(false);
		getTable().setFont(AppSettings.MAIN_FONT);
		
		//rowSelected binding
		ELProperty<AbstractTablePanel<T>, Boolean> isSelected = ELProperty.create("${selectedItem != null}");
		BeanProperty<AbstractTablePanel<T>, Boolean> localIsSelected = BeanProperty.create(ROW_SELECTED);
		Binding<? extends AbstractTablePanel<T>, Boolean, ? extends AbstractTablePanel<T>, Boolean> rowSelectedBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ, this, isSelected , this, localIsSelected);
		rowSelectedBinding.setSourceNullValue(false);
		rowSelectedBinding.setSourceUnreadableValue(false);
		BindingGroup bindingGroup = new BindingGroup();
		bindingGroup.addBinding(rowSelectedBinding);
		bindingGroup.bind();
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
	public void clearItemSelection() {
		getEventSelectionModel().clearSelection();
	}
	
	public void setSelectedItem(T selectedItem) {
		this.firePropertyChange(SELECTED_ITEM, this.selectedItem, this.selectedItem = selectedItem);
	}
	
	public T getSelectedItem() {
		return selectedItem;
	}
	
	public JTable getTable() {
		return table;
	}
	
	public void makeRowVisible(T item) {
		getEventSelectionModel().getTogglingSelected().add(item);
	}

	public void makeRowVisible(int row) {
		if (row > -1) {
			makeRowVisible(getItemList().get(row));
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
	public void setItemList(EventList<T> itemList, Class<T> itemClass) {
		ObservableElementList.Connector<T> itemConnector = GlazedLists.beanConnector(itemClass);
        EventList<T> observedItems = new ObservableElementList<T>(itemList, itemConnector);
        this.firePropertyChange(EVENT_LIST, this.itemList, this.itemList = observedItems);
		SortedList<T> sortedItems = SortedList.create(observedItems);
        eventSelectionModel = new EventSelectionModel<T>(sortedItems);
        eventSelectionModel.getSelected().addListEventListener(new ListEventListener<T>() {

			@SuppressWarnings("unchecked")
			public void listChanged(ListEvent<T> listChanges) {
				EventList<T> source = (EventList) listChanges.getSource();
				if (!source.isEmpty()) {
					setSelectedItem(source.get(0));
				}
				
			}
		});
        eventSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
        getTable().setModel(new EventTableModel<T>(sortedItems, getTableFormat()));
        TableComparatorChooser.install(getTable(), sortedItems, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
        getTable().setSelectionModel(eventSelectionModel);
        getTable().setColumnSelectionAllowed(false);
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
	
	class JTableButtonMouseListener implements MouseListener {

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

		  public void mouseEntered(MouseEvent e) {
		    __forwardEventToButton(e);
		  }

		  public void mouseExited(MouseEvent e) {
		    __forwardEventToButton(e);
		  }

		  public void mousePressed(MouseEvent e) {
		    __forwardEventToButton(e);
		  }

		  public void mouseReleased(MouseEvent e) {
		    __forwardEventToButton(e);
		  }
		}


}