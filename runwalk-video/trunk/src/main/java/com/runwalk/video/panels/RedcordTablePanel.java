package com.runwalk.video.panels;

import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;
import ca.odell.glazedlists.swing.TreeTableSupport;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.RedcordTableElement;
import com.runwalk.video.entities.RedcordTableElement.ExerciseDirection;
import com.runwalk.video.entities.RedcordTableElement.ExerciseType;
import com.runwalk.video.ui.DateTableCellRenderer;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class RedcordTablePanel extends AbstractTablePanel<RedcordTableElement> {

	private JTextArea comments;
	
	/**
	 * Create the panel.
	 */
	public RedcordTablePanel(ClientTablePanel clientTablePanel, UndoableEditListener undoableEditListener, 
			DaoService daoService) {
		super(new MigLayout("fill, nogrid"));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow, height :100:");
		
		setSecondButton(new JButton(getAction("addRedcordSession")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());
		
		setFirstButton(new JButton(getAction("deleteRedcordSession")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		add(getFirstButton());
		
		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		comments.getDocument().addUndoableEditListener(undoableEditListener);
		comments.setFont(AppSettings.MAIN_FONT);
		comments.setColumns(20);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		add(tscrollPane, "grow, height :60:");

		BindingGroup bindingGroup = new BindingGroup();
		//comments JTextArea binding
		BeanProperty<RedcordTablePanel, String> selectedItemComments = BeanProperty.create("selectedItem.comments");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<?, String, JTextArea, String> commentsBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, selectedItemComments, comments, jTextAreaValue);
		bindingGroup.addBinding(commentsBinding);

		BeanProperty<RedcordTablePanel, Boolean> isSelected = BeanProperty.create(ROW_SELECTED);
		BeanProperty<JTextArea, Boolean> jTextAreaEnabled = BeanProperty.create("enabled");
		Binding<?, Boolean, JTextArea, Boolean> enableCommentsBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, 
				isSelected, comments, jTextAreaEnabled);
		enableCommentsBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(enableCommentsBinding);

		BeanProperty<ClientTablePanel, Boolean> isClientSelected = BeanProperty.create(ROW_SELECTED);
		BeanProperty<RedcordTablePanel, Boolean> clientSelected = BeanProperty.create(CLIENT_SELECTED);
		Binding<?, Boolean, RedcordTablePanel, Boolean> clientSelectedBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ, clientTablePanel, isClientSelected, this, clientSelected);
		clientSelectedBinding.setSourceNullValue(false);
		clientSelectedBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(clientSelectedBinding);

	}

	@Override
	@SuppressWarnings("unchecked")
	protected TreeList<RedcordTableElement> specializeItemList(EventList<RedcordTableElement> eventList) {
		TreeList.Format<RedcordTableElement> listFormat = new TreeList.Format<RedcordTableElement>() {

			public void getPath(List<RedcordTableElement> paramList, RedcordTableElement redcordTableElement) {
				// TODO Auto-generated method stub
			}

			public boolean allowsChildren(RedcordTableElement redcordTableElement) {
				return redcordTableElement.allowsChildren();
			}

			public Comparator<RedcordTableElement> getComparator(int paramInt) {
				return null;
			}
			
		};
		return new TreeList<RedcordTableElement>(eventList, listFormat, TreeList.NODES_START_EXPANDED);
	}
	
	/**
	 * Covariant return here. We can do this cast because the return type of
	 * {@link #specializeItemList(EventList)} is a {@link TreeList}, as well.
	 * 
	 * @return a treelist containing the items for this panel
	 */
	@Override
	public TreeList<RedcordTableElement> getItemList() {
		return (TreeList<RedcordTableElement>) super.getItemList();
	}

	@Override
	public void setItemList(EventList<RedcordTableElement> itemList, ObservableElementList.Connector<RedcordTableElement> itemConnector) {
		super.setItemList(itemList, itemConnector);
		// a combobox renderer, used to set the enum values
		CustomJTableRenderer comboBoxRenderer = new CustomJTableRenderer(getTable().getDefaultRenderer(JComboBox.class));
		// name of the session / exercise
		getTable().getColumnModel().getColumn(0).setMinWidth(100);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		
		getTable().getColumnModel().getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
		// create special table cell editor for selecting exercise type
		EventList<ExerciseType> exerciseTypes = GlazedLists.eventListOf(ExerciseType.values());
		AutoCompleteCellEditor<ExerciseType> exerciseTypeTableCellEditor = AutoCompleteSupport.createTableCellEditor(exerciseTypes);
		getTable().getColumnModel().getColumn(2).setCellRenderer(comboBoxRenderer);
		getTable().getColumnModel().getColumn(2).setCellEditor(exerciseTypeTableCellEditor);
		getTable().getColumnModel().getColumn(2).setPreferredWidth(120);
		
		EventList<ExerciseDirection> exerciseDirections = GlazedLists.eventListOf(ExerciseDirection.values());
		AutoCompleteCellEditor<ExerciseDirection> exerciseDirectionTableCellEditor = AutoCompleteSupport.createTableCellEditor(exerciseDirections);
		getTable().getColumnModel().getColumn(3).setCellRenderer(comboBoxRenderer);
		getTable().getColumnModel().getColumn(3).setCellEditor(exerciseDirectionTableCellEditor);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(20);
		getTable().getColumnModel().getColumn(3).setResizable(false);
		// column to show comments
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		addMouseListenerToTable();				
		// add column info to the table here
		// install tree table support on the first column of the table
		TreeTableSupport.install(getTable(), getItemList(), 1);
	}

}
