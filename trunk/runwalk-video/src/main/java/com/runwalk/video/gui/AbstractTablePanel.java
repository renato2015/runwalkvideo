package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.swingbinding.JTableBinding;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.SerializableEntity;
import com.runwalk.video.gui.tasks.RefreshTask;
import com.runwalk.video.util.AppSettings;

@SuppressWarnings("serial")
public abstract class AbstractTablePanel<T extends SerializableEntity<T>> extends AppPanel implements AppComponent {

	private static final String SELECTED_ITEM = "selectedItem";
	protected static final String ROW_SELECTED = "rowSelected";
	private JTable table;
	private JButton firstButton, secondButton;
	private Boolean rowSelected = false;
	private List<T> itemList;
	private T selectedItem;
	protected JTableBinding<Analysis, ?, JTable> jTableSelectionBinding;
	//	private JTableBinding<Analysis, List<Analysis>, JTable> jTableSelectionBinding;
	
	public  AbstractTablePanel(LayoutManager mgr) {
		setLayout(mgr);
		table = new JTable();
		getTable().getTableHeader().setFont(AppSettings.MAIN_FONT);
		getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTable().setShowGrid(false);
		getTable().setAutoCreateRowSorter(true);
		getTable().setUpdateSelectionOnSort(false);
		getTable().setFont(AppSettings.MAIN_FONT);
		
		//selectedItem binding
		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<JTable, T> selectedElement = BeanProperty.create("selectedElement");
		BeanProperty<AbstractTablePanel<T>, T> localSelectedElement = BeanProperty.create(SELECTED_ITEM);
		Binding<JTable, T, ? extends AbstractTablePanel<T>, T> selectedElementBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getTable(), 
				selectedElement , this, localSelectedElement);
		selectedElementBinding.setSourceNullValue(null);
		selectedElementBinding.setSourceUnreadableValue(null);
		bindingGroup.addBinding(selectedElementBinding);
		
		//rowSelected binding
		ELProperty<JTable, Boolean> isSelected = ELProperty.create("${selectedElement != null}");
		BeanProperty<AbstractTablePanel<T>, Boolean> localIsSelected = BeanProperty.create(ROW_SELECTED);
		Binding<JTable, Boolean, ? extends AbstractTablePanel<T>, Boolean> rowSelectedBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getTable(), 
				isSelected , this, localIsSelected);
		rowSelectedBinding.setSourceNullValue(false);
		rowSelectedBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(rowSelectedBinding);
		bindingGroup.bind();
	}
	
	public AbstractTablePanel() {
		this(null);
	}
	
	@Action
	public abstract void update();
	
	/**
	 * This calls the implementation of the {@link #update()} method in a created {@link Task}.
	 * @return the created task
	 */
	@Action(block=Task.BlockingScope.ACTION)
	public Task<Boolean, Void> refresh() {
		return new RefreshTask(this);
	}

	public boolean isRowSelected() {
//		return getTable().getSelectedRow() != -1;
		return this.rowSelected;
	}
	
	public void setRowSelected(boolean rowSelected) {
		this.firePropertyChange(ROW_SELECTED, this.rowSelected, this.rowSelected = rowSelected);
	}
	
	/**
	 * Verwijder het huidig geselecteerde item.
	 */
	public void clearItemSelection() {
		getTable().clearSelection();
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
	
	protected void refreshTableBindings() {
		if (jTableSelectionBinding != null) {
			jTableSelectionBinding.refreshAndNotify();
		}
	}

	public void makeRowVisible(int row) {
		if (row != -1) {
			getTable().setRowSelectionInterval(row, row);
			getTable().scrollRectToVisible(getTable().getCellRect(row, 0, true));
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
	public void setItemList(List<T> itemList) {
		if (this.itemList != null) {
			this.itemList.retainAll(itemList);
			if (!this.itemList.containsAll(itemList)) {
				itemList.removeAll(this.itemList);
				this.itemList.addAll(this.itemList);
			}
		} else {
			this.itemList = ObservableCollections.observableList(itemList);
		}
		this.firePropertyChange("itemList", this.itemList, this.itemList);
	}
	
	public List<T> getItemList() {
		return itemList;
	}
	
	public javax.swing.Action getAction(String name) {
		return getApplicationActionMap().get(name);
	}

	public RunwalkVideoApp getApplication() {
		return RunwalkVideoApp.getApplication();
	}

	public JPanel getComponent() {
		return this;
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