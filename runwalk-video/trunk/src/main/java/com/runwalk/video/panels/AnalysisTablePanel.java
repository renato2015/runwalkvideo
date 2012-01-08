package com.runwalk.video.panels;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.application.utils.AppHelper;
import org.jdesktop.application.utils.PlatformType;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;

import com.google.common.collect.Iterables;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.tasks.DeleteTask;
import com.runwalk.video.tasks.PersistTask;
import com.runwalk.video.ui.DateTableCellRenderer;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class AnalysisTablePanel extends AbstractTablePanel<Analysis> {

	private static final String SELECTED_ITEM_RECORDED = "selectedItemRecorded";

	private final JTextArea comments;

	private final VideoFileManager videoFileManager;

	private final ClientTablePanel clientTablePanel;

	private final DaoService daoService;

	private final AppSettings appSettings;

	private Boolean clientSelected = false;
	
	private EventList<Article> articleList;

	private boolean selectedItemRecorded;

	public AnalysisTablePanel(ClientTablePanel clientTablePanel, UndoableEditListener undoableEditListener, 
			AppSettings appSettings, VideoFileManager videoFileManager, DaoService daoService) {
		super(new MigLayout("fill, nogrid"));
		this.clientTablePanel = clientTablePanel;
		this.appSettings = appSettings;
		this.videoFileManager = videoFileManager;
		this.daoService = daoService;

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow, height :100:");

		setFirstButton(new JButton(getAction("addAnalysis")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		add(getFirstButton());

		setSecondButton(new JButton(getAction("deleteAnalysis")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton(), "wrap");

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

	@Action(enabledProperty = CLIENT_SELECTED, block = BlockingScope.ACTION)
	public PersistTask<Analysis> addAnalysis() {
		// insert a new analysis record
		final Client selectedClient = getClientTablePanel().getSelectedItem();
		if (("".equals(selectedClient.getName())  || selectedClient.getName() == null) && 
			("".equals(selectedClient.getOrganization()) || selectedClient.getOrganization() == null)) {
			JOptionPane.showMessageDialog(
					SwingUtilities.windowForComponent(this), 
					getResourceMap().getString("addAnalysis.errorDialog.text"),
					getResourceMap().getString("addAnalysis.Action.text"), 
					JOptionPane.ERROR_MESSAGE);
			getLogger().warn("Attempt to insert analysis for " + selectedClient + " failed.");
			return null;
		}
		Analysis analysis = new Analysis(selectedClient);
		PersistTask<Analysis> result = new PersistTask<Analysis>(getDaoService(), Analysis.class, analysis);
		result.addTaskListener(new TaskListener.Adapter<Analysis, Void>() {

			@Override
			public void succeeded(TaskEvent<Analysis> event) {
				Analysis result = event.getValue();
				getItemList().getReadWriteLock().writeLock().lock();
				try {
					selectedClient.addAnalysis(result);
					setSelectedItem(result);
				} finally {
					getItemList().getReadWriteLock().writeLock().unlock();
				}
			}

		});
		return result;
	}

	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.ACTION)
	public DeleteTask<Analysis> deleteAnalysis() {		
		DeleteTask<Analysis> result = null;
		int n = JOptionPane.showConfirmDialog(
				SwingUtilities.windowForComponent(this),
				getResourceMap().getString("deleteAnalysis.confirmDialog.text"),
				getResourceMap().getString("deleteAnalysis.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			final Client owningClient = getSelectedItem().getClient();
			result = new DeleteTask<Analysis>(getDaoService(), Analysis.class, getSelectedItem());
			result.addTaskListener(new TaskListener.Adapter<Analysis, Void>() {

				@Override
				public void succeeded(TaskEvent<Analysis> event) {
					Analysis analysis = event.getValue();
					getItemList().getReadWriteLock().writeLock().lock();
					try {
						int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
						getItemList().remove(analysis);
						owningClient.removeAnalysis(analysis);
						// delete the video files
						setSelectedItem(lastSelectedRowIndex - 1);
						getVideoFileManager().deleteVideoFiles(analysis);
					} finally {
						getItemList().getReadWriteLock().writeLock().unlock();
					}
				}

			});
		}
		return result;
	}

	@Action(enabledProperty = SELECTED_ITEM_RECORDED)
	public void showVideoFile() throws IOException {
		List<Recording> recordings = getSelectedItem().getRecordings();
		if (!recordings.isEmpty()) {
			Recording lastRecording = Iterables.getLast(recordings);
			File videoFile = getVideoFileManager().getVideoFile(lastRecording);
			if (AppHelper.getPlatform() == PlatformType.WINDOWS) {
				String[] commands = new String[] {"cmd.exe", "/c", "explorer /select," + videoFile.getAbsolutePath()};
				//String[] commands = new String[] {vlcPath, videoFile.getAbsolutePath(), " --rate=" + playRate};
				Runtime.getRuntime().exec(commands);
			}
			// TODO show the video file on different platforms?
		}
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
	public void setItemList(EventList<Analysis> itemList, ObservableElementList.Connector<? super Analysis> itemConnector) {
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

	private ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
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
