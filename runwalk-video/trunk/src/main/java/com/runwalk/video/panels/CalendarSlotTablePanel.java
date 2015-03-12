package com.runwalk.video.panels;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;

import com.runwalk.video.core.AppComponent;
import com.runwalk.video.model.CalendarSlotModel;
import com.runwalk.video.model.CustomerModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.ui.table.JComboBoxTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@AppComponent
public class CalendarSlotTablePanel<T extends CalendarSlotModel<?>> extends AbstractTablePanel<T> {
	
	private static final String SAVE_SLOTS_ACTION = "saveSlots";
	
	private EventList<CustomerModel> customerModelList;

	public CalendarSlotTablePanel(EventList<CustomerModel> customerModelList) {
		this.customerModelList = customerModelList;
		setLayout(new MigLayout("insets dialog"));
		String borderTitle = getResourceMap().getString("calendarSlotDialog.border.title");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				borderTitle, TitledBorder.LEFT, TitledBorder.TOP, SettingsManager.MAIN_FONT.deriveFont(12))); // NOI18N
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");
		
		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JTextArea comments = new JTextArea();
		comments.setFont(SettingsManager.MAIN_FONT);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		
		BindingGroup bindingGroup = new BindingGroup();
		// comments JTextArea binding
		BeanProperty<CalendarSlotTablePanel<?>, String> selectedItemComments = BeanProperty.create("selectedItem.comments");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<? extends CalendarSlotTablePanel<?>, String, JTextArea, String> commentsBinding = 
				Bindings.createAutoBinding(UpdateStrategy.READ, this, selectedItemComments, comments, jTextAreaValue);
		bindingGroup.addBinding(commentsBinding);
		bindingGroup.bind();
		
		add(tscrollPane, "grow, height :60:, wrap");
		JButton okButton = new JButton(getAction(SAVE_SLOTS_ACTION));
		setFirstButton(okButton);
		add(okButton, "right align");
	}
	
	public void initialiseTableColumnModel() {
		TableColumnModel columnModel = getTable().getColumnModel();
		columnModel.getColumn(0).setCellRenderer(new DateTableCellRenderer(AppUtil.DATE_FORMATTER));
		columnModel.getColumn(0).setPreferredWidth(70);
		columnModel.getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.HOUR_MINUTE_FORMATTER));
		columnModel.getColumn(1).setPreferredWidth(30);
		columnModel.getColumn(2).setPreferredWidth(60);
		AutoCompleteCellEditor<CustomerModel> customerTableCellEditor = AutoCompleteSupport.createTableCellEditor(getCustomerList());
		customerTableCellEditor.setClickCountToStart(1);
		customerTableCellEditor.getComponent().setFont(SettingsManager.MAIN_FONT);
		JComboBoxTableCellRenderer<CustomerModel> customerTableCellRenderer = new JComboBoxTableCellRenderer<CustomerModel>(CustomerModel.class);
		customerTableCellRenderer.setFont(SettingsManager.MAIN_FONT);
		columnModel.getColumn(3).setCellRenderer(customerTableCellRenderer);
		columnModel.getColumn(3).setCellEditor(customerTableCellEditor);
		columnModel.getColumn(3).setPreferredWidth(150);
	}
	
	private EventList<CustomerModel> getCustomerList() {
		return customerModelList;
	}
	
	@Action
	public void saveSlots() {
		customerModelList = null;
		JDialog dialog = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
		dialog.setVisible(false);
		dialog.dispose();
	}
	
}