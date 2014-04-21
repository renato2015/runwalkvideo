package com.runwalk.video.panels;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
import org.jdesktop.application.Task;
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
import org.jdesktop.swingx.table.DatePickerCellEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;

import com.google.common.collect.Iterables;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.dao.jpa.ItemDao;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Analysis.Progression;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Item;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.model.ClientModel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.AbstractTask;
import com.runwalk.video.tasks.CompressVideoFilesTask;
import com.runwalk.video.tasks.CreateOrUpdateSuspendedSaleTask;
import com.runwalk.video.tasks.DeleteTask;
import com.runwalk.video.tasks.PersistTask;
import com.runwalk.video.ui.table.DatePickerTableCellRenderer;
import com.runwalk.video.ui.table.DateTableCellRenderer;
import com.runwalk.video.ui.table.JButtonTableCellRenderer;
import com.runwalk.video.ui.table.JComboBoxTableCellRenderer;
import com.runwalk.video.util.AppUtil;
import com.runwalk.video.util.BarcodeReader;

@SuppressWarnings("serial")
public class AnalysisTablePanel extends AbstractTablePanel<AnalysisModel> {

	public static final int WEEKS_AHEAD = 3;
	public static final String COMPRESSION_ENABLED = "compressionEnabled";
	public static final String COMPRESS_VIDEO_FILES_ACTION = "compressVideoFiles";
	private static final String DELETE_ANALYSIS_ACTION = "deleteAnalysis";
	private static final String ADD_ANALYSIS_ACTION = "addAnalysis";
	private static final String SELECTED_ITEM_RECORDED = "selectedItemRecorded";
	private static final String ADD_ANALYSIS_FOR_FEEDBACK_ENABLED = "addAnalysisForFeedbackEnabled";
	private static final String ADD_ANALYSIS_FOR_FEEDBACK_ACTION = "addAnalysisForFeedback";
	private static final String FIND_ITEM_BY_NUMBER_ACTION = "findItemByNumber";

	private final JTextArea comments;

	private final VideoFileManager videoFileManager;

	private final ClientTablePanel clientTablePanel;

	private final DaoService daoService;

	private final SettingsManager appSettings;

	private Boolean clientSelected = false;

	private boolean compressionEnabled;

	private EventList<Item> articleList = GlazedLists.eventListOf();

	private boolean selectedItemRecorded;

	private boolean addAnalysisForFeedbackEnabled;

	public AnalysisTablePanel(ClientTablePanel clientTablePanel,
			UndoableEditListener undoableEditListener, 
			SettingsManager appSettings, VideoFileManager videoFileManager,
			DaoService daoService) {
		super(new MigLayout("fill, nogrid"));
		this.clientTablePanel = clientTablePanel;
		this.appSettings = appSettings;
		this.videoFileManager = videoFileManager;
		this.daoService = daoService;

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow, height :100:");

		setFirstButton(new JButton(getAction(ADD_ANALYSIS_ACTION)));
		getFirstButton().setFont(SettingsManager.MAIN_FONT);
		add(getFirstButton());

		setSecondButton(new JButton(getAction(DELETE_ANALYSIS_ACTION)));
		getSecondButton().setFont(SettingsManager.MAIN_FONT);
		add(getSecondButton());

		JButton addAnalysisForFeedbackButton = new JButton(getAction(ADD_ANALYSIS_FOR_FEEDBACK_ACTION));
		addAnalysisForFeedbackButton.setFont(SettingsManager.MAIN_FONT);
		addAnalysisForFeedbackButton.setActionCommand(ADD_ANALYSIS_FOR_FEEDBACK_ACTION);
		add(addAnalysisForFeedbackButton);

		JButton compressAnalysisButton = new JButton(getAction(COMPRESS_VIDEO_FILES_ACTION));
		compressAnalysisButton.setFont(SettingsManager.MAIN_FONT);
		compressAnalysisButton.setActionCommand(ADD_ANALYSIS_FOR_FEEDBACK_ACTION);
		add(compressAnalysisButton, "gapleft push, wrap");

		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		comments.getDocument().addUndoableEditListener(undoableEditListener);
		comments.setFont(SettingsManager.MAIN_FONT);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		add(tscrollPane, "grow, height :60:");

		BindingGroup bindingGroup = new BindingGroup();
		// comments JTextArea binding
		BeanProperty<AnalysisTablePanel, String> selectedItemComments = BeanProperty.create("selectedItem.comments");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<? extends AbstractTablePanel<?>, String, JTextArea, String> commentsBinding = 
				Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this,	selectedItemComments, comments, jTextAreaValue);
		bindingGroup.addBinding(commentsBinding);

