package com.runwalk.video.gui.panels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.ObservableElementList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;

import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.entities.RecordingStatus;
import com.runwalk.video.gui.DateTableCellRenderer;
import com.runwalk.video.gui.OpenRecordingButton;
import com.runwalk.video.gui.tasks.CleanupRecordingsTask;
import com.runwalk.video.gui.tasks.CompressTask;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;
import com.runwalk.video.util.ResourceInjector;

@SuppressWarnings("serial")
public class AnalysisOverviewTablePanel extends AbstractTablePanel<Analysis> {

	private static final String COMPRESSION_ENABLED = "compressionEnabled";

	private boolean compressionEnabled;
	final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
	final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

	public AnalysisOverviewTablePanel() {
		super(new MigLayout("fill, nogrid"));
		JScrollPane scrollPane = new  JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewportView(getTable());
		add(scrollPane, "wrap, grow");
		
		JButton cleanupButton = new JButton(getAction("cleanup"));
		cleanupButton.setFont(AppSettings.MAIN_FONT);
		add(cleanupButton);
		setSecondButton(new JButton(getAction("compress")));
		getSecondButton().setFont(AppSettings.MAIN_FONT);
		add(getSecondButton());
	}

	@Action
	public Task<Boolean, Void> cleanup() {
		return new CleanupRecordingsTask(AppSettings.getInstance().getUncompressedVideoDir());
	}

	@Action(enabledProperty = COMPRESSION_ENABLED, block=Task.BlockingScope.APPLICATION)
	public Task<Boolean, Void> compress() {
		setCompressionEnabled(false);
		return new CompressTask(getUncompressedRecordings(), AppSettings.getInstance().getTranscoder());
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
	protected EventList<Analysis> specializeItemList(EventList<Analysis> eventList) {
		return new FilterList<Analysis>(eventList, new Matcher<Analysis>() {

			public boolean matches(Analysis item) {
				return item.getRecording() != null && !item.getRecording().isCompressed();
			}
			
		});
	}

	@Override
	public void setItemList(EventList<Analysis> itemList, ObservableElementList.Connector<Analysis> itemConnector) {
		super.setItemList(itemList, itemConnector);
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
		addMouseListenerToTable();
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
	    	switch(column) {
	    	case 0: return analysis.getRecording() != null && analysis.getRecording().isCompressed() ? completedIcon : incompleteIcon;
	    	case 1: return analysis.getCreationDate();
	    	case 2: return analysis.getClient().getName() + " " + analysis.getClient().getFirstname();
	    	case 3: return analysis.getRecording() != null ? analysis.getRecording().getKeyframeCount() : 0;
	    	case 4: return analysis.getRecording() != null ? analysis.getRecording().getDuration() : 0L;
	    	case 5: {
	    		String result = "<geen>";
	    		if (analysis.getRecording() != null) {
	    			String resourceKey = analysis.getRecording().getRecordingStatus().getResourceKey();
	    			result = ResourceInjector.injectResources(resourceKey, RecordingStatus.class);
	    		} 
	    		return result;
	    	}
	    	case 6: return new OpenRecordingButton(analysis.getRecording());
	    	default: return null;
	    	}
	    }
		
	}

}
