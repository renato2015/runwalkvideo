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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

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

	public AnalysisTablePanel() {
		super(new AbsoluteLayout());

		JScrollPane analysisTableScrollPanel = new  JScrollPane();
		analysisTableScrollPanel.setViewportView(getTable());
		BindingGroup bindingGroup = new BindingGroup();

		AbstractBindingListener changeListener = new ClientBindingListener();

		/*//analyses view binding
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
		getTable().addMouseListener(new JTableButtonMouseListener());*/
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
		comments.getDocument().addUndoableEditListener(RunwalkVideoApp.getApplication().getApplicationActions().getUndoableEditListener());
		comments.setFont(AppSettings.MAIN_FONT);
		comments.setColumns(20);
		comments.setRows(3);
		tscrollPane.setViewportView(comments);
		add(tscrollPane, new AbsoluteConstraints(10, 165, 550, 60));

		//comments JTextArea binding
		BeanProperty<AnalysisTablePanel, String> selectedElementComments = BeanProperty.create("selectedItem.comments");
		BeanProperty<JTextArea, String> jTextAreaValue = BeanProperty.create("text");
		Binding<? extends AbstractTablePanel<?> , String, JTextArea, String> commentsBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, this, 
				selectedElementComments, comments, jTextAreaValue);
		commentsBinding.addBindingListener(changeListener);
		bindingGroup.addBinding(commentsBinding);

		ELProperty<AnalysisTablePanel, Boolean> isSelected = ELProperty.create("${selectedItem != null}");
		BeanProperty<JTextArea, Boolean> jTextAreaEnabled = BeanProperty.create("enabled");
		Binding<?, Boolean, JTextArea, Boolean> enableCommentsBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, 
				isSelected, comments, jTextAreaEnabled);
		enableCommentsBinding.setSourceNullValue(false);
		enableCommentsBinding.setSourceUnreadableValue(false);
		bindingGroup.addBinding(enableCommentsBinding);

		ELProperty<ClientTablePanel, Boolean> isClientSelected = ELProperty.create("${selectedItem != null}");
		BeanProperty<AnalysisTablePanel, Boolean> clientSelected = BeanProperty.create(CLIENT_SELECTED);
		Binding<? extends AbstractTablePanel<?>, Boolean, AnalysisTablePanel, Boolean> clientSelectedBinding = Bindings.createAutoBinding(UpdateStrategy.READ, RunwalkVideoApp.getApplication().getClientTablePanel(), 
				isClientSelected, this, clientSelected);
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
		getItemList().getReadWriteLock().writeLock().lock();
		Analysis analysis = new Analysis(selectedClient);
		AppUtil.persistEntity(analysis);
		try {
			selectedClient.addAnalysis(analysis);
			makeRowVisible(analysis);
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
		final Client selectedClient = RunwalkVideoApp.getApplication().getSelectedClient();
		try {
			int lastSelectedRowIndex = getEventSelectionModel().getMinSelectionIndex();
			Analysis selectedAnalysis = getSelectedItem();
			selectedClient.removeAnalysis(selectedAnalysis);
			AppUtil.deleteEntity(selectedAnalysis);
			makeRowVisible(lastSelectedRowIndex - 1);
		} finally {
			getItemList().getReadWriteLock().writeLock().unlock();
		}
		//TODO kan je deze properties niet binden?? eventueel met een listener.. 
		getApplication().getAnalysisOverviewTable().setCompressionEnabled(true);
	}

	public boolean isClientSelected() {
		return clientSelected;
	}

	public void setClientSelected(boolean clientSelected) {
		//TODO make sure this is a single selection??
		this.firePropertyChange(CLIENT_SELECTED, this.clientSelected, this.clientSelected = clientSelected);
	}

	public TableFormat<Analysis> getTableFormat() {
		return new NewClientTablePanel.AnalysisTableFormat();
	}

}
