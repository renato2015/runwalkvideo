package com.runwalk.video.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.actions.RecordingStatus;
import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;

@SuppressWarnings("serial")
public class AnalysisOverviewTableModel extends AbstractTableModel<Analysis> {

	private ImageIcon completedIcon, incompleteIcon;

	public AnalysisOverviewTableModel() {
		super("analysisOverviewTableModel", 7);
		completedIcon = this.getModelResourceMap().getImageIcon("cameraTableModel.syncCompletedIcon");
		incompleteIcon = this.getModelResourceMap().getImageIcon("cameraTableModel.syncNeededIcon");
	}

	public List<Recording> getUncompressedRecordings() {
		List<Recording> list = new ArrayList<Recording>();
		for (Analysis analysis : getItemList()) {
			Recording movie = analysis.getRecording();
			if (movie != null && movie.isCompressable()) {
				list.add(movie);
			}
		}
		return list;
	}

	public boolean isCompressionEnabled() {
		for(Analysis analysis : getItemList()) {
			if (analysis.hasCompressableRecording()) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		if (RunwalkVideoApp.getApplication().isSaveNeeded()) {
			int n = JOptionPane.showConfirmDialog(
					RunwalkVideoApp.getApplication().getMainFrame(),
					"Er zijn nog onbewaarde veranderingen.\nVernieuwen zal deze ongedaan maken",
					"Bent u zeker dat u wil vernieuwen?",
					JOptionPane.OK_CANCEL_OPTION);
			if (n == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		setItemList(new ArrayList<Analysis>());
		Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createQuery("SELECT an from Analysis an WHERE an.recording.statuscode <> " + RecordingStatus.COMPRESSED.getCode()); // NOI18N
		ArrayList<Analysis> cachedItems = new ArrayList<Analysis>(query.getResultList());
		for(Analysis cachedAnalysis : cachedItems) {
		if (cachedAnalysis.getClient() != null) {
			int index = RunwalkVideoApp.getApplication().getClientTableModel().getItemIndex(cachedAnalysis.getClient());
			if (index != -1) {
				Client modelClient = RunwalkVideoApp.getApplication().getClientTableModel().getItem(index);
				//query naar de analyses verbonden aan de gevonden client
				for(Analysis analysis : modelClient.getAnalyses()) {
					//voeg de overeenstemmende analyse toe
					if (cachedAnalysis.getId().equals(analysis.getId())) {
						if (!analysis.hasCompressedRecording()) {
							addItem(analysis);
						}
					}
				}
			}
		}
		}
		sortItemList();
		fireTableDataChanged();
		RunwalkVideoApp.getApplication().getTableActions().setCompressionEnabled(true);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		final Analysis analysis = getItem(rowIndex);
		final Recording movie = analysis.getRecording();
		if (analysis != null) {
			//			File movie = getMovie(analysis);
			switch(columnIndex) {
			case 0: return analysis.hasCompressedRecording() ? completedIcon : incompleteIcon;
			case 1: return analysis.getTimeStamp();
			case 2: return analysis.getClient().toString();
			case 3: return movie != null ? movie.getKeyframeCount() : "<geen>";
			case 4:	return analysis.hasRecording() ? movie.formatDuration(ApplicationUtil.DURATION_FORMATTER) : "<geen>";
			case 5: return movie != null ? movie.getRecordingStatus().getDescription() : RecordingStatus.NON_EXISTANT_FILE.getDescription();
			case 6: {
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
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

}
