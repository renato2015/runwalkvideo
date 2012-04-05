package com.runwalk.video.ui;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;
import ca.odell.glazedlists.swing.EventTableModel;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.entities.Client;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.ui.table.JComboBoxTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@AppComponent
public class CalendarSlotDialog<T extends CalendarSlot<? super T>> extends JDialog {
	
	private static final String DISMISS_ACTION = "dismiss";

	private static final String SAVE_ACTION = "save";
	
	private EventList<Client> clientList;
	
	private EventTableModel<T> calendarSlotEventTableModel;

	public CalendarSlotDialog(Window parentWindow, EventList<T> calendarSlots, EventList<Client> clientList) {
		super(parentWindow);
		this.clientList = clientList;
		setLayout(new MigLayout());
		add(new JLabel("Synchroniseer afspraken met Google Calendar"), "wrap");
		JTable calendarEventTable = new JTable();
		CalendarSlotTableFormat tableFormat = new CalendarSlotTableFormat(getResourceMap());
		calendarSlotEventTableModel = new EventTableModel<T>(calendarSlots, tableFormat);
		calendarEventTable.setModel(calendarSlotEventTableModel);
		initialiseTableColumnModel(calendarEventTable.getColumnModel());
		add(calendarEventTable, "grow, wrap");
		JButton cancelButton = new JButton(getAction(DISMISS_ACTION));
		add(cancelButton, "right align");
		JButton okButton = new JButton(getAction(SAVE_ACTION));
		add(okButton, "right align");
	}
	
	private void initialiseTableColumnModel(TableColumnModel columnModel) {
		columnModel.getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
		columnModel.getColumn(2).setCellRenderer(new DateTableCellRenderer(AppUtil.DURATION_FORMATTER));
		AutoCompleteCellEditor<Client> clientTableCellEditor = AutoCompleteSupport.createTableCellEditor(getClientList());
		clientTableCellEditor.setClickCountToStart(1);
		columnModel.getColumn(3).setCellRenderer(new JComboBoxTableCellRenderer());
		columnModel.getColumn(3).setCellEditor(clientTableCellEditor);
		columnModel.getColumn(3).setPreferredWidth(120);
	}
	
	@Action
	public void dismiss() {
		clientList = null;
		setVisible(false);
		synchronized(this) {
			notifyAll();
		}
	}
	
	@Action
	public void save() {
		// actual saving should not be done here..
		synchronized(this) {
			notifyAll();
		}
	}

	private EventList<Client> getClientList() {
		return clientList;
	}
	
}