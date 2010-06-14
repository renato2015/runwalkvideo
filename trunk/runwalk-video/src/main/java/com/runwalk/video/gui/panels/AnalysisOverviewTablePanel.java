package com.runwalk.video.gui.panels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.DateTableCellRenderer;
import com.runwalk.video.gui.OpenRecordingButton;
import com.runwalk.video.gui.tasks.CleanupRecordingsTask;
import com.runwalk.video.gui.tasks.CompressTask;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class AnalysisOverviewTablePanel extends AbstractTablePanel<Analysis> {

	private static final String CLEANUP_ENABLED = "cleanupEnabled";
	private static final String COMPRESSION_ENABLED = "compressionEnabled";

	private boolean cleanupEnabled, compressionEnabled;
	final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
	final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

	public AnalysisOverviewTablePanel() {
		super(new AbsoluteLayout());
		
		JScrollPane overviewScrollPane = new  JScrollPane();
		overviewScrollPane.setViewportView(getTable());
		add(overviewScrollPane, new AbsoluteConstraints(10, 20, 550, 140));
		
		JButton cleanupButton = new JButton(getAction("cleanup"));
		cleanupButton.setFont(AppSettings.MAIN_FONT);
		add(cleanupButton, new AbsoluteConstraints(320, 170, -1, -1));
		setSecondButton(new JButton(getAction("compress")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton(), new AbsoluteConstraints(470, 170, -1, -1));
	}

	public boolean isCleanupEnabled() {
		return cleanupEnabled;
	}

	public void setCleanupEnabled(boolean cleanUpEnabled) {
		this.firePropertyChange(CLEANUP_ENABLED, this.cleanupEnabled, this.cleanupEnabled = cleanUpEnabled);
	}

	@Action(enabledProperty=CLEANUP_ENABLED)
	public Task<Boolean, Void> cleanup() {
		final Task<Boolean, Void> cleanupTask = new CleanupRecordingsTask(AppSettings.getInstance().getUncompressedVideoDir());
		cleanupTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {
			
			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				setCleanupEnabled(!event.getValue());
			}
			
		});
		return cleanupTask;
	}

	@Action(enabledProperty = COMPRESSION_ENABLED, block=Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> compress() {
		setCompressionEnabled(false);
		setCleanupEnabled(true);
		final CompressTask compressTask = new CompressTask(getUncompressedRecordings(), AppSettings.getInstance().getTranscoder());
		compressTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {
			
			@Override
			public void cancelled(TaskEvent<Void> event) {
				getApplication().setSaveNeeded(true);
			}

			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				setCompressionEnabled(!event.getValue());
				getApplication().setSaveNeeded(true);
			}

		});

		return compressTask;
	}

	public boolean isCompressionEnabled() {
		return compressionEnabled;
	}

	public void setCompressionEnabled(boolean compressionEnabled) {
		if (compressionEnabled) {
			for(Analysis analysis : getItemList()) {
				if (analysis.hasCompressableRecording()) {
					firePropertyChange(COMPRESSION_ENABLED, this.compressionEnabled, this.compressionEnabled = true);
					return;
				}
			}
		} else {
			firePropertyChange(COMPRESSION_ENABLED, this.compressionEnabled, this.compressionEnabled = false);
		}
	}

	public List<Recording> getUncompressedRecordings() {
		List<Recording> list = new ArrayList<Recording>();
		for (Analysis analysis : getItemList()) {
			Recording recording = analysis.getRecording();
			if (recording != null && recording.isCompressable()) {
				list.add(recording);
			}
		}
		return list;
	}
	
	public TableFormat<Analysis> getTableFormat() {
		return new AnalysisOverviewTableFormat();
	}
	
	@Override
	public void setItemList(EventList<Analysis> itemList, Class<Analysis> itemClass) {
		super.setItemList(itemList, itemClass);
		getTable().getColumnModel().getColumn(0).setCellRenderer(getTable().getDefaultRenderer(ImageIcon.class));
		getTable().getColumnModel().getColumn(0).setMaxWidth(25);
		getTable().getColumnModel().getColumn(1).setCellRenderer(new DateTableCellRenderer(AppUtil.EXTENDED_DATE_FORMATTER));
		getTable().getColumnModel().getColumn(1).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(4).setCellRenderer(new DateTableCellRenderer(AppUtil.DURATION_FORMATTER));
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(5).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(6).setCellRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		getTable().getColumnModel().getColumn(6).setPreferredWidth(40);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		//TODO mouselistener should only be added once!!
//		if (getTable().getMouseListeners().length == 0) {
			getTable().addMouseListener(new JTableButtonMouseListener());
//		}

	}
	
	public class AnalysisOverviewTableFormat implements TableFormat<Analysis> {

	    public int getColumnCount() {
	        return 7;
	    }

	    public String getColumnName(int column) {
	        if(column == 0)      return "";
	        else if(column == 1) return "Tijdstip analyse";
	        else if(column == 2) return "Naam klant";
	        else if(column == 3) return "Aantal keyframes";
	        else if(column == 4) return "Duur video";
	        else if(column == 5) return "Status";
	        else if(column == 6) return "";
	        throw new IllegalStateException();
	    }

	    public Object getColumnValue(Analysis analysis, int column) {
	    	analysis.setDirty(false);
	    	switch(column) {
	    	case 0: return analysis.getRecording() != null && analysis.getRecording().isCompressed() ? completedIcon : incompleteIcon;
	    	case 1: return analysis.getCreationDate();
	    	case 2: return analysis.getClient().getName() + " " + analysis.getClient().getFirstname();
	    	case 3: return analysis.getRecording() != null ? analysis.getRecording().getKeyframeCount() : 0;
	    	case 4: return analysis.getRecording() != null ? analysis.getRecording().getDuration() : 0L;
	    	case 5: return analysis.getRecording() != null ? analysis.getRecording().getRecordingStatus().getDescription() : "<geen>";
	    	case 6: return new OpenRecordingButton(analysis.getRecording());
	    	default: return null;
	    	}
	    }
		
	}

}
