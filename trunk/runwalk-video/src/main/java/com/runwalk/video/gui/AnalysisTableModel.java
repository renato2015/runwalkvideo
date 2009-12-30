package com.runwalk.video.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Articles;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;

@SuppressWarnings("serial")
public class AnalysisTableModel extends AbstractTableModel<Analysis> {

	public AnalysisTableModel() {
		super("analysisTableModel", 5);
	}

	public void update() {
		//save comments currently viewed..
		RunwalkVideoApp.getApplication().getAnalysisTablePanel().saveComments();
		if (RunwalkVideoApp.getApplication().getSelectedClient() != null) {
			setItemList(RunwalkVideoApp.getApplication().getSelectedClient().getAnalyses());
			sortItemList();
			clearItemSelection();
			fireTableDataChanged();
		}
	}

	public Object getValueAt(int row, int column) {
		if (RunwalkVideoApp.getApplication().getSelectedClient() != null) {
			final Analysis analysis = getItem(row);
			final Recording movie = analysis.getRecording();
			switch(column) {
			case 0: return analysis.getTimeStamp();
			case 1: return analysis.getArticle();
			case 2: return movie == null || movie.getKeyframes() == null ? "<geen>" : movie.getKeyframes().size();
			case 3: return analysis.hasRecording() ? movie.formatDuration(ApplicationUtil.DURATION_FORMATTER) : "<geen>";
			case 4: {
				final JButton button = new JButton("open");
				button.setEnabled(analysis.hasRecording());
				button.setFont(ApplicationSettings.MAIN_FONT);
				button.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (button.isEnabled()) {
							RunwalkVideoApp.getApplication().getPlayerGUI().playFile(movie);
						}
					}
				});
				return button;
			}
			}
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		Analysis selectedAnalysis = getItem(row);
		switch(column) {
		case 1: selectedAnalysis.setArticle((Articles) value);break;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1;
	}

}