		BeanProperty<AnalysisTablePanel, Boolean> isSelected = BeanProperty.create(ROW_SELECTED);
		BeanProperty<JTextArea, Boolean> jTextAreaEnabled = BeanProperty.create("enabled");
		Binding<?, Boolean, JTextArea, Boolean> enableCommentsBinding = 
				Bindings.createAutoBinding(UpdateStrategy.READ, this, isSelected, comments, jTextAreaEnabled);
		enableCommentsBinding.setSourceNullValue(false);
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
		
		ELProperty<AnalysisTablePanel, Boolean> isAddFeedbackEnabled = ELProperty.create("${rowSelected && clientTablePanel.selectedItem.emailAddress != null}");
		BeanProperty<AnalysisTablePanel, Boolean> analysisSelected = BeanProperty.create(ADD_ANALYSIS_FOR_FEEDBACK_ENABLED);
		Binding<? extends AbstractTablePanel<?>, Boolean, AnalysisTablePanel, Boolean> addAnalysisForFeedbackBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ, this, isAddFeedbackEnabled, this, analysisSelected);
		addAnalysisForFeedbackBinding.setSourceUnreadableValue(false);
		addAnalysisForFeedbackBinding.setTargetNullValue(false);
		bindingGroup.addBinding(addAnalysisForFeedbackBinding);
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

	public boolean isAddAnalysisForFeedbackEnabled() {
		return addAnalysisForFeedbackEnabled;
	}

	public void setAddAnalysisForFeedbackEnabled(boolean addAnalysisForFeedbackEnabled) {
		firePropertyChange(ADD_ANALYSIS_FOR_FEEDBACK_ENABLED, this.addAnalysisForFeedbackEnabled, 
				this.addAnalysisForFeedbackEnabled = addAnalysisForFeedbackEnabled);
	}

	@Action(enabledProperty = ADD_ANALYSIS_FOR_FEEDBACK_ENABLED, block = BlockingScope.ACTION)
	public PersistTask<Analysis> addAnalysisForFeedback(ActionEvent event) {
		return addAnalysis(event);
	}

	private Date getDateForFeedback() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.WEEK_OF_MONTH, WEEKS_AHEAD);
		return calendar.getTime();
	}

	@Action(enabledProperty = CLIENT_SELECTED, block = BlockingScope.ACTION)
	public PersistTask<Analysis> addAnalysis(ActionEvent event) {
		// insert a new analysis record
		final ClientModel selectedModel = getClientTablePanel().getSelectedItem();
		final Client selectedClient = selectedModel.getEntity();
		if (("".equals(selectedModel.getName()) || selectedModel.getName() == null)
				&& ("".equals(selectedModel.getOrganization()) || selectedClient
						.getOrganization() == null)) {
			JOptionPane.showMessageDialog(SwingUtilities
					.windowForComponent(this),
					getResourceMap().getString("addAnalysis.errorDialog.text"),
					getResourceMap().getString("addAnalysis.Action.text"),
					JOptionPane.ERROR_MESSAGE);
			getLogger().warn("Attempt to insert analysis for " + selectedClient + " failed.");
			return null;
		}
		Analysis analysis = createAnalysisForEvent(event, selectedClient);
		PersistTask<Analysis> result = new PersistTask<Analysis>(getDaoService(), Analysis.class, analysis);
		result.addTaskListener(new TaskListener.Adapter<Analysis, Void>() {

			@Override
			public void succeeded(TaskEvent<Analysis> event) {
				Analysis result = event.getValue();
				getItemList().getReadWriteLock().writeLock().lock();
				try {
					AnalysisModel analysisModel = new AnalysisModel(selectedModel, result);
					selectedModel.addAnalysisModel(analysisModel);
					setSelectedItemRow(analysisModel);
				} finally {
					getItemList().getReadWriteLock().writeLock().unlock();
				}
			}

		});
		return result;
	}

	private Analysis createAnalysisForEvent(ActionEvent event, final Client selectedClient) {
		Analysis analysis;
		if (ADD_ANALYSIS_FOR_FEEDBACK_ACTION.equals(event.getActionCommand())) {
			analysis = new Analysis(selectedClient, getSelectedItem().getEntity(), getDateForFeedback());
		} else {
			analysis = new Analysis(selectedClient);
		}
		return analysis;
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
			// TODO check if analysis doesn't have feedback analaysis
			final AnalysisModel selectedModel = getSelectedItem();
			result = new DeleteTask<Analysis>(getDaoService(), Analysis.class,
					selectedModel.getEntity());
			result.addTaskListener(new TaskListener.Adapter<Analysis, Void>() {

				@Override
				public void succeeded(TaskEvent<Analysis> event) {
					Analysis analysis = event.getValue();
					getItemList().getReadWriteLock().writeLock().lock();
					try {
						int lastSelectedRowIndex = getEventSelectionModel()
								.getMinSelectionIndex();
						getItemList().remove(selectedModel);
						// FIXME selectedModel.removeAnalysisModel(selectedModel);
						// delete the video files
						if (lastSelectedRowIndex > 0) {
							setSelectedItemRow(lastSelectedRowIndex - 1);
						}
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
				String[] commands = new String[] { "cmd.exe", "/c",
						"explorer /select," + videoFile.getAbsolutePath() };
				// String[] commands = new String[] {vlcPath,
				// videoFile.getAbsolutePath(), " --rate=" + playRate};
				Runtime.getRuntime().exec(commands);
			}
			// TODO show the video file on different platforms?
		}
	}

	@Action(enabledProperty = COMPRESSION_ENABLED, block = Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> compressVideoFiles() {
		setCompressionEnabled(false);
		String transcoder = getAppSettings().getTranscoderName();
		Window parentComponent = SwingUtilities.windowForComponent(this);
		return new CompressVideoFilesTask(parentComponent, getVideoFileManager(), 
				getSelectedItem().getRecordings(), transcoder);
	}

	@Action
	public Task<Item, Void> findItemByNumber(final ActionEvent event) {
		return new AbstractTask<Item, Void>(FIND_ITEM_BY_NUMBER_ACTION) {

			@Override
			protected Item doInBackground() throws Exception {
				String itemNumber = null;
				if ("comboBoxEdited".equals(event.getActionCommand())) {
					@SuppressWarnings("unchecked")
					JComboBox<Item> itemComboBox = (JComboBox<Item>) event.getSource();
					Object item = itemComboBox.getEditor().getItem();
					// TODO check if selection changed??
					Item selectedItem = (Item) itemComboBox.getSelectedItem();
					if (item != null && !selectedItem.getItemNumber().equals(item.toString())) {
						itemNumber = item.toString();
					}
				} else if ("barcodeReceived".equals(event.getActionCommand())) {
					BarcodeReader barcodeReader = (BarcodeReader) event.getSource();
					itemNumber = barcodeReader.getBarcode();
				}
				if (itemNumber != null) {
					ItemDao itemDao = daoService.getDao(Item.class);
					return itemDao.getItemByItemNumber(itemNumber);
				}
				return null;
			}
			
			@Override
			protected void succeeded(Item result) {
				try {
					getArticleList().getReadWriteLock().writeLock().lock();
					if (result != null) {
						if (!getArticleList().contains(result)) {
							getArticleList().add(result);
						}
						getSelectedItem().setItem(result);
						Client selectedClient = clientTablePanel.getSelectedItem().getEntity();
						AnalysisTablePanel.this.getTaskService().execute(new CreateOrUpdateSuspendedSaleTask(getDaoService(), 
								selectedClient, result));
					}
				} finally {
					getArticleList().getReadWriteLock().writeLock().unlock();
				}
			}

		};
	}

	public EventList<Item> getArticleList() {
		return articleList;
	}

	public void initialiseTableColumnModel() {
		getTable().getColumnModel().getColumn(0).setMinWidth(60);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		DateTableCellRenderer cellRenderer = new DatePickerTableCellRenderer(AppUtil.DATE_FORMATTER, AppUtil.EXTENDED_DATE_FORMATTER);
		cellRenderer.setFont(SettingsManager.MAIN_FONT);
		getTable().getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		DatePickerCellEditor datePickerCellEditor = new DatePickerCellEditor(AppUtil.DATE_FORMATTER);
		getTable().getColumnModel().getColumn(0).setCellEditor(datePickerCellEditor);

		BarcodeReader reader = new BarcodeReader();
		reader.addActionListener(getAction(FIND_ITEM_BY_NUMBER_ACTION));
		// create special table cell editor for selecting articles
		AutoCompleteCellEditor<Item> createTableCellEditor = AutoCompleteSupport.createTableCellEditor(getArticleList());
		createTableCellEditor.getAutoCompleteSupport().getComboBox().addActionListener(getAction(FIND_ITEM_BY_NUMBER_ACTION));
		createTableCellEditor.setClickCountToStart(0);
		createTableCellEditor.getComponent().setFont(SettingsManager.MAIN_FONT);
		getTable().getColumnModel().getColumn(1).setCellEditor(createTableCellEditor);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);
		getTable().getColumnModel().getColumn(2).setPreferredWidth(18);

		EventList<Progression> progression = GlazedLists.eventListOf(Progression.values());
		AutoCompleteCellEditor<Progression> progressionTableCellEditor = AutoCompleteSupport.createTableCellEditor(progression);
		progressionTableCellEditor.getAutoCompleteSupport().getComboBox().setEditable(false);
		progressionTableCellEditor.getAutoCompleteSupport().getComboBox().setFont(SettingsManager.MAIN_FONT);
		progressionTableCellEditor.getAutoCompleteSupport().setStrict(true);
		progressionTableCellEditor.getAutoCompleteSupport().setBeepOnStrictViolation(false);
		progressionTableCellEditor.getAutoCompleteSupport().setFirstItem(null);
		progressionTableCellEditor.setClickCountToStart(1);
		JComboBoxTableCellRenderer comboBoxTableCellRenderer = new JComboBoxTableCellRenderer();
		getTable().getColumnModel().getColumn(2).setCellRenderer(comboBoxTableCellRenderer);
		comboBoxTableCellRenderer.setFont(SettingsManager.MAIN_FONT);
		getTable().getColumnModel().getColumn(2).setCellEditor(progressionTableCellEditor);

		getTable().getColumnModel().getColumn(3).setPreferredWidth(18);
		String defaultValue = getResourceMap().getString("tableFormat.defaultValue");
		getTable().getColumnModel().getColumn(3).setCellRenderer(new DateTableCellRenderer(defaultValue, AppUtil.DURATION_FORMATTER));
		getTable().getColumnModel().getColumn(5).setPreferredWidth(40);
		getTable().getColumnModel().getColumn(5).setResizable(false);
		final String buttonTitle = getResourceMap().getString("analysisModelTableFormat.openButton.text");
		getTable().getColumnModel().getColumn(5).setCellRenderer(new JButtonTableCellRenderer(buttonTitle));
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		// compression only works on windows
		boolean isWindows = AppHelper.getPlatform() == PlatformType.WINDOWS;
		firePropertyChange(COMPRESSION_ENABLED, this.compressionEnabled, this.compressionEnabled = compressionEnabled && isWindows);
	}

	public boolean isClientSelected() {
		return clientSelected;
	}

	public void setClientSelected(boolean clientSelected) {
		this.firePropertyChange(CLIENT_SELECTED, this.clientSelected, this.clientSelected = clientSelected);
	}

	public ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}

	public SettingsManager getAppSettings() {
		return appSettings;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	private DaoService getDaoService() {
		return daoService;
	}

}
