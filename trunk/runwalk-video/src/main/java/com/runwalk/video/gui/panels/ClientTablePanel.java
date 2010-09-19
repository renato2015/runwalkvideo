package com.runwalk.video.gui.panels;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.runwalk.video.VideoFileManager;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.gui.DateTableCellRenderer;
import com.runwalk.video.gui.tasks.SaveTask;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class ClientTablePanel extends AbstractTablePanel<Client> {

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

		setSecondButton(new JButton(getAction("addClient")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());

		setFirstButton(new JButton(getAction("deleteClient")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		add(getFirstButton());

		JButton saveButton = new JButton(getAction("save"));
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
	public void setItemList(EventList<Client> itemList, Class<Client> itemClass) {
		super.setItemList(itemList, itemClass);
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
		final Task<List<Client>, Void> saveTask = new SaveTask<Client>(Client.class, getItemList(), getDaoService());
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
					// increment version field
					client.incrementVersion();
				}
			}

			@Override
			public void failed(TaskEvent<Throwable> event) {
				JOptionPane.showMessageDialog(
						SwingUtilities.windowForComponent(ClientTablePanel.this), 
						event.getValue().getMessage(), 
						getResourceMap().getString("save.errorMessage"), 
						JOptionPane.ERROR_MESSAGE);
			}

		});
		return saveTask;
	}

	public boolean isSaveNeeded() {
		return saveNeeded ;
	}

	public void setSaveNeeded(boolean saveNeeded) {
		this.firePropertyChange(SAVE_NEEDED, this.saveNeeded, this.saveNeeded = saveNeeded);
	}

	@Action
	public void addClient() {
		clearSearchField();
		Client client = new Client();
		getDaoService().getDao(Client.class).persist(client);
		getItemList().add(client);
		setSelectedItem(client);
		getApplication().getClientInfoPanel().requestFocus();
		setSaveNeeded(true);
	}

	@Action(enabledProperty = ROW_SELECTED)
	public void deleteClient() {
		int n = JOptionPane.showConfirmDialog(
				SwingUtilities.windowForComponent(this),
				getResourceMap().getString("deleteClient.confirmDialog.text"),
				getResourceMap().getString("deleteClient.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.CLOSED_OPTION)	return;
		int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
		Client selectedClient = getSelectedItem();
		// delete all video files for the selected client
		getVideoFileManager().deleteVideoFiles(selectedClient);
		getItemList().remove(selectedClient);
		// deleting a  should cascade to all its contained entities
		getDaoService().getDao(Client.class).delete(selectedClient);
		// select previous record
		setSelectedItem(lastSelectedRowIndex - 1);
		setSaveNeeded(true);
	}

	private void clearSearchField() {
		searchField.setText("");
	}

	public TableFormat<Client> getTableFormat() {
		return new ClientTableFormat();
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public class ClientTableFormat implements TableFormat<Client> {

		public int getColumnCount() {
			return 4;
		}

		public String getColumnName(int column) {
			if(column == 0)      return "ID";
			else if(column == 1) return "Naam";
			else if(column == 2) return "Voornaam";
			else if(column == 3) return "Datum laatste analyse";
			throw new IllegalStateException();
		}

		public Object getColumnValue(Client client, int column) {
			if(column == 0)      return client.getId();
			else if(column == 1) return client.getName();
			else if(column == 2) return client.getFirstname();
			else if(column == 3) return client.getLastAnalysisDate();
			throw new IllegalStateException();
		}
	}

}
