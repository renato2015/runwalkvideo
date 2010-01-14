package com.runwalk.video.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Client;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.tasks.CleanupRecordingsTask;
import com.runwalk.video.gui.tasks.CompressTask;
import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;

public class AnalysisOverviewTablePanel extends AbstractTablePanel<Analysis> {

	private static final String CLEANUP_ENABLED = "cleanupEnabled";

	private static final String COMPRESSION_ENABLED = "compressionEnabled";

	private boolean cleanupEnabled, compressionEnabled;

	@SuppressWarnings("unchecked")
	public AnalysisOverviewTablePanel() {
		super(new AbsoluteLayout());

		final ImageIcon completedIcon = getResourceMap().getImageIcon("status.complete.icon");
		final ImageIcon incompleteIcon = getResourceMap().getImageIcon("status.incomplete.icon");

		update();

		//analyses view binding
		BeanProperty<AnalysisOverviewTablePanel, List<Analysis>> analyses = BeanProperty.create("itemList");
		jTableSelectionBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ_WRITE, this, analyses, getTable());

		//icon binding
		ELProperty<Analysis, Boolean> compressed = ELProperty.create("${recording.compressed}");
		JTableBinding<Analysis, ?, JTable>.ColumnBinding columnBinding = jTableSelectionBinding.addColumnBinding(compressed);
		//		JTableBinding<Analysis, List<Analysis>, JTable>.ColumnBinding columnBinding = jTableSelectionBinding.addColumnBinding(compressed);
		columnBinding.addBindingListener(new AbstractBindingListener() {

			@Override
			public void synced(Binding binding1) {
				getLogger().debug("synced");
			}

			@Override
			public void syncFailed(Binding binding1, SyncFailure syncfailure) {
				getLogger().debug("sync failed");
			}

		});
		columnBinding.setColumnClass(ImageIcon.class);
		columnBinding.setColumnName("");
		columnBinding.setConverter(new Converter<Boolean, ImageIcon>() {

			@Override
			public ImageIcon convertForward(Boolean arg0) {
				return arg0 ? completedIcon : incompleteIcon;
			}

			@Override
			public Boolean convertReverse(ImageIcon arg0) {
				return null;
			}
		});

		//analysis date binding
		BeanProperty<Analysis, Date> timestamp = BeanProperty.create("creationDate");
		columnBinding = jTableSelectionBinding.addColumnBinding(timestamp);
		columnBinding.setColumnName("Tijdstip analyse");
		columnBinding.setEditable(false);
		columnBinding.setConverter(new Converter<Date, String>() {

			@Override
			public String convertForward(Date arg0) {
				return ApplicationUtil.formatDate(arg0, ApplicationUtil.EXTENDED_DATE_FORMATTER);
			}

			@Override
			public Date convertReverse(String arg0) {
				return null;
			}

		});

		//client name binding
		ELProperty<Analysis, String> clientName = ELProperty.create("${client.firstname} ${client.name}");
		columnBinding = jTableSelectionBinding.addColumnBinding(clientName);
		columnBinding.setColumnName("Naam klant");
		columnBinding.setEditable(false);

		//keyframe count binding
		ELProperty<Analysis, Integer> recordingKeyframeCount = ELProperty.create("${recording.keyframeCount}");
		columnBinding = jTableSelectionBinding.addColumnBinding(recordingKeyframeCount);
		columnBinding.setColumnName("Aantal keyframes");
		columnBinding.setEditable(false);

		//recording duration binding
		ELProperty<Analysis, Long> recordingDuration = ELProperty.create("${recording.duration}");
		columnBinding = jTableSelectionBinding.addColumnBinding(recordingDuration);
		columnBinding.setColumnName("Duur video");
		columnBinding.setEditable(false);
		columnBinding.setConverter(new Converter<Long, String>() {

			@Override
			public String convertForward(Long duration) {
				return duration == null ? "<geen>" : ApplicationUtil.formatDate(new Date(duration), ApplicationUtil.DURATION_FORMATTER);
			}

			@Override
			public Long convertReverse(String arg0) {
				return null;
			}

		});

