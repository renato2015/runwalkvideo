package com.runwalk.video.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Client;
import com.runwalk.video.util.ApplicationSettings;

public class ClientTablePanel extends AbstractTablePanel {

	private static final long serialVersionUID = 1L;

	private JPanel buttonPanel;
	private JButton saveButton;
	private JTextField searchField;

	private TableRowSorter<ClientTableModel> sorter;

	private JLabel theLabel;
	private Icon searchOverlay, search;

	public ClientTablePanel(AbstractTableModel<Client> model) {
		super(model);
		ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(ClientTablePanel.class);
		setBorder( BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), resourceMap.getString("borderPanel.border.title"), TitledBorder.LEFT, TitledBorder.TOP, ApplicationSettings.MAIN_FONT.deriveFont(12))); // NOI18N
		
		getTable().getTableHeader().setVisible(true);
		getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			//Selection changed!!
			public void valueChanged(ListSelectionEvent e) {
				RunwalkVideoApp.getApplication().getClientInfoPanel().setEnabled(isRowSelected());
				RunwalkVideoApp.getApplication().getTableActions().setClientSelected(isRowSelected());
				
				if (isRowSelected()) {
					int selected = getTable().getSelectedRow();
					int selectedModelIndex = getTable().convertRowIndexToModel(selected);
					if (getGenericTableModel().getSelectedIndex() != -1) {
						if (!RunwalkVideoApp.getApplication().getClientInfoPanel().saveData()) { 
							int previousSelected = getGenericTableModel().getSelectedIndex();
							makeRowVisible(previousSelected);
							getTable().setFocusable(false);
							RunwalkVideoApp.getApplication().getTableActions().setClientSelected(false);
							return;
						}
					}
					getGenericTableModel().setSelectedIndex(selectedModelIndex);
					//TODO herorganiseer delete en validatie hier..
					//TODO merge saveChanges and the saveData method in clientinfopanel??
					RunwalkVideoApp.getApplication().getAnalysisTableModel().update();
					getTable().setFocusable(true);
					RunwalkVideoApp.getApplication().getClientInfoPanel().showDetails(RunwalkVideoApp.getApplication().getSelectedClient());
					RunwalkVideoApp.getApplication().getAnalysisTablePanel().clearComments();
				}
			}
		});
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

		buttonPanel = new JPanel();
		buttonPanel.setLayout(new AbsoluteLayout());

		setSecondButton(new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("addClient")));
		getSecondButton().setFont(ApplicationSettings.MAIN_FONT);
		buttonPanel.add(getSecondButton(), new AbsoluteConstraints(0, 0, -1, -1));

		setFirstButton(new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("deleteClient")));
		getFirstButton().setFont(ApplicationSettings.MAIN_FONT);
		buttonPanel.add(getFirstButton(), new AbsoluteConstraints(110, 0, -1, -1));
		
		saveButton = new  JButton(RunwalkVideoApp.getApplication().getApplicationActionMap().get("save"));
		saveButton.setFont(ApplicationSettings.MAIN_FONT);
		buttonPanel.add(saveButton, new AbsoluteConstraints(230, 0, -1, -1));

		JPanel searchPanel = new JPanel();
		
		search = resourceMap.getIcon("searchPanel.searchIcon");
		searchOverlay = resourceMap.getIcon("searchPanel.searchOverlayIcon");

		theLabel = new JLabel(resourceMap.getString("searchPanel.searchFieldLabel.text"));
		theLabel.setFont(ApplicationSettings.MAIN_FONT);
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
		
		searchField = new JTextField();
		getSearchField().setPreferredSize(new Dimension(100,20));
		getSearchField().setFont(ApplicationSettings.MAIN_FONT);
		getSearchField().getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) { }
			public void insertUpdate(DocumentEvent e) {
				setSearchFilter();
			}
			public void removeUpdate(DocumentEvent e) {	
				setSearchFilter();
			}
		});
		getSearchField().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSearchFilter();
			}
		});
		getSearchField().setVisible(true);

		searchPanel.add(theLabel);
		searchPanel.add(getSearchField());
		
		sorter = new TableRowSorter<ClientTableModel>(RunwalkVideoApp.getApplication().getClientTableModel());
		getTable().setRowSorter(getSorter());
		
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
	
	private void setSearchFilter() {
        StringBuilder sb = new StringBuilder();
        String text = getSearchField().getText();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            sb.append('[').append(Character.toLowerCase(c)).append(Character.toUpperCase(c)).append(']');
        }
		RowFilter<ClientTableModel, Integer> filter = RowFilter.regexFilter(".*" + sb  + ".*");
		getSorter().setRowFilter(filter);
	}
	
	public void clearSearch() {
		RowFilter<ClientTableModel, Integer> filter = RowFilter.regexFilter(".*.*");
		getSorter().setRowFilter(filter);
		getSearchField().setText("");
	}

	private TableRowSorter<ClientTableModel> getSorter() {
		return sorter;
	}

	private JTextField getSearchField() {
		return searchField;
	}


}
