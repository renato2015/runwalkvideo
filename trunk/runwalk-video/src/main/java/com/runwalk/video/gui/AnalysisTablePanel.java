package com.runwalk.video.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.persistence.Query;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Articles;
import com.runwalk.video.util.ApplicationSettings;

public class AnalysisTablePanel extends AbstractTablePanel {

	private static final long serialVersionUID = 1L;

	private JTextArea comments;
	
	public static class LoggingBindingListener extends AbstractBindingListener {

	    public void syncFailed(Binding binding, SyncFailure fail) {
	        String description;
	        if ((fail != null) && (fail.getType() == Binding.SyncFailureType.VALIDATION_FAILED)) {
	            description = fail.getValidationResult().getDescription();
	        } else {
	            description = "Sync failed!";
	        }
	        String msg = "[" + binding.getName() + "] " + description;
	        Logger.getLogger(getClass()).debug(msg);
	    }

	    @Override
	    public void synced(Binding binding) {
	        String bindName = binding.getName();
	        String msg = "[" + bindName + "] Synced";
	        Logger.getLogger(getClass()).debug(msg);
	    }

	}

	public AnalysisTablePanel(AbstractTableModel<Analysis> model) {
		super(model, new AbsoluteLayout());

		JScrollPane analysisTableScrollPanel = new  JScrollPane();
		analysisTableScrollPanel.setViewportView(getTable());

		getTable().getColumnModel().getColumn(0).setMinWidth(70);
		getTable().getColumnModel().getColumn(0).setResizable(false);
//		getTable().getColumnModel().getColumn(2).setMaxWidth(50);
		//		JFormattedTextField dateField = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
		//		getTable().getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(dateField));

		Query query = RunwalkVideoApp.getApplication().createQuery("select OBJECT(ar) from Articles ar"); // NOI18N
		ObservableList<Articles> articleList = ObservableCollections.observableList(query.getResultList());

		JComboBox shoes = new JComboBox();
		shoes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RunwalkVideoApp.getApplication().setSaveNeeded(true);
			}
		});
		//		shoes.setEditable(true);
		shoes.setFont(ApplicationSettings.MAIN_FONT);
		JComboBoxBinding<Articles, List<Articles>, JComboBox> cb = 
			SwingBindings.createJComboBoxBinding(AutoBinding.UpdateStrategy.READ, articleList, shoes);
		cb.bind();
		
		getTable().getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(shoes));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(120);

		getTable().getColumnModel().getColumn(3).setPreferredWidth(18);
		getTable().getColumnModel().getColumn(3).setResizable(false);

		getTable().getColumnModel().getColumn(4).setPreferredWidth(5);
		getTable().getColumnModel().getColumn(4).setResizable(false);
		getTable().getColumnModel().getColumn(4).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		add(analysisTableScrollPanel, new AbsoluteConstraints(10, 20, 550, 100));
		getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				RunwalkVideoApp.getApplication().getTableActions().setAnalysisSelected(isRowSelected());
				getComments().setEnabled(isRowSelected());
				boolean isCaptureGraphActive = RunwalkVideoApp.getApplication().getPlayer().getCaptureGraph().getActive();
				if (isRowSelected()) {
					int selected = getTable().getSelectedRow();
					 /* valuechange event wordt gedispatched voor focusgained..
					 * daarom is het nodig om een eventuele waarde in het comments field 
					 * eerst te bewaren vooraleer de selectie te laten veranderen
					 */
					AnalysisTablePanel.this.saveComments();
					getGenericTableModel().setSelectedIndex(getTable().convertRowIndexToModel(selected));
					setComments(RunwalkVideoApp.getApplication().getSelectedAnalysis().getComments());
					if (isCaptureGraphActive && isSelectedItemRecorded()) {
						RunwalkVideoApp.getApplication().getPlayerGUI().captureFrameToFront();
					}
				}
				RunwalkVideoApp.getApplication().getPlayerGUI().enableRecording(isSelectedItemRecorded() && isCaptureGraphActive);
			}
		});
		getTable().addMouseListener(new JTableButtonMouseListener());

		JPanel buttonPanel =  new JPanel();
		buttonPanel.setLayout(new AbsoluteLayout());

		setSecondButton(new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("addAnalysis")));
		getSecondButton().setFont(ApplicationSettings.MAIN_FONT);
		buttonPanel.add(getSecondButton(), new AbsoluteConstraints(10, 0, -1, -1));

		setFirstButton(new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("deleteAnalysis")));
		getFirstButton().setFont(ApplicationSettings.MAIN_FONT);
		buttonPanel.add(getFirstButton(), new AbsoluteConstraints(130, 0, -1, -1));

		add(buttonPanel, new AbsoluteConstraints(0, 130, -1, 30));

		JScrollPane tscrollPane = new JScrollPane();
		tscrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		comments = new JTextArea();
		getComments().addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				saveComments();
			}
			
		});

		getComments().getDocument().addUndoableEditListener(RunwalkVideoApp.getApplication().getApplicationActions().getUndoableEditListener());
		getComments().setFont(ApplicationSettings.MAIN_FONT);
		getComments().setColumns(20);
		getComments().setRows(3);
		tscrollPane.setViewportView(getComments());
		add(tscrollPane, new AbsoluteConstraints(10, 165, 550, 60));
		}

		public void clearComments() {
			setComments("");
		}

		private void setComments(String comments) {
			this.getComments().setText(comments);
		}

		private JTextArea getComments() {
			return comments;
		}
		
		public boolean isSelectedItemRecorded() {
			return (isRowSelected() && !RunwalkVideoApp.getApplication().getSelectedAnalysis().hasRecording());
		}

		public void saveComments() {
			Analysis selectedAnalysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
			if (isRowSelected() && selectedAnalysis != null) {
				selectedAnalysis.setComments(getComments().getText());
				RunwalkVideoApp.getApplication().setSaveNeeded(true);
			}
		}

	}
