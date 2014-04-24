package com.runwalk.video.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.persistence.NoResultException;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList.Connector;
import ca.odell.glazedlists.impl.beans.BeanConnector;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.runwalk.video.dao.Dao;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.AbstractEntityModel;
import com.runwalk.video.model.ClientModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.DeleteTask;
import com.runwalk.video.tasks.PersistTask;
import com.runwalk.video.tasks.RefreshEntityTask;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class ClientTablePanel extends AbstractTablePanel<ClientModel> {

	private static final String SAVE_ACTION = "save";

	private static final String REFRESH_CLIENT_ACTION = "refreshClient";

	private static final String DELETE_CLIENT_ACTION = "deleteClient";

	private static final String ADD_CLIENT_ACTION = "addClient";

	private final JTextField searchField;
	private final TextComponentMatcherEditor<ClientModel> matcherEditor;

	private final VideoFileManager videoFileManager;
	private final DaoService daoService;

	public ClientTablePanel(VideoFileManager videoFileManager, DaoService daoManager) {
		super(new MigLayout("nogrid, fill"));
		this.videoFileManager = videoFileManager;
		this.daoService = daoManager;

		String borderTitle = getResourceMap().getString("clientTablePanel.border.title");
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
				borderTitle, TitledBorder.LEFT, TitledBorder.TOP, SettingsManager.MAIN_FONT.deriveFont(12))); // NOI18N
		getTable().getTableHeader().setVisible(true);
		getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");

		setSecondButton(new JButton(getAction(ADD_CLIENT_ACTION)));
		getSecondButton().setFont(SettingsManager.MAIN_FONT);
		add(getSecondButton());

		setFirstButton(new JButton(getAction(DELETE_CLIENT_ACTION)));
		getFirstButton().setFont(SettingsManager.MAIN_FONT);
		add(getFirstButton());

		JButton refreshClientButton = new JButton(getAction(REFRESH_CLIENT_ACTION));
		refreshClientButton.setFont(SettingsManager.MAIN_FONT);
		add(refreshClientButton);

		JButton saveButton = new JButton(getAction(SAVE_ACTION));
		saveButton.setFont(SettingsManager.MAIN_FONT);
		add(saveButton);

		final Icon search = getResourceMap().getIcon("searchPanel.searchIcon");
		final Icon searchOverlay = getResourceMap().getIcon("searchPanel.searchOverlayIcon");

		searchField = new JTextField();
		searchField.setFont(SettingsManager.MAIN_FONT);
		matcherEditor = new TextComponentMatcherEditor<ClientModel>(searchField.getDocument(), GlazedLists.toStringTextFilterator());

		final JLabel theLabel = new JLabel(getResourceMap().getString("searchPanel.searchFieldLabel.text"));
		theLabel.setFont(SettingsManager.MAIN_FONT);
		theLabel.setIcon(search);
		theLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				theLabel.setIcon(search);
				clearSearchField();
				getItemList().getReadWriteLock().readLock().lock();
				try {
					setSelectedItemRow(getTable().getSelectedRow());
				} finally {
					getItemList().getReadWriteLock().readLock().unlock();
				}
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
	public void setItemList(EventList<ClientModel> itemList, Connector<? super ClientModel> connector) {
		super.setItemList(itemList, new BeanConnector<ClientModel>(ClientModel.class) { 

			@Override
			protected PropertyChangeListener createPropertyChangeListener() {
				return new PropertyChangeHandler() {

					@Override
					public void propertyChange(PropertyChangeEvent event) {
						if (AbstractEntityModel.DIRTY.equals(event.getPropertyName())) {
							if ((Boolean) event.getNewValue()) {
								setDirty(true);
							}
						} else {
							super.propertyChange(event);
						}
					}
				};
			}

		});
	}

	public void initialiseTableColumnModel() {
		getTable().getColumnModel().getColumn(0).setPreferredWidth(35);
		getTable().getColumnModel().getColumn(0).setMaxWidth(35);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);
		getTable().getColumnModel().getColumn(1).setMaxWidth(160);
		getTable().getColumnModel().getColumn(3).setMinWidth(80);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(130);
		getTable().getColumnModel().getColumn(3).setMaxWidth(130);
		getTable().getColumnModel().getColumn(3).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
	}

	@Override
	protected EventList<ClientModel> specializeItemList(EventList<ClientModel> eventList) {
		FilterList<ClientModel> filterList = new FilterList<ClientModel>(eventList);
		filterList.setMatcherEditor(getSearchEngineTextFieldMatcherEditor());
		return filterList;
	}

	private TextComponentMatcherEditor<ClientModel> getSearchEngineTextFieldMatcherEditor() {
		return matcherEditor;
	}

	public boolean save() {
		getItemList().getReadWriteLock().readLock().lock();
		try {
			// advantage of dirty checking on the client is that we don't need to serialize the complete list for saving just a few items
			Dao<Client> dao = getDaoService().getDao(Client.class);
			for(ClientModel clientModel : getItemList()) {
				if (clientModel.isDirty()) {
					Client client = clientModel.getEntity();
					Client mergedClient = dao.merge(client);
					// set dirty flag to false again
					clientModel.setDirty(false);
					// set version field on old client
					if (mergedClient.getVersion() != client.getVersion()) {
						client.incrementVersion();
					}
				}
			}
		} finally {
			getItemList().getReadWriteLock().readLock().unlock();
		}
		return true;
	}

	@Action(block = BlockingScope.ACTION)
	public PersistTask<Client> addClient() {
		clearSearchField();
		Client client = new Client();
		PersistTask<Client> result = new PersistTask<Client>(getDaoService(), Client.class, client);
		result.addTaskListener(new TaskListener.Adapter<Client, Void>() {

			@Override
			public void succeeded(TaskEvent<Client> event) {
				addClientModel(new ClientModel(event.getValue()));
			}

		});
		return result;
	}
	
	/**
	 * Add the given {@link Client} to the table's list.
	 * 
	 * @param client The client to remove
	 */
	private void addClientModel(ClientModel clientModel) {
		getItemList().getReadWriteLock().writeLock().lock();
		try {
			getItemList().add(clientModel);
			setSelectedItemRow(clientModel);
			ClientTablePanel.this.transferFocus();
			setDirty(true);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
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
		final ClientModel selectedModel = getSelectedItem();
		DeleteTask<Client> result = new DeleteTask<Client>(getDaoService(), Client.class, selectedModel.getEntity());
		result.addTaskListener(new TaskListener.Adapter<Client, Void>() {

			@Override
			public void succeeded(TaskEvent<Client> event) {
				deleteClientModel(selectedModel);
			}

		});
		return result;
	}
	
	/**
	 * Remove the given Client from the table's list.
	 * 
	 * @param client The client to remove
	 */
	private void deleteClientModel(ClientModel clientModel) {
		getItemList().getReadWriteLock().writeLock().lock();
		try {
			int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
			// delete all video files for the selected client
			getVideoFileManager().deleteVideoFiles(clientModel.getEntity());
			getItemList().remove(clientModel);
			// select previous record
			if (lastSelectedRowIndex > 0) {
				setSelectedItemRow(lastSelectedRowIndex - 1);
			}
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
	}

	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.APPLICATION)
	public RefreshEntityTask<Client> refreshClient() {
		final ClientModel selectedModel = getSelectedItem();
		if (selectedModel.isDirty() && isDirty()) {
			int n = JOptionPane.showConfirmDialog(
					SwingUtilities.windowForComponent(this),
					getResourceMap().getString("refreshClient.confirmDialog.text"),
					getResourceMap().getString("refreshClient.Action.text"),
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION) return null;
		}
		return new RefreshEntityTask<Client>(getDaoService(), Client.class, getSelectedItem().getEntity()) {

			@Override
			protected Client doInBackground() throws Exception {
				Client client = super.doInBackground();
				// refresh file cache for newly added clients
				getItemList().getReadWriteLock().writeLock().lock();
				try {
					selectedModel.setEntity(client);
					selectedModel.setDirty(false);
				} finally {
					getItemList().getReadWriteLock().writeLock().unlock();
				}
				return client;
			}

			@Override
			protected void failed(Throwable throwable) {
				if (throwable instanceof NoResultException) {
					ClientTablePanel outerInstance = ClientTablePanel.this;
					JOptionPane.showMessageDialog( 
						SwingUtilities.windowForComponent(outerInstance),
						outerInstance.getResourceMap().getString("refreshClient.errorDialog.text"),
						outerInstance.getResourceMap().getString("refreshClient.errorDialog.title"),
						JOptionPane.WARNING_MESSAGE
					);
					deleteClientModel(selectedModel);
				} else {
					super.failed(throwable);
				}
			}

		};
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