		//recording status description
		ELProperty<Analysis, String> statusDescription = ELProperty.create("${recording.recordingStatus.description}");
		columnBinding = jTableSelectionBinding.addColumnBinding(statusDescription);
		columnBinding.setColumnName("Status");
		columnBinding.setColumnClass(String.class);
		columnBinding.setEditable(false);

		//play button binding
		BeanProperty<Analysis, OpenRecordingButton> openButton = BeanProperty.create("recording");
		columnBinding = jTableSelectionBinding.addColumnBinding(openButton);
		columnBinding.setRenderer(new CustomJTableRenderer(getTable().getDefaultRenderer(JButton.class)));
		getTable().addMouseListener(new JTableButtonMouseListener());
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
		BindingGroup bindingGroup = new BindingGroup();
		bindingGroup.addBinding(jTableSelectionBinding);
		jTableSelectionBinding.bind();
		bindingGroup.bind();
		getTable().addMouseListener(new JTableButtonMouseListener());

		JScrollPane overviewScrollPane = new  JScrollPane();
		overviewScrollPane.setViewportView(getTable());
		getTable().getColumnModel().getColumn(0).setMaxWidth(25);
		getTable().getColumnModel().getColumn(0).setResizable(false);
		getTable().getColumnModel().getColumn(1).setPreferredWidth(80);
		getTable().getColumnModel().getColumn(4).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(5).setPreferredWidth(60);
		getTable().getColumnModel().getColumn(6).setPreferredWidth(80);
		getTable().addMouseListener(new JTableButtonMouseListener());

		add(overviewScrollPane, new AbsoluteConstraints(10, 20, 550, 140));

		JButton deleteDuplicateButton = new JButton(getAction("cleanup"));
		deleteDuplicateButton.setFont(ApplicationSettings.MAIN_FONT);
		add(deleteDuplicateButton, new AbsoluteConstraints(235, 170, -1, -1));
		setSecondButton(new JButton(getAction("compress")));
		getSecondButton().setFont(ApplicationSettings.MAIN_FONT);
		add(getSecondButton(), new AbsoluteConstraints(370, 170, -1, -1));
		setFirstButton(new JButton(getAction("refresh")));
		getFirstButton().setFont(ApplicationSettings.MAIN_FONT);
		add(getFirstButton(), new AbsoluteConstraints(470, 170, -1, -1));

	}

	@Action
	public void update() {
		List<Analysis> result = new ArrayList<Analysis>();
		for(Client client : RunwalkVideoApp.getApplication().getClientTablePanel().getItemList()) {
			for(Analysis analysis : client.getAnalyses()) {
				if (!analysis.getRecording().isCompressed()) {
					result.add(analysis);
				}
			}
		}
		Collections.sort(result);
		setItemList(result);
		setCompressionEnabled(true);
	}

	public boolean isCleanupEnabled() {
		return cleanupEnabled;
	}

	public void setCleanupEnabled(boolean cleanUpEnabled) {
		this.cleanupEnabled = cleanUpEnabled;
		this.firePropertyChange(CLEANUP_ENABLED, !isCleanupEnabled(), isCleanupEnabled());
	}

	@Action(enabledProperty=CLEANUP_ENABLED)
	public Task<Boolean, Void> cleanup() {
		final Task<Boolean, Void> cleanupTask = new CleanupRecordingsTask(ApplicationSettings.getInstance().getUncompressedVideoDir());
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

		final CompressTask compressTask = new CompressTask(getUncompressedRecordings(), ApplicationSettings.getInstance().getSettings().getTranscoder());
		compressTask.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {
			
			@Override
			public void cancelled(TaskEvent<Void> event) {
				getApplication().setSaveNeeded(true);
			}

			@Override
			public void succeeded(TaskEvent<Boolean> event) {
				setCompressionEnabled(!event.getValue());
				setCleanupEnabled(true);
				refreshTableBindings();
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

}
