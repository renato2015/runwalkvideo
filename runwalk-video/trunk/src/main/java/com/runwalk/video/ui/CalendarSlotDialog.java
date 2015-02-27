package com.runwalk.video.ui;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;
import ca.odell.glazedlists.swing.DefaultEventSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.entities.CalendarSlot;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.ui.table.JComboBoxTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@AppComponent
public class CalendarSlotDialog<T extends CalendarSlot<? super T>> extends JDialog {
	
	private static final String SAVE_ACTION = "save";
	
	private final CountDownLatch dismissSignal;
	
	private final DefaultEventSelectionModel<T> eventSelectionModel;
	
	private EventList<CustomerModel> customerModelList;

	private EventList<T> selected;
	
	public CalendarSlotDialog(Window parentWindow, final CountDownLatch dismissSignal, EventList<T> calendarSlots, EventList<CustomerModel> customerModelList) {
		super(parentWindow);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				save();
			}

		});
		this.customerModelList = customerModelList;
		this.dismissSignal = dismissSignal;
		setLayout(new MigLayout("insets dialog"));
		setTitle(getResourceMap().getString("calendarSlotDialog.title")); // NOI18N
		String borderTitle = getResourceMap().getString("calendarSlotDialog.border.title");
		JPanel calendarSlotTablePane = (JPanel) getContentPane();
		calendarSlotTablePane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				borderTitle, TitledBorder.LEFT, TitledBorder.TOP, SettingsManager.MAIN_FONT.deriveFont(12))); // NOI18N
		JTable calendarSlotTable = new JTable();
		calendarSlotTable.getTableHeader().setFont(SettingsManager.MAIN_FONT);
		calendarSlotTable.setShowGrid(false);
		calendarSlotTable.setFont(SettingsManager.MAIN_FONT);
		// create a table format
		CalendarSlotTableFormat tableFormat = new CalendarSlotTableFormat(getResourceMap());
		// create a sorted list
		SortedList<T> sortedCalendarSlots = SortedList.create(calendarSlots);
		sortedCalendarSlots.setMode(SortedList.AVOID_MOVING_ELEMENTS);
		// create a table model
		DefaultEventTableModel<T> calendarSlotEventTableModel = new DefaultEventTableModel<T>(sortedCalendarSlots, tableFormat);
		calendarSlotTable.setModel(calendarSlotEventTableModel);
		// create a selection model
		eventSelectionModel = new DefaultEventSelectionModel<T>(sortedCalendarSlots);
		eventSelectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
		calendarSlotTable.setSelectionModel(eventSelectionModel);
		// install sorters on the table
		TableComparatorChooser.install(calendarSlotTable, sortedCalendarSlots, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);
		// initialize column models
		initialiseTableColumnModel(calendarSlotTable.getColumnModel());
		// add components to the dialog's container
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(calendarSlotTable);
		add(scrollPane, "wrap, grow");
		JButton okButton = new JButton(getAction(SAVE_ACTION));
		getRootPane().setDefaultButton(okButton);
		add(okButton, "right align");
		pack();
		setLocationRelativeTo(null);
	}
	
	private void initialiseTableColumnModel(TableColumnModel columnModel) {
		columnModel.getColumn(0).setCellRenderer(new DateTableCellRenderer(AppUtil.DATE_FORMATTER));
		columnModel.getColumn(0).setPreferredWidth(70);
		columnModel.getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.HOUR_MINUTE_FORMATTER));
		columnModel.getColumn(1).setPreferredWidth(30);
		columnModel.getColumn(2).setPreferredWidth(40);
		columnModel.getColumn(3).setPreferredWidth(20);
		AutoCompleteCellEditor<CustomerModel> clientTableCellEditor = AutoCompleteSupport.createTableCellEditor(getCustomerList());
		clientTableCellEditor.setClickCountToStart(1);
		columnModel.getColumn(3).setCellRenderer(new JComboBoxTableCellRenderer<CustomerModel>(CustomerModel.class));
		columnModel.getColumn(3).setCellEditor(clientTableCellEditor);
		columnModel.getColumn(3).setPreferredWidth(150);
	}
	
	@Action
	public void save() {
		selected = eventSelectionModel.getSelected();
		//eventSelectionModel.dispose();
		customerModelList.dispose();
		customerModelList = null;
		setVisible(false);
		getDismissSignal().countDown();
		dispose();
	}
	
	private EventList<CustomerModel> getCustomerList() {
		return customerModelList;
	}
	
	public EventList<T> getSelected() {
		return selected;
	}

	private CountDownLatch getDismissSignal() {
		return dismissSignal;
	}
	
}