package com.runwalk.video.gui.panels;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;

import com.google.common.collect.Iterables;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.DateTableCellRenderer;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class AnalysisTablePanel extends AbstractTablePanel<Analysis> {

	private static final String CLIENT_SELECTED = "clientSelected";

	private static final String SELECTED_ITEM_RECORDED = "selectedItemRecorded";

	private JTextArea comments;

	private Boolean clientSelected = false;

	private EventList<Article> articleList;

	private final VideoFileManager videoFileManager;

	private final ClientTablePanel clientTablePanel;

	private final DaoService daoService;

	private final AppSettings appSettings;

	private boolean selectedItemRecorded;

	public AnalysisTablePanel(ClientTablePanel clientTablePanel, UndoableEditListener undoableEditListener, 
			AppSettings appSettings, VideoFileManager videoFileManager, DaoService daoService) {
		super(new MigLayout("fill, nogrid"));
		this.videoFileManager = videoFileManager;
		this.appSettings = appSettings;
		this.daoService = daoService;
		this.clientTablePanel = clientTablePanel;

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow, height :100:");

		setSecondButton(new JButton(getAction("addAnalysis")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());

		setFirstButton(new JButton(getAction("deleteAnalysis")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		add(getFirstButton(), "wrap");

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
		BeanProperty<AnalysisTablePanel, String> selectedItemComments = BeanProperty.create("selectedItem.comments");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<? extends AbstractTablePanel<?> , String, JTextArea, String> commentsBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, selectedItemComments, comments, jTextAreaValue);
		bindingGroup.addBinding(commentsBinding);

		BeanProperty<AnalysisTablePanel, Boolean> isSelected = BeanProperty.create(ROW_SELECTED);
		BeanProperty<JTextArea, Boolean> jTextAreaEnabled = BeanProperty.create("enabled");
		Binding<?, Boolean, JTextArea, Boolean> enableCommentsBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, 
				isSelected, comments, jTextAreaEnabled);
		enableCommentsBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(enableCommentsBinding);

		BeanProperty<ClientTablePanel, Boolean> isClientSelected = BeanProperty.create(ROW_SELECTED);
		BeanProperty<AnalysisTablePanel, Boolean> clientSelected = BeanProperty.create(CLIENT_SELECTED);
		Binding<? extends AbstractTablePanel<?>, Boolean, AnalysisTablePanel, Boolean> clientSelectedBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ, getClientTablePanel(), isClientSelected, this, clientSelected);
		clientSelectedBinding.setSourceNullValue(false);
		clientSelectedBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(clientSelectedBinding);

		ELProperty<AnalysisTablePanel, Boolean> recorded = ELProperty.create("${rowSelected && selectedItem.recorded}");
		BeanProperty<AnalysisTablePanel, Boolean> selectedItemRecorded = BeanProperty.create(SELECTED_ITEM_RECORDED);
		Binding<? extends AbstractTablePanel<?>, Boolean, AnalysisTablePanel, Boolean> selectedItemRecordedBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ, this, recorded, this, selectedItemRecorded);
		selectedItemRecordedBinding.setSourceUnreadableValue(false);
		selectedItemRecordedBinding.setTargetNullValue(false);
		bindingGroup.addBinding(selectedItemRecordedBinding);
		bindingGroup.bind();
	}

	public boolean isSelectedItemRecorded() {
		return selectedItemRecorded;
	}

	public void setSelectedItemRecorded(boolean selectedItemRecorded) {
		firePropertyChange(SELECTED_ITEM_RECORDED, this.selectedItemRecorded, this.selectedItemRecorded = selectedItemRecorded);
	}

	public void clearComments() {
		comments.setText("");
	}

	@Action(enabledProperty = CLIENT_SELECTED)
	public void addAnalysis() {
		//insert a new analysis record
		final Client selectedClient = getClientTablePanel().getSelectedItem();
		if (selectedClient.getName() == null && selectedClient.getOrganization() == null) {
			JOptionPane.showMessageDialog(null, 
					"Voer eerst een naam in voor deze klant!", 
					"Fout aanmaken analyse", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		getItemList().getReadWriteLock().writeLock().lock();
		Analysis analysis = new Analysis(selectedClient);
		getDaoService().getDao(Analysis.class).persist(analysis);
		try {
			selectedClient.addAnalysis(analysis);
			setSelectedItem(analysis);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
	}

	private ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	@Action(enabledProperty = ROW_SELECTED)
	public void deleteAnalysis() {		
		int n = JOptionPane.showConfirmDialog(
				null,
				getResourceMap().getString("deleteAnalysis.confirmDialog.text"),
				getResourceMap().getString("deleteAnalysis.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.CANCEL_OPTION ||n == JOptionPane.CLOSED_OPTION) return;
		getItemList().getReadWriteLock().writeLock().lock();
		try {
			int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
			Analysis selectedAnalysis = getSelectedItem();
			getItemList().remove(selectedAnalysis);
			getClientTablePanel().getSelectedItem().removeAnalysis(selectedAnalysis);
			// delete the video files
			for (Recording recording : selectedAnalysis.getRecordings()) {
				getVideoFileManager().deleteVideoFile(recording);
			}
			setSelectedItem(lastSelectedRowIndex - 1);
			getDaoService().getDao(Analysis.class).delete(selectedAnalysis);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
			//TODO handle failed delete?
		}
		//TODO kan je deze properties niet binden?? eventueel met een listener.. 
	}

	@Action(enabledProperty = SELECTED_ITEM_RECORDED)
	public void showVideoFile() throws IOException {
		List<Recording> recordings = getSelectedItem().getRecordings();
		if (!recordings.isEmpty()) {
			Recording lastRecording = Iterables.getLast(recordings);
			File videoFile = getVideoFileManager().getVideoFile(lastRecording);
			if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
				String[] commands = new String[] {"cmd.exe", "/c", "explorer /select," + videoFile.getAbsolutePath()};
//				String[] commands = new String[] {vlcPath, videoFile.getAbsolutePath(), " --rate=" + playRate};
				Runtime.getRuntime().exec(commands);
			}
		}
	}

	@Override
	protected EventList<Analysis> specializeItemList(EventList<Analysis> eventList) {
		eventList.addListEventListener(new ListEventListener<Analysis>() {

			public void listChanged(ListEvent<Analysis> listChanges) {
				while (listChanges.next()) {
					final int changeIndex = listChanges.getIndex();
					final int changeType = listChanges.getType();
					if (changeType == ListEvent.UPDATE) {
						getClientTablePanel().setSaveNeeded(true);
						Analysis changedItem = listChanges.getSourceList().get(changeIndex);
						changedItem.getClient().setDirty(true);
					}
				}
			}
		});
		return eventList;
	}

	public void setArticleList(EventList<Article> articleList) {
		if (this.articleList != null) {
			// dispose the current list, so it can be garbage collected
			this.articleList.dispose();
		}
		this.articleList = articleList;
	}

	public EventList<Article> getArticleList() {
		return this.articleList;
	}

	@Override
	public void setItemList(EventList<Analysis> itemList, ObservableElementList.Connector<Analysis> itemConnector) {
		super.setItemList(itemList, itemConnector);
		getTable().getColumnModel().getColumn(0).setMinWidth(70);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		// create special table cell editor for selecting articles
		AutoCompleteCellEditor<Article> createTableCellEditor = AutoCompleteSupport.createTableCellEditor(getArticleList());
		getTable().getColumnModel().getColumn(0).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
		CustomJTableRenderer comboBoxRenderer = new CustomJTableRenderer(getTable().getDefaultRenderer(JComboBox.class));
		getTable().getColumnModel().getColumn(1).setCellRenderer(comboBoxRenderer);
		getTable().getColumnModel().getColumn(1).setCellEditor(createTableCellEditor);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(18);
		getTable().getColumnModel().getColumn(3).setResizable(false);
		getTable().getColumnModel().getColumn(3).setCellRenderer(new DateTableCellRenderer(AppUtil.DURATION_FORMATTER));
		getTable().getColumnModel().getColumn(4).setPreferredWidth(40);
		getTable().getColumnModel().getColumn(4).setResizable(false);
		CustomJTableRenderer buttonRenderer = new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class));
		getTable().getColumnModel().getColumn(4).setCellRenderer(buttonRenderer);
		addMouseListenerToTable();
	}

	public boolean isClientSelected() {
		return clientSelected;
	}

	public void setClientSelected(boolean clientSelected) {
		this.firePropertyChange(CLIENT_SELECTED, this.clientSelected, this.clientSelected = clientSelected);
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	private DaoService getDaoService() {
		return daoService;
	}

}
