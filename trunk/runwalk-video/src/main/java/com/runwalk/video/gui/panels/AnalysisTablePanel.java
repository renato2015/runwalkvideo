package com.runwalk.video.gui.panels;

import javax.persistence.Query;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jdesktop.application.Action;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.beans.PropertyAdapter;
import com.jgoodies.binding.beans.PropertyConnector;
import com.jgoodies.binding.value.ValueModel;
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
		super(new AbsoluteLayout());

		JScrollPane analysisTableScrollPanel = new  JScrollPane();
		analysisTableScrollPanel.setViewportView(getTable());

		add(analysisTableScrollPanel, new AbsoluteConstraints(10, 20, 550, 100));

		JPanel buttonPanel =  new JPanel();
		buttonPanel.setLayout(new AbsoluteLayout());

		setSecondButton(new JButton(getAction("addAnalysis")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(getSecondButton(), new AbsoluteConstraints(10, 0, -1, -1));

		setFirstButton(new JButton(getAction("deleteAnalysis")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(getFirstButton(), new AbsoluteConstraints(130, 0, -1, -1));

		add(buttonPanel, new AbsoluteConstraints(0, 130, -1, 30));

		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		comments.getDocument().addUndoableEditListener(getApplication().getApplicationActions().getUndoableEditListener());
		comments.setFont(AppSettings.MAIN_FONT);
		comments.setColumns(20);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		add(tscrollPane, new AbsoluteConstraints(10, 165, 550, 60));

		BindingGroup bindingGroup = new BindingGroup();
		//comments JTextArea binding
		BeanProperty<AnalysisTablePanel, String> selectedItemComments = BeanProperty.create("selectedItem.comments");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<? extends AbstractTablePanel<?> , String, JTextArea, String> commentsBinding = 
			Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, selectedItemComments, comments, jTextAreaValue);
		bindingGroup.addBinding(commentsBinding);
		
//		PropertyAdapter commentsBean = new PropertyAdapter(this, "selectedItem.comments", true);
//		PropertyAdapter propertyAdapter = new PropertyAdapter(this, "selectedItem");
//		PresentationModel model = new PresentationModel(this);
//		ValueModel selectedItemModel =  model.getModel("selectedItem");
//		PropertyConnector.connect(selectedItemModel, "value", comments, "text");
		

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
	public void setItemList(EventList<Analysis> itemList, Class<Analysis> itemClass) {
		super.setItemList(itemList, itemClass);
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
