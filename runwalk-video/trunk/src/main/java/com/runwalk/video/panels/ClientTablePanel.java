package com.runwalk.video.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.google.common.collect.Iterables;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.tasks.DeleteTask;
import com.runwalk.video.tasks.PersistTask;
import com.runwalk.video.tasks.RefreshEntityTask;
import com.runwalk.video.tasks.SaveTask;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class ClientTablePanel extends AbstractTablePanel<Client> {

	private static final String REFRESH_CLIENT_ACTION = "refreshClient";

	private static final String SAVE_CLIENT_ACTION = "save";

	private static final String DELETE_CLIENT_ACTION = "deleteClient";

	private static final String ADD_CLIENT_ACTION = "addClient";

	private static final String SAVE_NEEDED = "saveNeeded";

	private boolean saveNeeded = false;

	private final JTextField searchField;
	private final TextComponentMatcherEditor<Client> matcherEditor;

	private final VideoFileManager videoFileManager;
	private final DaoService daoService;

	public ClientTablePanel(VideoFileManager videoFileManager, DaoService daoManager) {
		super(new MigLayout("nogrid, fill"));
		this.videoFileManager = videoFileManager;
		this.daoService = daoManager;

		String borderTitle = getResourceMap().getString("borderPanel.border.title");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				borderTitle, TitledBorder.LEFT, TitledBorder.TOP, AppSettings.MAIN_FONT.deriveFont(12))); // NOI18N
		getTable().getTableHeader().setVisible(true);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");

		setSecondButton(new JButton(getAction(ADD_CLIENT_ACTION)));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());

		setFirstButton(new JButton(getAction(DELETE_CLIENT_ACTION)));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		add(getFirstButton());

		JButton refreshClientButton = new JButton(getAction(REFRESH_CLIENT_ACTION));
		refreshClientButton.setFont(AppSettings.MAIN_FONT);
		add(refreshClientButton);

		JButton saveButton = new JButton(getAction(SAVE_CLIENT_ACTION));
		saveButton.setFont(AppSettings.MAIN_FONT);
		add(saveButton);
		
		final Icon search = getResourceMap().getIcon("searchPanel.searchIcon");
		final Icon searchOverlay = getResourceMap().getIcon("searchPanel.searchOverlayIcon");

		searchField = new JTextField();
		searchField.setFont(AppSettings.MAIN_FONT);
		matcherEditor = new TextComponentMatcherEditor<Client>(searchField.getDocument(), GlazedLists.toStringTextFilterator());

		final JLabel theLabel = new JLabel(getResourceMap().getString("searchPanel.searchFieldLabel.text"));
		theLabel.setFont(AppSettings.MAIN_FONT);
		theLabel.setIcon(search);
		theLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				theLabel.setIcon(search);
				clearSearchField();
				setSelectedItem(getTable().getSelectedRow());
			}
			public void mouseEntered(MouseEvent arg0) {
				theLabel.setIcon(searchOverlay);
			}
			public void mouseExited(MouseEvent arg0) {
				theLabel.setIcon(search);
			}
		});

		add(theLabel, "gapleft push");
		add(searchField, "width :100:, growy");
	}

	@Override
	public void setItemList(EventList<Client> itemList, Connector<? super Client> connector) {
		super.setItemList(itemList, new BeanConnector<Client>(Client.class) { 

			@Override
			protected PropertyChangeListener createPropertyChangeListener() {
				return new PropertyChangeHandler() {

					@Override
					public void propertyChange(PropertyChangeEvent event) {
						if (Client.DIRTY.equals(event.getPropertyName())) {
							if ((Boolean) event.getNewValue()) {
								setSaveNeeded(true);
							}
						} else {
							super.propertyChange(event);
						}
					}
				};
			}
			
		});
		getTable().getColumnModel().getColumn(0).setMinWidth(35);
		getTable().getColumnModel().getColumn(0).setPreferredWidth(35);
		getTable().getColumnModel().getColumn(0).setMaxWidth(35);
		getTable().getColumnModel().getColumn(1).setMinWidth(70);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);
		getTable().getColumnModel().getColumn(1).setMaxWidth(160);
		getTable().getColumnModel().getColumn(3).setMinWidth(80);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(100);
		getTable().getColumnModel().getColumn(3).setMaxWidth(100);
		getTable().getColumnModel().getColumn(3).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
	}

	@Override
	protected EventList<Client> specializeItemList(EventList<Client> eventList) {
		FilterList<Client> filterList = new FilterList<Client>(eventList);
		filterList.setMatcherEditor(getSearchEngineTextFieldMatcherEditor());
		return filterList;
	}

	private TextComponentMatcherEditor<Client> getSearchEngineTextFieldMatcherEditor() {
		return matcherEditor;
	}

	@Action(enabledProperty = SAVE_NEEDED)
	public Task<List<Client>, Void> save() {
		final Task<List<Client>, Void> saveTask = new SaveTask<Client>(getDaoService(), Client.class, getItemList());
		saveTask.addTaskListener(new TaskListener.Adapter<List<Client>, Void>() {

			@Override
			public void succeeded(TaskEvent<List<Client>> event) {
				setSaveNeeded(event.getValue() == null);
				for(Client mergedClient : event.getValue()) {
					// these are the merged client instances
					int index = getItemList().indexOf(mergedClient);
					Client client = getItemList().get(index);
					// set dirty flag to false again
					client.setDirty(false);
					// set version field on old client
					if (mergedClient.getVersion() != client.getVersion()) {
						client.incrementVersion();
					}
				}
			}

		});
		return saveTask;
	}

	public boolean isSaveNeeded() {
		return saveNeeded;
	}

	public void setSaveNeeded(boolean saveNeeded) {
		this.firePropertyChange(SAVE_NEEDED, this.saveNeeded, this.saveNeeded = saveNeeded);
	}

	@Action(block = BlockingScope.ACTION)
	public PersistTask<Client> addClient() {
		clearSearchField();
		Client client = new Client();
		PersistTask<Client> result = new PersistTask<Client>(getDaoService(), Client.class, client);
		result.addTaskListener(new TaskListener.Adapter<Client, Void>() {
			
			@Override
			public void succeeded(TaskEvent<Client> event) {
				Client client = event.getValue();
				getItemList().add(client);
				setSelectedItem(client);
				ClientTablePanel.this.transferFocus();
				setSaveNeeded(true);
			}

		});
		return result;
	}

	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.ACTION)
	public DeleteTask<Client> deleteClient() {
		int n = JOptionPane.showConfirmDialog(
				SwingUtilities.windowForComponent(this),
				getResourceMap().getString("deleteClient.confirmDialog.text"),
				getResourceMap().getString("deleteClient.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION) return null;
		DeleteTask<Client> result = new DeleteTask<Client>(getDaoService(), Client.class, getSelectedItem());
		result.addTaskListener(new TaskListener.Adapter<Client, Void>() {

			@Override
			public void succeeded(TaskEvent<Client> event) {
				int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
				Client client = event.getValue();
				// delete all video files for the selected client
				getVideoFileManager().deleteVideoFiles(client);
				getItemList().remove(client);
				// select previous record
				setSelectedItem(lastSelectedRowIndex - 1);
				setSaveNeeded(true);
			}
			
		});
		return result;
	}
	
	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.APPLICATION)
	public RefreshEntityTask<Client> refreshClient() {
		RefreshEntityTask<Client> result = new RefreshEntityTask<Client>(getDaoService(), getItemList(), Client.class, getSelectedItem());
		result.addTaskListener(new TaskListener.Adapter<List<Client>, Void>() {

			@Override
			public void succeeded(TaskEvent<List<Client>> event) {
				List<Client> clientList = event.getValue();
				// selected client is the last one in the list
				Client selectedClient = Iterables.getLast(clientList);
				setSelectedItemProperty(selectedClient);
				// refresh file cache for newly added clients
				for (Client client : clientList) {
					getVideoFileManager().refreshCache(client.getAnalyses(), false);
				}
			}
			
		});
		
		return result;
	}

	private void clearSearchField() {
		searchField.setText("");
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public DaoService getDaoService() {
		return daoService;
	}

}
