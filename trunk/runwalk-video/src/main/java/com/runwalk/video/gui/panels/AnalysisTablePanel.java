package com.runwalk.video.gui.panels;

import javax.persistence.Query;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Article;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.DateTableCellRenderer;
import com.runwalk.video.gui.OpenRecordingButton;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class AnalysisTablePanel extends AbstractTablePanel<Analysis> {

	private static final String CLIENT_SELECTED = "clientSelected";

	private JTextArea comments;

	private Boolean clientSelected = false;

	public AnalysisTablePanel() {
		super(new MigLayout("fill, nogrid"));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow, width :570:, height :100:");

		setSecondButton(new JButton(getAction("addAnalysis")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());

		setFirstButton(new JButton(getAction("deleteAnalysis")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		add(getFirstButton(), "wrap");

		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		comments.getDocument().addUndoableEditListener(getApplication().getApplicationActions().getUndoableEditListener());
		comments.setFont(AppSettings.MAIN_FONT);
		comments.setColumns(20);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		add(tscrollPane, "grow, width :570:, height :60:");

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
			Bindings.createAutoBinding(UpdateStrategy.READ, getApplication().getClientTablePanel(), isClientSelected, this, clientSelected);
		clientSelectedBinding.setSourceNullValue(false);
		clientSelectedBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(clientSelectedBinding);
		bindingGroup.bind();
	}

	public void clearComments() {
		comments.setText("");
	}

	@Action(enabledProperty = CLIENT_SELECTED)
	public void addAnalysis() {
		//insert a new analysis record
		final Client selectedClient = RunwalkVideoApp.getApplication().getSelectedClient();
		if (selectedClient.getName() == null && selectedClient.getOrganization() == null) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
					"Voer eerst een naam in voor deze klant!", 
					"Fout aanmaken analyse", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		getItemList().getReadWriteLock().writeLock().lock();
		Analysis analysis = new Analysis(selectedClient);
		Recording recording = new Recording(analysis);
		analysis.setRecording(recording);
		AppUtil.persistEntity(analysis);
		try {
			selectedClient.addAnalysis(analysis);
			setSelectedItem(analysis);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
		getApplication().getMediaControls().captureFrameToFront();
	}

	@Action(enabledProperty = ROW_SELECTED)
	public void deleteAnalysis() {		
		int n = JOptionPane.showConfirmDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
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
			getApplication().getSelectedClient().removeAnalysis(selectedAnalysis);
			setSelectedItem(lastSelectedRowIndex - 1);
			AppUtil.deleteEntity(selectedAnalysis);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
		//TODO kan je deze properties niet binden?? eventueel met een listener.. 
		getApplication().getAnalysisOverviewTable().setCompressionEnabled(true);
	}

	@Override
	protected EventList<Analysis> specializeItemList(EventList<Analysis> eventList) {
		eventList.addListEventListener(new ListEventListener<Analysis>() {

			public void listChanged(ListEvent<Analysis> listChanges) {
				while (listChanges.next()) {
		            final int changeIndex = listChanges.getIndex();
		            final int changeType = listChanges.getType();
		            if (changeType == ListEvent.UPDATE) {
		            	getApplication().setSaveNeeded(true);
		            	Analysis changedItem = listChanges.getSourceList().get(changeIndex);
						changedItem.getClient().setDirty(true);
		            }
				}
			}
		});
		return eventList;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setItemList(EventList<Analysis> itemList, ObservableElementList.Connector<Analysis> itemConnector) {
		super.setItemList(itemList, itemConnector);
		getTable().getColumnModel().getColumn(0).setMinWidth(70);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		Query query = RunwalkVideoApp.getApplication().createQuery("SELECT OBJECT(ar) from Article ar"); // NOI18N
		AutoCompleteCellEditor<Article> createTableCellEditor = AutoCompleteSupport.createTableCellEditor(GlazedLists.eventList(query.getResultList()));
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

	public TableFormat<Analysis> getTableFormat() {
		return new AnalysisTableFormat();
	}

	public class AnalysisTableFormat implements WritableTableFormat<Analysis> {

		public int getColumnCount() {
			return 5;
		}

		public String getColumnName(int column) {
			if(column == 0)      return "Datum";
			else if(column == 1) return "Gekozen schoen";
			else if(column == 2) return "Aantal keyframes";
			else if(column == 3) return "Duur video";
			else if(column == 4) return "Open video";
			throw new IllegalStateException();
		}

		public Object getColumnValue(Analysis analysis, int column) {
			if(column == 0) {
				return analysis.getCreationDate();
			}
			else if(column == 1) return analysis.getArticle();
			else if(column == 2) {
				return analysis.getRecording() != null ? analysis.getRecording().getKeyframeCount() : 0;
			}
			else if(column == 3) return analysis.getRecording() != null ? analysis.getRecording().getDuration() : 0L;
			else if(column == 4) return new OpenRecordingButton(analysis.getRecording());
			throw new IllegalStateException();
		}

		public boolean isEditable(Analysis baseObject, int column) {
			return column == 1;
		}

		public Analysis setColumnValue(Analysis analysis, Object editedValue, int column) {
			if (editedValue instanceof Article) {
				analysis.setArticle((Article) editedValue);
			}
			return analysis;
		}

	}

}
