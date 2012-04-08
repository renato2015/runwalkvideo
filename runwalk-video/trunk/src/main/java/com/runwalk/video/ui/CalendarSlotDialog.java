package com.runwalk.video.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
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
import com.runwalk.video.settings.SettingsManager;
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
	
	private final CountDownLatch dismissSignal;

	public CalendarSlotDialog(Window parentWindow, CountDownLatch dismissSignal, EventList<T> calendarSlots, EventList<Client> clientList) {
		super(parentWindow);

		this.clientList = clientList;
		this.dismissSignal = dismissSignal;
		setLayout(new MigLayout());
		setTitle(getResourceMap().getString("calendarSlotDialog.title")); // NOI18N
		String borderTitle = getResourceMap().getString("calendarSlotDialog.border.title");
		JPanel calendarSlotTablePane = (JPanel) getContentPane();
		calendarSlotTablePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				borderTitle, TitledBorder.LEFT, TitledBorder.TOP, SettingsManager.MAIN_FONT.deriveFont(12))); // NOI18N
		JTable calendarEventTable = new JTable();
		calendarEventTable.getTableHeader().setFont(SettingsManager.MAIN_FONT);
		calendarEventTable.setShowGrid(false);
		calendarEventTable.setFont(SettingsManager.MAIN_FONT);
		CalendarSlotTableFormat tableFormat = new CalendarSlotTableFormat(getResourceMap());
		calendarSlotEventTableModel = new EventTableModel<T>(calendarSlots, tableFormat);
		calendarEventTable.setModel(calendarSlotEventTableModel);
		initialiseTableColumnModel(calendarEventTable.getColumnModel());
		// add components to the dialog's container
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(calendarEventTable);
		add(scrollPane, "wrap, grow");
/*		JButton cancelButton = new JButton(getAction(DISMISS_ACTION));
		add(cancelButton, "right align");*/
		JButton okButton = new JButton(getResourceMap().getString("save.Action.text"));
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
			
		});
		getRootPane().setDefaultButton(okButton);
		add(okButton, "right align");
		pack();
	}
	
	private void initialiseTableColumnModel(TableColumnModel columnModel) {
		columnModel.getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.DATE_FORMATTER));
		columnModel.getColumn(1).setPreferredWidth(70);
		columnModel.getColumn(2).setCellRenderer(new DateTableCellRenderer(AppUtil.HOUR_MINUTE_FORMATTER));
		columnModel.getColumn(2).setPreferredWidth(30);
		AutoCompleteCellEditor<Client> clientTableCellEditor = AutoCompleteSupport.createTableCellEditor(getClientList());
		clientTableCellEditor.setClickCountToStart(1);
		columnModel.getColumn(3).setCellRenderer(new JComboBoxTableCellRenderer());
		columnModel.getColumn(3).setCellEditor(clientTableCellEditor);
		columnModel.getColumn(3).setPreferredWidth(150);
	}
	
	@Action
	public void save() {
		clientList = null;
		getDismissSignal().countDown();
	}

	private EventList<Client> getClientList() {
		return clientList;
	}

	private CountDownLatch getDismissSignal() {
		return dismissSignal;
	}
	
}