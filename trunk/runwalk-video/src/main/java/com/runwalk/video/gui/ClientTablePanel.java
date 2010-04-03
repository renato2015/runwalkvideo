package com.runwalk.video.gui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.tasks.SaveTask;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class ClientTablePanel extends AbstractTablePanel<Client> {

	private static final String SAVE_NEEDED = "saveNeeded";

	private JTextField searchField;

	private JTableBinding<Client, ClientTablePanel, JTable> jTableSelectionBinding;

	private boolean saveNeeded = false;

	@SuppressWarnings("unchecked")
	public ClientTablePanel() {
		String borderTitle = getResourceMap().getString("borderPanel.border.title");
		setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), borderTitle, TitledBorder.LEFT, TitledBorder.TOP, AppSettings.MAIN_FONT.deriveFont(12))); // NOI18N
		getTable().getTableHeader().setVisible(true);

		update();

		BeanProperty<ClientTablePanel, List<Client>> clients = BeanProperty.create("itemList");
		jTableSelectionBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, this, clients, getTable());
		BeanProperty<Client, Long> id = BeanProperty.create("id");
		JTableBinding<Client, ClientTablePanel, JTable>.ColumnBinding columnBinding = jTableSelectionBinding.addColumnBinding(id);
		columnBinding.setColumnName("Id");
		columnBinding.setColumnClass(Long.class);
		columnBinding.setEditable(false);

		BeanProperty<Client, String> firstname = BeanProperty.create("firstname");
		columnBinding = jTableSelectionBinding.addColumnBinding(firstname);
		columnBinding.setColumnName("Voornaam");
		columnBinding.setColumnClass(String.class);

		BeanProperty<Client, String> name = BeanProperty.create("name");
		columnBinding = jTableSelectionBinding.addColumnBinding(name);
		columnBinding.setColumnName("Naam");
		columnBinding.setColumnClass(String.class);

		BeanProperty<Client, Date> lastAnalysisDate = BeanProperty.create("lastAnalysisDate");
		columnBinding = jTableSelectionBinding.addColumnBinding(lastAnalysisDate);
		columnBinding.setColumnName("Datum laatste analyse");
		columnBinding.setColumnClass(String.class);
		columnBinding.setEditable(false);
		columnBinding.setConverter(new Converter<Date, String>() {

			@Override
			public String convertForward(Date arg0) {
				return AppUtil.formatDate(arg0, AppUtil.EXTENDED_DATE_FORMATTER);
			}

			@Override
			public Date convertReverse(String arg0) {
				/*try {
					return DateFormat.getInstance().parse(arg0);
				} catch (ParseException e) {*/
					return null;
				//}
			}
		});
		BindingGroup bindingGroup = new BindingGroup();
		jTableSelectionBinding.setSourceUnreadableValue(Collections.emptyList());
		jTableSelectionBinding.setSourceNullValue(Collections.emptyList());
		bindingGroup.addBinding(jTableSelectionBinding);
		jTableSelectionBinding.bind();

		getTable().getColumnModel().getColumn(0).setMinWidth(30);
		getTable().getColumnModel().getColumn(0).setPreferredWidth(30);
		getTable().getColumnModel().getColumn(0).setMaxWidth(30);
		//		getTable().getCellEditor().getTableCellEditorComponent(table, value, isSelected, row, column);
		getTable().getColumnModel().getColumn(1).setMinWidth(70);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);
		getTable().getColumnModel().getColumn(1).setMaxWidth(160);
		getTable().getColumnModel().getColumn(3).setMinWidth(80);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(100);
		getTable().getColumnModel().getColumn(3).setMaxWidth(100);
		JScrollPane masterScrollPane = new JScrollPane();
		masterScrollPane.setViewportView(getTable());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new AbsoluteLayout());

		setSecondButton(new JButton(getAction("addClient")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(getSecondButton(), new AbsoluteConstraints(0, 0, -1, -1));

		setFirstButton(new JButton(getAction("deleteClient")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(getFirstButton(), new AbsoluteConstraints(110, 0, -1, -1));

		JButton saveButton = new  JButton(getAction("save"));
		saveButton.setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(saveButton, new AbsoluteConstraints(230, 0, -1, -1));

		JPanel searchPanel = new JPanel();

		final Icon search = getResourceMap().getIcon("searchPanel.searchIcon");
		final Icon searchOverlay = getResourceMap().getIcon("searchPanel.searchOverlayIcon");

		searchField = new JTextField();
		searchField.setPreferredSize(new Dimension(100,20));
		searchField.setFont(AppSettings.MAIN_FONT);

		final JLabel theLabel = new JLabel(getResourceMap().getString("searchPanel.searchFieldLabel.text"));
		theLabel.setFont(AppSettings.MAIN_FONT);
		theLabel.setIcon(search);
		theLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				theLabel.setIcon(search);
				clearSearch();
				makeRowVisible(getTable().getSelectedRow());
			}
			public void mouseEntered(MouseEvent arg0) {
				theLabel.setIcon(searchOverlay);
			}
			public void mouseExited(MouseEvent arg0) {
				theLabel.setIcon(search);
			}
		});

		searchPanel.add(theLabel);
		searchPanel.add(searchField);

		BeanProperty<JTable, RowSorter<? extends TableModel>> rowSorter = BeanProperty.create("rowSorter");
		BeanProperty<JTextField, String> mask = BeanProperty.create("text");
		Binding<JTable, RowSorter<? extends TableModel>, JTextField, String> rowSorterBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, getTable(), rowSorter, searchField, mask);
		rowSorterBinding.setSourceUnreadableValue("");
		rowSorterBinding.setSourceNullValue("");
		rowSorterBinding.setConverter(new Converter<RowSorter<? extends TableModel>, String>() {

			@Override
			public String convertForward(RowSorter<? extends TableModel> rowSorter) {
				return rowSorter.toString();
			}

			@Override
			public RowSorter<? extends TableModel> convertReverse(String mask) {
				TableRowSorter<? extends TableModel> sorter = new TableRowSorter<TableModel>(getTable().getModel());

				// The following statement makes the filter case-sensitive. If you want 
				//filter to work in a case-insensitive way, uncomment the line below, comment 
				//the 7 code lines below
				//sorter.setRowFilter(RowFilter.regexFilter(".*" + mask + ".*"));

				//The following 7 lines create a case-insensitive filter. If you want 
				//the filter to be case-sensitive, comment them out and uncomment the 
				//line above
				String m = mask.toString();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < m.length(); i++) {
					char c = m.charAt(i);
					sb.append('[').append(Character.toLowerCase(c)).append(Character.toUpperCase(c)).append(']');
				}
				sorter.setRowFilter(RowFilter.regexFilter(".*" + sb + ".*"));
				return sorter;
			}

		});
		bindingGroup.addBinding(rowSorterBinding);
		bindingGroup.bind();
		clearSearch();

		buttonPanel.add(searchPanel, new AbsoluteConstraints(370, 0, 180, -1));

		//Layout the this panel..
		GroupLayout groupLayout = new GroupLayout(this);
		setLayout(groupLayout);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout.createSequentialGroup()
						.addContainerGap()
						.add(groupLayout.createParallelGroup(GroupLayout.LEADING)
								.add(groupLayout.createSequentialGroup()
										.add(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.add(85, 85, 85))
										.add(groupLayout.createSequentialGroup()
												.add(masterScrollPane, GroupLayout.PREFERRED_SIZE, 527, GroupLayout.PREFERRED_SIZE)
												.addContainerGap(21, Short.MAX_VALUE))))
		);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(GroupLayout.LEADING)
				.add(groupLayout.createSequentialGroup()
						.add(masterScrollPane, GroupLayout.PREFERRED_SIZE, 301, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.RELATED)
						.add(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

	}

	@Action(enabledProperty=SAVE_NEEDED)
	public Task<List<Client>, Void> save() {
		final Task<List<Client>, Void> saveTask = new SaveTask<Client>(getItemList());
		saveTask.addTaskListener(new TaskListener.Adapter<List<Client>, Void>() {

			@Override
			public void succeeded(TaskEvent<List<Client>> event) {
				setSaveNeeded(event.getValue() == null);
			}

		});
		return saveTask;
	}

	public boolean isSaveNeeded() {
		return saveNeeded ;
	}

	public void setSaveNeeded(boolean saveNeeded) {
		this.saveNeeded = saveNeeded;
		this.firePropertyChange(SAVE_NEEDED, !isSaveNeeded(), isSaveNeeded());
	}

	@Action
	public void addClient() {
		Client client = new Client();
		AppUtil.persistEntity(client);
		getItemList().add(client);
		refreshTableBindings();
		clearSearch();
		int clientCount = getItemList().size() - 1;
		int selectedRow = clientCount > 0 ? getTable().convertRowIndexToView(clientCount) : 0;
		makeRowVisible(selectedRow);
		getApplication().getClientInfoPanel().requestFocus();
		setSaveNeeded(true);
	}

	@Action(enabledProperty = ROW_SELECTED)
	public void deleteClient() {
		int n = JOptionPane.showConfirmDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				getResourceMap().getString("deleteClient.confirmDialog.text"),
				getResourceMap().getString("deleteClient.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION)	return;

		int selectedRow = getTable().getSelectedRow();
		Client clientToDelete = getSelectedItem();
		getItemList().remove(clientToDelete);
		AppUtil.deleteEntity(clientToDelete);
		//select previous records..
		if (selectedRow > 0) {
			makeRowVisible(selectedRow-1);
		} else {
			clearItemSelection();
		}
		getLogger().debug("Client " + clientToDelete.getId() +  " (" + clientToDelete.getName() + ") deleted.");
		setSaveNeeded(true);
	}

	private void clearSearch() {
		searchField.setText("");
	}

	@Override
	public void update() {
		Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createNamedQuery("findAllClients"); // NOI18N
		query.setHint("eclipselink.left-join-fetch", "c.unobservableAnalyses.recording");
		query.setHint("eclipselink.left-join-fetch", "c.city");
		setItemList(query.getResultList());	
		refreshTableBindings();
	}

}
