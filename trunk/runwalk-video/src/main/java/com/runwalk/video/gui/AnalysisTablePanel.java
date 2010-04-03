package com.runwalk.video.gui;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import org.jdesktop.application.Action;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Articles;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class AnalysisTablePanel extends AbstractTablePanel<Analysis> {

	private static final String CLIENT_SELECTED = "clientSelected";

	private JTextArea comments;

	private Boolean clientSelected = false;

	@SuppressWarnings("unchecked")
	public AnalysisTablePanel() {
		super(new AbsoluteLayout());

		JScrollPane analysisTableScrollPanel = new  JScrollPane();
		analysisTableScrollPanel.setViewportView(getTable());
		BindingGroup bindingGroup = new BindingGroup();

		//analyses view binding
		ELProperty<JTable, List<Analysis>> analyses = ELProperty.create("${selectedElement.analyses}");
		jTableSelectionBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, RunwalkVideoApp.getApplication().getClientTable(), analyses, getTable());

		//timestamp binding
		BeanProperty<Analysis, Date> timestamp = BeanProperty.create("creationDate");
		JTableBinding<Analysis, ?, JTable>.ColumnBinding columnBinding = jTableSelectionBinding.addColumnBinding(timestamp);
		columnBinding.setColumnName("Tijdstip analyse");
		columnBinding.setColumnClass(String.class);
		columnBinding.setEditable(false);
		columnBinding.setConverter(new Converter<Date, String>() {

			@Override
			public String convertForward(Date arg0) {
				return AppUtil.formatDate(arg0, AppUtil.EXTENDED_DATE_FORMATTER);
			}

			@Override
			public Date convertReverse(String arg0) {
				return null;
			}

		});

		//article binding model
		Query query = RunwalkVideoApp.getApplication().createQuery("SELECT OBJECT(ar) from Articles ar"); // NOI18N
		ObservableList<Articles> articleList = ObservableCollections.observableList(query.getResultList());

		JComboBox shoes = new JComboBox();
		shoes.setFont(AppSettings.MAIN_FONT);
		JComboBoxBinding<Articles, List<Articles>, JComboBox> cb = SwingBindings.createJComboBoxBinding(AutoBinding.UpdateStrategy.READ, articleList, shoes);
		cb.bind();
		
		AbstractBindingListener changeListener = new ClientBindingListener();
		
		//article binding
		BeanProperty<Analysis, Articles> article = BeanProperty.create("article");
		columnBinding = jTableSelectionBinding.addColumnBinding(article);
		columnBinding.setColumnName("Gekozen artikel");
		columnBinding.setColumnClass(Articles.class);
		columnBinding.setEditor(new DefaultCellEditor(shoes));
		columnBinding.addBindingListener(changeListener);

		//recording keyframe count binding
		ELProperty<Analysis, Integer> recordingKeyframeCount = ELProperty.create("${recording.keyframeCount}");
		columnBinding = jTableSelectionBinding.addColumnBinding(recordingKeyframeCount);
		columnBinding.setColumnName("Aantal keyframes");
		columnBinding.setColumnClass(Integer.class);
		columnBinding.setEditable(false);
		columnBinding.addBindingListener(changeListener);

		//recording duration binding
		ELProperty<Analysis, Long> recordingDuration = ELProperty.create("${recording.duration}");
		columnBinding = jTableSelectionBinding.addColumnBinding(recordingDuration);
		columnBinding.setColumnName("Duur video");
		columnBinding.setColumnClass(Long.class);
		columnBinding.setEditable(false);
		columnBinding.addBindingListener(changeListener);
		columnBinding.setConverter(new Converter<Long, String>() {

			@Override
			public String convertForward(Long duration) {
				return duration == null ? "<geen>" : AppUtil.formatDate(new Date(duration), AppUtil.DURATION_FORMATTER);
			}

			@Override
			public Long convertReverse(String arg0) {
				return null;
			}

		});

		BeanProperty<Analysis, OpenRecordingButton> openButton = BeanProperty.create("recording");
		columnBinding = jTableSelectionBinding.addColumnBinding(openButton);
		columnBinding.setRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		columnBinding.setEditable(false);
		columnBinding.setColumnName("");
		columnBinding.setColumnClass(JButton.class);
		columnBinding.setConverter(new Converter<Recording, JButton>() {

			@Override
			public JButton convertForward(final Recording recording) {
				return new OpenRecordingButton(recording);
			}

			@Override
			public Recording convertReverse(JButton button) {
				return null;
			}

		});

		jTableSelectionBinding.setSourceNullValue(Collections.emptyList());
		jTableSelectionBinding.setSourceUnreadableValue(Collections.emptyList());
		bindingGroup.addBinding(jTableSelectionBinding);
		jTableSelectionBinding.bind();

		getTable().getColumnModel().getColumn(0).setMinWidth(70);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		//		getTable().getColumnModel().getColumn(2).setMaxWidth(50);
		//		JFormattedTextField dateField = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
		//		getTable().getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(dateField));

		getTable().getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(shoes));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);

		getTable().getColumnModel().getColumn(3).setPreferredWidth(18);
		getTable().getColumnModel().getColumn(3).setResizable(false);

		getTable().getColumnModel().getColumn(4).setPreferredWidth(5);
		getTable().getColumnModel().getColumn(4).setResizable(false);
		//		getTable().getColumnModel().getColumn(4).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		getTable().addMouseListener(new JTableButtonMouseListener());
		add(analysisTableScrollPanel, new AbsoluteConstraints(10, 20, 550, 100));

		JPanel buttonPanel =  new JPanel();
		buttonPanel.setLayout(new AbsoluteLayout());

		setSecondButton(new JButton(RunwalkVideoApp.getApplication().getContext().getActionMap(this).get("addAnalysis")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(getSecondButton(), new AbsoluteConstraints(10, 0, -1, -1));

		setFirstButton(new JButton(RunwalkVideoApp.getApplication().getContext().getActionMap(this).get("deleteAnalysis")));
		getFirstButton().setFont(AppSettings.MAIN_FONT);
		buttonPanel.add(getFirstButton(), new AbsoluteConstraints(130, 0, -1, -1));

		add(buttonPanel, new AbsoluteConstraints(0, 130, -1, 30));

		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		comments.getDocument().addUndoableEditListener(RunwalkVideoApp.getApplication().getApplicationActions().getUndoableEditListener());
		comments.setFont(AppSettings.MAIN_FONT);
		comments.setColumns(20);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		add(tscrollPane, new AbsoluteConstraints(10, 165, 550, 60));

		//comments JTextArea binding
		ELProperty<JTable, String> selectedElementComments = ELProperty.create("${selectedElement.comments}");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<JTable, String, JTextArea, String> commentsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, getTable(), 
				selectedElementComments, comments, jTextAreaValue, "commentsBinding");
		commentsBinding.addBindingListener(changeListener);
		bindingGroup.addBinding(commentsBinding);

		ELProperty<JTable, Boolean> isSelected = ELProperty.create("${selectedElement != null}");
		BeanProperty<JTextArea, Boolean> jTextAreaEnabled = BeanProperty.create("enabled");
		Binding<JTable, Boolean, JTextArea, Boolean> enableCommentsBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getTable(), 
				isSelected, comments, jTextAreaEnabled, "enableCommentsBinding");
		enableCommentsBinding.setSourceNullValue(false);
		enableCommentsBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(enableCommentsBinding);

		BeanProperty<AnalysisTablePanel, Boolean> clientSelected = BeanProperty.create(CLIENT_SELECTED);
		Binding<JTable, Boolean, AnalysisTablePanel, Boolean> clientSelectedBinding = Bindings.createAutoBinding(UpdateStrategy.READ, RunwalkVideoApp.getApplication().getClientTable(), 
				isSelected, this, clientSelected, "clientSelectedBinding");
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
		selectedClient.setDirty(true);
		if (selectedClient.getName() == null && selectedClient.getOrganization() == null) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
					"Voer eerst een naam in voor deze klant!", 
					"Fout aanmaken analyse", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Analysis analysis = new Analysis(selectedClient);
		AppUtil.persistEntity(analysis);
		//update tables ..
		int modelIndex = selectedClient.addAnalysis(analysis);
		int viewIndex;

		try {
			refreshTableBindings();
			viewIndex = getTable().convertRowIndexToView(modelIndex);
			makeRowVisible(viewIndex);
		} catch( IndexOutOfBoundsException exc) {
			getLogger().error("Exception occured updating analysis selection", exc);
		}
		
		getApplication().getAnalysisOverviewTable().getItemList().add(analysis);
		modelIndex = getApplication().getAnalysisOverviewTable().getItemList().indexOf(analysis);
		try {
			getApplication().getAnalysisOverviewTable().refreshTableBindings();
			viewIndex = RunwalkVideoApp.getApplication().getAnalysisOverviewTable().getTable().convertRowIndexToView(modelIndex);
			getApplication().getAnalysisOverviewTable().makeRowVisible(viewIndex);
		} catch( IndexOutOfBoundsException exc) {
			getLogger().error("Exception occured updating analysis overview selection", exc);
		}

		getLogger().debug("Analysis " + analysis.getId() + " for client " + selectedClient.getId() + " (" + selectedClient.getName() +  ") added.");
		getApplication().getMediaControls().captureFrameToFront();
	}

	@Action(enabledProperty = "rowSelected")
	public void deleteAnalysis() {		
		if (RunwalkVideoApp.getApplication().getAnalysisTablePanel().isRowSelected()) {
			int n = JOptionPane.showConfirmDialog(
					RunwalkVideoApp.getApplication().getMainFrame(),
					getResourceMap().getString("deleteAnalysis.confirmDialog.text"),
					getResourceMap().getString("deleteAnalysis.Action.text"),
					JOptionPane.WARNING_MESSAGE,
					JOptionPane.OK_CANCEL_OPTION);
			if (n == JOptionPane.CANCEL_OPTION ||n == JOptionPane.CLOSED_OPTION) return;
			Analysis selectedAnalysis = getSelectedItem();
			int selectedRow = getTable().getSelectedRow();
			getApplication().getSelectedClient().removeAnalysis(selectedAnalysis);
//			selectedAnalysis.getRecording().getVideoFile().delete();
			//select previous records..
			if (selectedRow > 0) {
				makeRowVisible(selectedRow-1);
			} else {
				clearItemSelection();
			}

			selectedRow = getApplication().getAnalysisOverviewTable().getTable().getSelectedRow() - 1;
			getApplication().getAnalysisOverviewTable().getItemList().remove(selectedAnalysis);
			/*if (selectedRow > 0) {
				RunwalkVideoApp.getApplication().getAnalysisOverviewTable().makeRowVisible(selectedRow-1);
			} else {
				RunwalkVideoApp.getApplication().getAnalysisOverviewTable().clearItemSelection();
			}*/

			AppUtil.deleteEntity(selectedAnalysis);
			getLogger().debug("Analysis " + selectedAnalysis.getId() + " for client " + selectedAnalysis.getClient().getId() + " (" + selectedAnalysis.getClient().getName() + ") deleted."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		//TODO kan je deze properties niet binden?? eventueel met een listener.. 
		getApplication().getAnalysisOverviewTable().setCompressionEnabled(true);
	}

	public boolean isClientSelected() {
		return clientSelected;
	}

	public void setClientSelected(boolean clientSelected) {
		this.firePropertyChange(CLIENT_SELECTED, this.clientSelected, this.clientSelected = clientSelected);
	}

	@Override
	public void update() {
		refreshTableBindings();
	}

}
