package com.runwalk.video.panels;

import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditListener;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingx.table.DatePickerCellEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;
import ca.odell.glazedlists.swing.TreeTableSupport;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.RedcordExercise;
import com.runwalk.video.entities.RedcordSession;
import com.runwalk.video.entities.RedcordTableElement;
import com.runwalk.video.entities.RedcordTableElement.ExerciseDirection;
import com.runwalk.video.entities.RedcordTableElement.ExerciseType;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.DeleteTask;
import com.runwalk.video.tasks.PersistTask;
import com.runwalk.video.ui.table.DatePickerTableCellRenderer;
import com.runwalk.video.ui.table.JComboBoxTableCellRenderer;
import com.runwalk.video.ui.table.JSpinnerTableCellEditor;
import com.runwalk.video.ui.table.JSpinnerTableCellRenderer;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class RedcordTablePanel extends AbstractTablePanel<RedcordTableElement> {

	private static final String REDCORD_SESSION_SELECTED = "redcordSessionSelected";

	private static final String REDCORD_EXERCISE_SELECTED = "redcordExerciseSelected";

	private final ClientTablePanel clientTablePanel;
	
	private final DaoService daoService;

	private JTextArea comments;
	
	private Boolean clientSelected = false;
	
	private Boolean redcordSessionSelected = false;
	
	private Boolean redcordExerciseSelected = false;
	
	/**
	 * Create the panel.
	 */
	public RedcordTablePanel(ClientTablePanel clientTablePanel, UndoableEditListener undoableEditListener, 
			DaoService daoService) {
		super(new MigLayout("fill, nogrid"));
		this.clientTablePanel = clientTablePanel;
		this.daoService = daoService;
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow, height :130:");
		
		setFirstButton(new JButton(getAction("addRedcordSession")));
		getFirstButton().setFont(SettingsManager.MAIN_FONT);
		add(getFirstButton());
		
		setSecondButton(new JButton(getAction("deleteRedcordSession")));
		getSecondButton().setFont(SettingsManager.MAIN_FONT);
		add(getSecondButton());
		
		JButton thirdButton = new JButton(getAction("addRedcordExercise"));
		thirdButton.setFont(SettingsManager.MAIN_FONT);
		add(thirdButton);
		
		JButton fourthButton = new JButton(getAction("deleteRedcordExercise"));
		fourthButton.setFont(SettingsManager.MAIN_FONT);
		add(fourthButton, "wrap");
		
		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		comments.getDocument().addUndoableEditListener(undoableEditListener);
		comments.setFont(SettingsManager.MAIN_FONT);
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
		
		ELProperty<RedcordTablePanel, Boolean> isRedcordSessionSelected = ELProperty.create("${rowSelected && selectedItem.class.simpleName == 'RedcordSession'}");
		BeanProperty<RedcordTablePanel, Boolean> redcordSessionSelected = BeanProperty.create(REDCORD_SESSION_SELECTED);
		Binding<? extends AbstractTablePanel<?>, Boolean, RedcordTablePanel, Boolean> selectedRedcordSessionBinding = 
				Bindings.createAutoBinding(UpdateStrategy.READ, this, isRedcordSessionSelected, this, redcordSessionSelected);
		selectedRedcordSessionBinding.setSourceUnreadableValue(false);
		selectedRedcordSessionBinding.setTargetNullValue(false);
		bindingGroup.addBinding(selectedRedcordSessionBinding);
		
		ELProperty<RedcordTablePanel, Boolean> isRedcordExerciseSelected = ELProperty.create("${rowSelected && selectedItem.class.simpleName == 'RedcordExercise'}");
		BeanProperty<RedcordTablePanel, Boolean> redcordExerciseSelected = BeanProperty.create(REDCORD_EXERCISE_SELECTED);
		Binding<? extends AbstractTablePanel<?>, Boolean, RedcordTablePanel, Boolean> selectedRedcordExerciseBinding = 
				Bindings.createAutoBinding(UpdateStrategy.READ, this, isRedcordExerciseSelected, this, redcordExerciseSelected);
		selectedRedcordExerciseBinding.setSourceUnreadableValue(false);
		selectedRedcordExerciseBinding.setTargetNullValue(false);
		bindingGroup.addBinding(selectedRedcordExerciseBinding);
		
		bindingGroup.bind();
		
	}
	
	@Action(enabledProperty = CLIENT_SELECTED, block = BlockingScope.ACTION)
	public PersistTask<RedcordSession> addRedcordSession() {
		// insert a new session record
		final Client selectedClient = getClientTablePanel().getSelectedItem();
		if (("".equals(selectedClient.getName())  || selectedClient.getName() == null) && 
			("".equals(selectedClient.getOrganization()) || selectedClient.getOrganization() == null)) {
			JOptionPane.showMessageDialog(
					SwingUtilities.windowForComponent(this), 
					getResourceMap().getString("addRedcordSession.errorDialog.text"),
					getResourceMap().getString("addRedcordSession.Action.text"), 
					JOptionPane.ERROR_MESSAGE);
			getLogger().warn("Attempt to insert redcordSession for " + selectedClient + " failed.");
			return null;
		}
		// get session count and set name
		int sessionCount = selectedClient.getRedcordSessionCount();
		String sessionName = getResourceMap().getString("addRedcordSession.Action.defaultSessionName", ++sessionCount);
		RedcordSession redcordSession = new RedcordSession(selectedClient, sessionName); 
		PersistTask<RedcordSession> result = new PersistTask<RedcordSession>(getDaoService(), RedcordSession.class, redcordSession);
		result.addTaskListener(new TaskListener.Adapter<RedcordSession, Void>() {

			@Override
			public void succeeded(TaskEvent<RedcordSession> event) {
				RedcordSession result = event.getValue();
				getItemList().getReadWriteLock().writeLock().lock();
				try {
					selectedClient.addRedcordSession(result);
					setSelectedItemRow(result);
				} finally {
					getItemList().getReadWriteLock().writeLock().unlock();
				}
			}

		});
		return result;
	}
	
	@Action(enabledProperty = REDCORD_SESSION_SELECTED, block = BlockingScope.ACTION)
	public DeleteTask<RedcordSession> deleteRedcordSession() {		
		DeleteTask<RedcordSession> result = null;
		int n = JOptionPane.showConfirmDialog(
				SwingUtilities.windowForComponent(this),
				getResourceMap().getString("deleteRedcordSession.confirmDialog.text"),
				getResourceMap().getString("deleteRedcordSession.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			final Client selectedClient = getClientTablePanel().getSelectedItem();
			result = new DeleteTask<RedcordSession>(getDaoService(), RedcordSession.class, (RedcordSession) getSelectedItem());
			result.addTaskListener(new TaskListener.Adapter<RedcordSession, Void>() {

				@Override
				public void succeeded(TaskEvent<RedcordSession> event) {
					RedcordTableElement redcordSession = event.getValue();
					getItemList().getReadWriteLock().writeLock().lock();
					try {
						int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
						selectedClient.removeRedcordSession(redcordSession);
						getItemList().remove(redcordSession);
						// set selection on previous item
						setSelectedItemRow(lastSelectedRowIndex - 1);
					} finally {
						getItemList().getReadWriteLock().writeLock().unlock();
					}
				}

			});
		}
		return result;
	}
	
	@Action(enabledProperty = ROW_SELECTED, block = BlockingScope.ACTION)
	public PersistTask<RedcordExercise> addRedcordExercise() {
		// insert a new exercise record
		RedcordSession selectedRedcordSession = null;
		if (getSelectedItem() instanceof RedcordSession) {
			selectedRedcordSession = (RedcordSession) getSelectedItem();
		} else {
			selectedRedcordSession = ((RedcordExercise) getSelectedItem()).getRedcordSession();
		}
		final RedcordSession finaSelectedRedcordSession = selectedRedcordSession;
		// get exercise count and set name
		int exerciseCount = selectedRedcordSession.getRedcordExerciseCount();
		String exerciseName = getResourceMap().getString("addRedcordExercise.Action.defaultExerciseName", ++exerciseCount);
		RedcordExercise redcordExercise = new RedcordExercise(selectedRedcordSession, exerciseName);
		PersistTask<RedcordExercise> result = new PersistTask<RedcordExercise>(getDaoService(), RedcordExercise.class, redcordExercise);
		result.addTaskListener(new TaskListener.Adapter<RedcordExercise, Void>() {

			@Override
			public void succeeded(TaskEvent<RedcordExercise> event) {
				RedcordExercise result = event.getValue();
				getItemList().getReadWriteLock().writeLock().lock();
				try {
					finaSelectedRedcordSession.addRedcordExercise(result);
					//getItemList().add(result);
					setSelectedItemRow(result);
				} finally {
					getItemList().getReadWriteLock().writeLock().unlock();
				}
			}

		});
		return result;
	}
	
	@Action(enabledProperty = REDCORD_EXERCISE_SELECTED, block = BlockingScope.ACTION)
	public DeleteTask<RedcordExercise> deleteRedcordExercise() {		
		DeleteTask<RedcordExercise> result = null;
		int n = JOptionPane.showConfirmDialog(
				SwingUtilities.windowForComponent(this),
				getResourceMap().getString("deleteRedcordExercise.confirmDialog.text"),
				getResourceMap().getString("deleteRedcordExercise.Action.text"),
				JOptionPane.WARNING_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			RedcordExercise selectedRedcordExercise = (RedcordExercise) getSelectedItem();
			final RedcordSession owningRedcordSession = selectedRedcordExercise.getRedcordSession();
			result = new DeleteTask<RedcordExercise>(getDaoService(), RedcordExercise.class, selectedRedcordExercise);
			result.addTaskListener(new TaskListener.Adapter<RedcordExercise, Void>() {

				@Override
				public void succeeded(TaskEvent<RedcordExercise> event) {
					RedcordTableElement redcordExercise = event.getValue();
					getItemList().getReadWriteLock().writeLock().lock();
					try {
						int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
						getItemList().remove(redcordExercise);
						owningRedcordSession.removeRedcordExercise(redcordExercise);
						// set selection on previous item
						setSelectedItemRow(lastSelectedRowIndex - 1);
					} finally {
						getItemList().getReadWriteLock().writeLock().unlock();
					}
				}

			});
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected TreeList<RedcordTableElement> specializeItemList(EventList<RedcordTableElement> eventList) {
		
		final Comparator<RedcordTableElement> redcordTableElementComparator = GlazedLists.chainComparators(
	            GlazedLists.beanPropertyComparator(RedcordTableElement.class, "name")
	    );
		
		TreeList.Format<RedcordTableElement> listFormat = new TreeList.Format<RedcordTableElement>() {

			public void getPath(List<RedcordTableElement> paramList, RedcordTableElement redcordTableElement) {
				if (!redcordTableElement.allowsChildren()) {
					RedcordExercise redcordExercise = (RedcordExercise) redcordTableElement;
					// always add the session up front
					paramList.add(redcordExercise.getRedcordSession());
					paramList.add(redcordTableElement);
				} else {
					paramList.add(redcordTableElement);
				}
			}

			public boolean allowsChildren(RedcordTableElement redcordTableElement) {
				return redcordTableElement.allowsChildren();
			}

			public Comparator<RedcordTableElement> getComparator(int paramInt) {
				return redcordTableElementComparator;
			}
			
		};
		// workaround to set selection back after a TreeTable update
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
	
	public void initialiseTableColumnModel() {
		getTable().setRowHeight(20);
		JComboBoxTableCellRenderer comboBoxTableCellRenderer = new JComboBoxTableCellRenderer();
		// name of the session / exercise
		getTable().getColumnModel().getColumn(0).setMinWidth(70);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		// date of the session
		DatePickerCellEditor datePickerCellEditor = new DatePickerCellEditor(AppUtil.DATE_FORMATTER);
		datePickerCellEditor.setClickCountToStart(1);
		getTable().getColumnModel().getColumn(1).setCellEditor(datePickerCellEditor);
		getTable().getColumnModel().getColumn(1).setCellRenderer(new DatePickerTableCellRenderer(AppUtil.DATE_FORMATTER));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(70);
		
		SpinnerDateModel spinnerDateModel = new SpinnerDateModel();
		JSpinner spinner = new JSpinner(spinnerDateModel);
		spinner.setEditor(new JSpinner.DateEditor(spinner, AppUtil.HOUR_MINUTE_FORMATTER.toPattern()));
		getTable().getColumnModel().getColumn(2).setCellRenderer(JSpinnerTableCellRenderer.dateTableCellRenderer(AppUtil.HOUR_MINUTE_FORMATTER));
		getTable().getColumnModel().getColumn(2).setCellEditor(new JSpinnerTableCellEditor(spinner));
		getTable().getColumnModel().getColumn(2).setPreferredWidth(30);
		
		// create special table cell editor for selecting exercise type
		EventList<ExerciseType> exerciseTypes = GlazedLists.eventListOf(ExerciseType.values());
		AutoCompleteCellEditor<ExerciseType> exerciseTypeTableCellEditor = AutoCompleteSupport.createTableCellEditor(exerciseTypes);
		exerciseTypeTableCellEditor.getAutoCompleteSupport().getComboBox().setEditable(false);
		exerciseTypeTableCellEditor.getAutoCompleteSupport().setStrict(true);
		exerciseTypeTableCellEditor.getAutoCompleteSupport().setFirstItem(null);
		exerciseTypeTableCellEditor.getAutoCompleteSupport().setBeepOnStrictViolation(false);
		exerciseTypeTableCellEditor.setClickCountToStart(1);
		getTable().getColumnModel().getColumn(3).setCellRenderer(comboBoxTableCellRenderer);
		getTable().getColumnModel().getColumn(3).setCellEditor(exerciseTypeTableCellEditor);
		getTable().getColumnModel().getColumn(3).setPreferredWidth(50);
		
		EventList<ExerciseDirection> exerciseDirections = GlazedLists.eventListOf(ExerciseDirection.values());
		AutoCompleteCellEditor<ExerciseDirection> exerciseDirectionTableCellEditor = AutoCompleteSupport.createTableCellEditor(exerciseDirections);
		exerciseDirectionTableCellEditor.getAutoCompleteSupport().getComboBox().setEditable(false);
		exerciseDirectionTableCellEditor.getAutoCompleteSupport().setStrict(true);
		exerciseDirectionTableCellEditor.getAutoCompleteSupport().setBeepOnStrictViolation(false);
		exerciseDirectionTableCellEditor.getAutoCompleteSupport().setFirstItem(null);
		exerciseDirectionTableCellEditor.setClickCountToStart(1);
		getTable().getColumnModel().getColumn(4).setCellRenderer(comboBoxTableCellRenderer);
		getTable().getColumnModel().getColumn(4).setCellEditor(exerciseDirectionTableCellEditor);
		getTable().getColumnModel().getColumn(4).setResizable(false);
		getTable().getColumnModel().getColumn(4).setPreferredWidth(50);
		// install tree table support on the first column of the table
		TreeTableSupport.install(getTable(), getItemList(), 0);
		// workaround for issue #GLAZEDLISTS-462
		getEventSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				if (isRowSelected() && getSelectedItem() != null &&
							getEventSelectionModel().getSelected().isEmpty()) {
						setSelectedItemRow(getSelectedItem()); 
				}
			}
			
		});
	}
	
	@Override
	public boolean save() {
		// TODO implement syncing code here
		return super.save();
	}

	public boolean isClientSelected() {
		return clientSelected;
	}

	public void setClientSelected(boolean clientSelected) {
		firePropertyChange(CLIENT_SELECTED, this.clientSelected, this.clientSelected = clientSelected);
	}
	
	public Boolean getRedcordSessionSelected() {
		return redcordSessionSelected;
	}

	public void setRedcordSessionSelected(Boolean redcordSessionSelected) {
		firePropertyChange(REDCORD_SESSION_SELECTED, this.redcordSessionSelected, this.redcordSessionSelected = redcordSessionSelected);
	}
	
	public Boolean getRedcordExerciseSelected() {
		return redcordExerciseSelected;
	}

	public void setRedcordExerciseSelected(Boolean redcordExerciseSelected) {
		firePropertyChange(REDCORD_EXERCISE_SELECTED, this.redcordExerciseSelected, this.redcordExerciseSelected = redcordExerciseSelected);
	}

	public ClientTablePanel getClientTablePanel() {
		return clientTablePanel;
	}
	
	public DaoService getDaoService() {
		return daoService;
	}


}
