package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.runwalk.video.util.ApplicationSettings;

public abstract class AbstractTablePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JButton firstButton, secondButton;

	public  AbstractTablePanel(AbstractTableModel<?> model, LayoutManager mgr) {
		super(mgr);
		table = new JTable(model);
		getTable().getTableHeader().setFont(ApplicationSettings.MAIN_FONT);
		getTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getTable().setShowGrid(false);
		getTable().setAutoCreateRowSorter(true);
		getTable().setFont(ApplicationSettings.MAIN_FONT);
	}
	
	public AbstractTablePanel(AbstractTableModel<?> model) {
		this(model, null);
	}
	
	protected AbstractTableModel<?> getGenericTableModel() {
		return (AbstractTableModel<?>) getTable().getModel();
	}

	public boolean isRowSelected() {
		return getTable().getSelectedRow() != -1;
	}

	public JTable getTable() {
		return table;
	}

	public void makeRowVisible(int row) {
		getTable().setRowSelectionInterval(row, row);
		getTable().scrollRectToVisible(getTable().getCellRect(row, 0, true));
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