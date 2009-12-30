package com.runwalk.video.gui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.util.ApplicationSettings;

public class AnalysisOverviewTablePanel extends AbstractTablePanel {

	private static final long serialVersionUID = 1L;

	private  JLabel statusLabel;

	public AnalysisOverviewTablePanel(AbstractTableModel<Analysis> model) {
		super(model, new AbsoluteLayout());
		JScrollPane conversionScrollPanel = new  JScrollPane();
		conversionScrollPanel.setViewportView(getTable());
		getTable().getColumnModel().getColumn(0).setMaxWidth(25);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		getTable().getColumnModel().getColumn(0).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(ImageIcon.class)));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(5).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(6).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(6).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				RunwalkVideoApp.getApplication().getTableActions().setAnalysisSelected(isRowSelected());
				if (isRowSelected()) {
					int selected = getTable().getSelectedRow();
					getGenericTableModel().setSelectedIndex(getTable().convertRowIndexToModel(selected));
				} else {
					getGenericTableModel().clearItemSelection();
				}
//				if (RunwalkVideoApp.getApplication().getPlayer().getCaptureGraph().getActive()) {
//					RunwalkVideoApp.getApplication().getPlayerGUI().enableRecording(isSelectedItemRecorded());
//				}
			}
		});
		getTable().addMouseListener(new JTableButtonMouseListener());

		add(conversionScrollPanel, new AbsoluteConstraints(10, 20, 550, 140));

		JButton deleteDuplicateButton = new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("cleanup"));
		deleteDuplicateButton.setFont(ApplicationSettings.MAIN_FONT);
		add(deleteDuplicateButton, new AbsoluteConstraints(235, 170, -1, -1));
		setSecondButton(new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("compress"))); // NOI18N
		getSecondButton().setFont(ApplicationSettings.MAIN_FONT);
		add(getSecondButton(), new AbsoluteConstraints(370, 170, -1, -1));
		setFirstButton(new JButton(RunwalkVideoApp.getApplication().getTableActionMap().get("updateOverview"))); // NOI18N
		getFirstButton().setFont(ApplicationSettings.MAIN_FONT);
		add(getFirstButton(), new AbsoluteConstraints(470, 170, -1, -1));

		setMessageLabel(new JLabel());
		getMessageLabel().setFont(ApplicationSettings.MAIN_FONT);
		//		add(getSyncLabel(), new AbsoluteConstraints(10, 170, 290, 30));
	}

	public void setStatusMessage(String msg) {
		getMessageLabel().setText(msg);
	}

	private void setMessageLabel(JLabel syncLabel) {
		this.statusLabel = syncLabel;
	}

	private JLabel getMessageLabel() {
		return statusLabel;
	}

}
