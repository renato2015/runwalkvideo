package com.runwalk.video.media;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.application.TaskEvent;
import org.jdesktop.application.TaskListener;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyStateEvent;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.runwalk.video.core.Containable;
import com.runwalk.video.core.OnEdt;
import com.runwalk.video.core.WindowConstants;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.media.VideoComponent.State;
import com.runwalk.video.model.AnalysisModel;
import com.runwalk.video.panels.AbstractTablePanel;
import com.runwalk.video.panels.AnalysisTablePanel;
import com.runwalk.video.settings.SettingsManager;
import com.runwalk.video.tasks.AbstractTask;
import com.runwalk.video.tasks.CreateKeyframeTask;
import com.runwalk.video.tasks.RecordTask;
import com.runwalk.video.ui.WindowManager;
import com.runwalk.video.ui.actions.ApplicationActionConstants;
import com.runwalk.video.ui.actions.MediaActionConstants;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class MediaControls extends JPanel implements PropertyChangeListener, ApplicationActionConstants, 
	MediaActionConstants, Containable, ActionListener {

	// enabled action properties
	public static final String MUTED = "muted";
	public static final String STOP_ENABLED = "stopEnabled";
	public static final String RECORDING_ENABLED = "recordingEnabled";
	public static final String PLAYER_CONTROLS_ENABLED = "playerControlsEnabled";

	// selected action properties
	private static final String TITLE_SEPARATOR = " > ";	
	private static final String PLAYING = "playing";
	private static final String CAPTURER_CONTROLS_ENABLED = "capturerControlsEnabled";
	private static final String TITLE = "Media Controls";

	private final SettingsManager appSettings;
	private final AbstractTablePanel<AnalysisModel> analysisTablePanel;

	private final WindowManager windowManager;
	private final VideoFileManager videoFileManager;
	private final DaoService daoService;
	private final VideoCapturerFactory<?> videoCapturerFactory;
	private final VideoPlayerFactory<?> videoPlayerFactory;

	private final Timer timer;

	// open the selected recording
	private final AbstractTablePanel.ClickHandler<AnalysisModel> clickHandler = new AbstractTablePanel.ClickHandler<AnalysisModel>() {

		public void handleClick(AnalysisModel element) {
			if (element.isRecorded()) {
				getTaskService().execute(openRecordings(element));
			}
		}

	};
	
	final JComponent busyGlassPane = new JComponent() {{
			setOpaque(true);
	}};

	private Boolean selectedRecordingRecordable = false;
	private boolean recordingEnabled, playerControlsEnabled, stopEnabled, capturerControlsEnabled, toggleFullScreenEnabled;

	private JLabel elapsedTimeLabel;
	private JSlider scroll;

	private Set<VideoComponent> videoComponents = Sets.newHashSet();
	private VideoComponent frontMostComponent;

	/**
	 * <code>true</code> if all of the active {@link VideoPlayer}s are in playback mode
	 */
	private boolean playing = false;

	/**
	 * <code>true</code> if all of the active {@link VideoPlayer}s are muting their sound
	 */
	private boolean muted  = false;

	private RecordTask recordTask = null;

	public MediaControls(SettingsManager appSettings, VideoFileManager videoFileManager, WindowManager windowManager, 
			DaoService daoService, VideoCapturerFactory<?> videoCapturerFactory, VideoPlayerFactory<?> videoPlayerFactory,
			AnalysisTablePanel analysisTablePanel) {
		this.appSettings = appSettings;
		this.videoFileManager = videoFileManager;
		this.daoService = daoService;
		this.windowManager = windowManager;
		this.videoCapturerFactory = videoCapturerFactory;
		this.videoPlayerFactory = videoPlayerFactory;
		this.analysisTablePanel = analysisTablePanel;

		setLayout(new MigLayout("insets 10 10 0 10, nogrid, fill"));
		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<MediaControls, Boolean> playingEnabled = BeanProperty.create(PLAYER_CONTROLS_ENABLED);
		Binding<?, Boolean, ?, Boolean> enabledBinding = null;

		ELProperty<AnalysisTablePanel, Boolean> recorded = ELProperty.create("${rowSelected && !selectedItem.recorded && !selectedItem.feedbackRecord}");
		BeanProperty<MediaControls, Boolean> recordingEnabled = BeanProperty.create(RECORDING_ENABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, analysisTablePanel, recorded, this, recordingEnabled);
		enabledBinding.addBindingListener(new AbstractBindingListener() {

			@Override
			public void sourceChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
				selectedRecordingRecordable = (Boolean) binding.getSourceValueForTarget().getValue();
			}

		});
		enabledBinding.setSourceUnreadableValue(false);
		enabledBinding.setTargetNullValue(false);
		bindingGroup.addBinding(enabledBinding);

		timer = new Timer(50, this);

		setSlider(new JSlider(JSlider.HORIZONTAL, 0, 1000, 0));
		getSlider().setPaintTicks(false);
		getSlider().setPaintLabels(true);
		getSlider().setSnapToTicks(false);
		// Listener for the scroll
		getSlider().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				if (getFrontMostPlayer() != null && getFrontMostPlayer().isActive()) {
					int position = getSlider().getValue() * getFrontMostPlayer().getDuration() / 1000;
					getLogger().debug("Slide stateChanged : " + position);
					getFrontMostPlayer().setPosition(position);
				}
			}
		});

		BeanProperty<JSlider, Boolean> enabled = BeanProperty.create("enabled");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, playingEnabled, getSlider(), enabled);
		bindingGroup.addBinding(enabledBinding);
		add(getSlider(), "wrap, grow, gapbottom 10");

		//add control buttons
		createJButton(PREVIOUS_KEYFRAME_ACTION); 
		createJButton(SLOWER_ACTION); 
		createJButton(RECORD_ACTION);
		createJButton(STOP_ACTION); 
		createJButton(TOGGLE_PLAY_ACTION); 
		createJButton(FASTER_ACTION); 
		createJButton(NEXT_KEYFRAME_ACTION); 
		createJButton(CREATE_KEYFRAME_ACTION); 
		//createJButton(DECREASE_VOLUME_ACTION); 
		//createJButton(INCREASE_VOLUME_ACTION); 
		//createJButton(TOGGLE_MUTED_ACTION); 
		createJButton(SHOW_CAPTURER_SETTINGS_ACTION); 
		createJButton(SHOW_CAMERA_SETTINGS_ACTION); 
		// TODO bring consistency in button behavior, toggle fullscreen for all active windows??
		//createJButton(FULL_SCREEN_ACTION); 

		elapsedTimeLabel = new JLabel();
		clearStatusInfo();
		add(elapsedTimeLabel, "gapleft 15");
		// bind these bindings!
		bindingGroup.bind();

		final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusManager.addPropertyChangeListener(new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();
				Window activeWindow = focusManager.getActiveWindow();
				if (activeWindow != null && e.getNewValue() != null) {
					if (!activeWindow.getName().equals("mainFrame") && Frame.class.isAssignableFrom(activeWindow.getClass())) {
						enableVideoComponentControls(((Frame) activeWindow).getTitle());
					} else if ("focusOwner".equals(property)) {
						Component component = SwingUtilities.getAncestorOfClass(JInternalFrame.class, (Component) e.getNewValue());
						enableVideoComponentControls(component);
					}
				}
			}

		});
	}

	private AbstractButton createJButton(String actionName) {
		javax.swing.Action action = getAction(actionName);
		AbstractButton button = null;
		if (action.getValue(javax.swing.Action.SELECTED_KEY) != null) {
			button = new JToggleButton(action);
			String selectedIconResourceName = actionName + ".Action.selectedIcon";
			Icon selectedIcon = getResourceMap().getIcon(selectedIconResourceName);
			button.setSelectedIcon(selectedIcon);
			button.setRolloverEnabled(false);
		} else {
			button = new JButton(action);
		}
		button.setMargin(new Insets(2, 2, 2, 2));
		button.setActionCommand(actionName);
		add(button, "gap 0");
		return button;
	}

	public AbstractTablePanel.ClickHandler<AnalysisModel> getClickHandler() {
		return clickHandler;
	}

	@Action(block=BlockingScope.APPLICATION)
	public Task<Void, Void> disposeVideoComponents() {
		return new AbstractTask<Void, Void>(DISPOSE_VIDEO_COMPONENTS_ACTION) {

			protected Void doInBackground() throws Exception {
				message("startMessage", videoComponents.size());
				for (VideoComponent videoComponent : Lists.newArrayList(videoComponents)) {
					this.getLogger().debug("Stopping video for " + videoComponent.getTitle());
					videoComponent.dispose();
				}
				new Robot().waitForIdle();
				message("endMessage");
				return null;
			}

		};
	}

	@Action(enabledProperty = STOP_ENABLED)
	public Task<?, ?> stop() {
		Task<?, ?> result = null;
		setStopEnabled(false);
		timer.stop();
		if (isRecording()) {
			stopRecording();
		} else {
			result = stopPlaying();
		}
		return result;
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED, selectedProperty = PLAYING )
	public Task<?, ?> togglePlaying() {
		return new AbstractTask<Void, Void>(TOGGLE_PLAY_ACTION) {

			protected Void doInBackground() throws Exception {
				for (VideoPlayer player : getPlayers()) {
					if (isPlaying()) {
						player.play();
						timer.restart();
						setStopEnabled(true);
						message("startMessage", player.getPlayRate());
					} else {
						player.pause();
						timer.stop();
						message("endMessage");
					}
				}
				return null;
			}

		};

	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(final boolean playing) {
		firePropertyChange(PLAYING, this.playing, this.playing = playing);

	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void increaseVolume() {
		for (VideoPlayer player : getPlayers()) {
			player.increaseVolume();
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void decreaseVolume() {
		for (VideoPlayer player : getPlayers()) {
			player.decreaseVolume();
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED, selectedProperty = MUTED)
	public Task<?, ?> toggleMuted() {
		return new AbstractTask<Void, Void>(TOGGLE_MUTED_ACTION) {

			protected Void doInBackground() throws Exception {
				for (VideoPlayer player : getPlayers()) {
					player.setMuted(isMuted());
				}
				return null;
			}

		};
	}

	/**
	 * Mute audio for all active {@link VideoPlayer}s.
	 * @param muted set to <code>true</code> to mute 
	 */
	public void setMuted(boolean muted) {
		firePropertyChange(MUTED, this.muted, this.muted  = muted);
	}

	/**
	 * This getter will return <code>false</code> if at least one active {@link VideoPlayer} is not muted.
	 * @return <code>true</code> if all players are playing audio
	 */
	public boolean isMuted() {
		boolean result = false;
		for (VideoPlayer player : getPlayers()) {
			result |= player.isMuted();
		}
		return result;
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public Task<?, ?> slower(ActionEvent event) {
		return changePlayRate(event);
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public Task<?, ?> faster(ActionEvent event) {
		return changePlayRate(event);
	}

	private Task<?, ?> changePlayRate(final ActionEvent event) {
		Task<?, ?> result = null;
		if (!isPlaying()) {
			// set selectedProperty manually as it won't be set when invoking it from code
			setPlaying(true);
			invokeAction(TOGGLE_PLAY_ACTION, event.getSource());
		} else {
			result = new AbstractTask<Void, Void>(CHANGE_PLAY_RATE_ACTION) {

				protected Void doInBackground() throws Exception {
					List<Float> result = Lists.newArrayList();
					for (VideoPlayer player : getPlayers()) {
						float playRate = player.getPlayRate();
						if (SLOWER_ACTION.equals(event.getActionCommand())) {
							playRate = player.slower();
						} else if (FASTER_ACTION.equals(event.getActionCommand())) {
							playRate = player.faster();
						}
						result.add(playRate);
					}
					// save play rate to settings
					message("endMessage", result);
					return null;
				}

			};

		}
		return result;
	}

	@Action
	public Task<VideoPlayer, Void> openRecording(javax.swing.Action action) {
		Object value = action.getValue(javax.swing.Action.LONG_DESCRIPTION);
		final String url = JOptionPane.showInputDialog(value == null ? "" : value.toString()); 
		AbstractTask<VideoPlayer, Void> result = null;
		result = new AbstractTask<VideoPlayer, Void>(OPEN_RECORDING_ACTION) {

			protected VideoPlayer doInBackground() throws Exception {
				VideoPlayer result = null;
				if (url != null && url.length() > 0) {
					message("startMessage", url);
					result = videoPlayerFactory.createVideoPlayer(url);
				}
				return result;
			}

		};
		result.addTaskListener(new TaskListener.Adapter<VideoPlayer, Void>() {

			public void succeeded(TaskEvent<VideoPlayer> event) {
				VideoPlayer videoPlayer = event.getValue();
				if (videoPlayer != null) {
					getWindowManager().addWindow(videoPlayer);
				}
			}


		});
		return result;
	}

	@Action(enabledProperty = CAPTURER_CONTROLS_ENABLED)
	public Task<?, ?> showCapturerSettings() {
		return getFrontMostCapturer().showCapturerSettings();
	}

	@Action(enabledProperty = CAPTURER_CONTROLS_ENABLED)
	public Task<?, ?> showCameraSettings() {
		return getFrontMostCapturer().showCameraSettings();
	}
	
	private void setPlayerPosition(VideoPlayer videoPlayer, int position) {
		videoPlayer.setPosition(position);
		if (videoPlayer == getFrontMostPlayer()) {
			setStatusInfo(videoPlayer.getPosition(), videoPlayer.getDuration());
		}
		getLogger().debug("Player position " + videoPlayer.getPosition() + " " + position);
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void nextKeyframe() {
		for (VideoPlayer videoPlayer : getPlayers()) {
			videoPlayer.pauseIfPlaying();
			// get the current recording for the player
			String videoPath = videoPlayer.getVideoPath();
			Recording recording = getVideoFileManager().getRecording(videoPath);
			recording.sortKeyframes();
			for (Keyframe frame : recording.getKeyframes()) {
				if (frame.getPosition() > videoPlayer.getPosition()) {
					setPlayerPosition(videoPlayer, frame.getPosition());
					setPlaying(false);
					return;
				}
			}
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void previousKeyframe(ActionEvent event) {
		for (VideoPlayer videoPlayer : getPlayers()) {
			videoPlayer.pauseIfPlaying();
			String videoPath = videoPlayer.getVideoPath();
			Recording recording = getVideoFileManager().getRecording(videoPath);
			recording.sortKeyframes();
			for (int i = recording.getKeyframes().size()-1; i >= 0; i--) {
				Keyframe frame = recording.getKeyframes().get(i);
				if (frame.getPosition() < videoPlayer.getPosition()) {
					setPlayerPosition(videoPlayer, frame.getPosition());
					setPlaying(false);
					return;
				}
			}
			invokeAction(STOP_ACTION, event.getSource());
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public CreateKeyframeTask createKeyframe() {
		CreateKeyframeTask result = new CreateKeyframeTask(getVideoFileManager(), getDaoService(), getFrontMostPlayer(), getPlayers());
		result.addTaskListener(new TaskListener.Adapter<Keyframe, Void>() {

			@SuppressWarnings("unchecked")
			@Override
			public void succeeded(TaskEvent<Keyframe> event) {
				Keyframe result = event.getValue();
				// set the slider's position to that of the frontmost player's newly created Keyframe
				Integer keyframePosition = result.getPosition();
				int sliderPosition = getSliderPosition(keyframePosition);
				getSlider().getLabelTable().put(sliderPosition, new JLabel("*"));
				setStatusInfo(keyframePosition, getFrontMostPlayer().getDuration());
				getSlider().updateUI();
			}

		});
		setPlaying(false);
		return result;
	}

	/**
	 * This property is changed under two different conditions:
	 * 
	 * <ul>
	 * <li>When an {@link Analysis} is selected in the table then we have to perform a check on the activity of the capture graph here</li>
	 * <li>When the capture graph is activated by making it the frontmost window</li>
	 * </ul>
	 * 
	 * @return Whether recording is enabled or not
	 */
	public void setRecordingEnabled(boolean recordSelected) {
		boolean capturersReady = false;
		for (VideoCapturer capturer : getCapturers()) {
			capturersReady = capturer.isActive() && !capturer.isRecording();
		}
		boolean recordingEnabled = capturersReady && recordSelected && selectedRecordingRecordable && !isRecording();
		for (VideoPlayer player : getPlayers()) {
			recordingEnabled &= player.isIdle();
		}
		if (recordingEnabled && !this.recordingEnabled) {
			setPlayerControlsEnabled(false);
			capturersToFront();
		}
		firePropertyChange(RECORDING_ENABLED, this.recordingEnabled, this.recordingEnabled = recordingEnabled);
	}

	public boolean isRecordingEnabled() {
		return recordingEnabled;
	}

	public boolean getPlayerControlsEnabled() {
		return playerControlsEnabled;
	}

	public boolean getCapturerControlsEnabled() {
		return capturerControlsEnabled;
	}

	public void setStopEnabled(boolean stopEnabled) {
		firePropertyChange(STOP_ENABLED, this.stopEnabled, this.stopEnabled = stopEnabled);
	}

	public boolean isStopEnabled() {
		return stopEnabled;
	}

	public boolean isToggleFullScreenEnabled() {
		return toggleFullScreenEnabled;
	}

	public void setToggleFullScreenEnabled(boolean fullScreenEnabled) {
		firePropertyChange(TOGGLE_FULL_SCREEN_ENABLED, this.toggleFullScreenEnabled, this.toggleFullScreenEnabled = fullScreenEnabled );
	}

	@Action
	public void startVideoCapturer() {
		// if there is no actionEvent specified, then this call was made at startup time
		VideoComponent capturer = videoCapturerFactory.createVideoCapturer();
		if (capturer != null) {
			capturer.addPropertyChangeListener(this);
			videoComponents.add(capturer);
			getWindowManager().addWindow(capturer);
			capturersToFront();
		}
	}

	@Action(enabledProperty = RECORDING_ENABLED)
	public RecordTask record() {
		final AnalysisModel selectedModel = getAnalysisTablePanel().getSelectedItem();
		RecordTask result = null;
		if (recordTask == null && selectedModel != null) {
			setRecordingEnabled(false);
			setStopEnabled(true);
			result = new RecordTask(getVideoFileManager(), getDaoService(), getCapturers(), selectedModel);
			result.addTaskListener(new TaskListener.Adapter<Boolean, Void>() {
				
				@Override
				public void process(TaskEvent<List<Void>> event) {
					getAnalysisTablePanel().updateRow(selectedModel);
				}

				@Override
				public void finished(TaskEvent<Void> event) {
					// set this manually as such a specific propertyChangeEvent won't be fired
					selectedRecordingRecordable = false;
					setRecordingEnabled(false);
					getAnalysisTablePanel().updateRow(selectedModel);
				}

			});
			recordTask = result;
			timer.restart();
		}
		return result;
	}

	private boolean isRecording() {
		return recordTask != null && recordTask.isRecording();
	}

	private void stopRecording() {
		synchronized(recordTask) {
			recordTask.setRecording(false);
			recordTask.notifyAll();
		}
		recordTask = null;
	}

	private Task<?, ?> stopPlaying() {
		setPlaying(false);
		setStatusInfo(0, getFrontMostPlayer().getDuration());
		return new AbstractTask<Void, Void>(STOP_PLAYING_ACTION) {

			protected Void doInBackground() throws Exception {
				for (VideoPlayer player : getPlayers()) {
					player.stop();
				}
				message("startMessage");
				return null;
			}
		};
	}

	public Task<?, ?> openRecordings(final AnalysisModel analysisModel) {
		return new AbstractTask<Void, Void>(OPEN_RECORDINGS_ACTION) {

			{ 
				setInputBlocker(new CursorInputBlocker(this, SwingUtilities.getWindowAncestor(MediaControls.this))); 
			}

			protected Void doInBackground() throws Exception {
				message("startMessage");
				VideoPlayer videoPlayer = null;
				int recordingCount = 0;
				if (analysisModel != null) {
					for(int i = 0; i < analysisModel.getRecordings().size(); i++) {
						final Recording recording = analysisModel.getRecordings().get(i);
						if (analysisModel.isRecorded()) {
							final File videoFile = getVideoFileManager().getVideoFile(recording);
							if (recordingCount < getPlayers().size()) {
								videoPlayer = getPlayers().get(recordingCount);
								loadVideo(videoPlayer, videoFile);
							} else {
								startVideoPlayer(videoFile);
							} 

						}
						setSliderLabels(recording);
						recordingCount++;
					}
					setSliderPosition(0);
					new Robot().waitForIdle();
					message("endMessage", recordingCount, analysisModel.getEntity().getClient());
				}
				return null;
			}

		};
	}

	@OnEdt
	public void loadVideo(final VideoPlayer videoPlayer, final File videoFile) {
		// if loading fails, rebuild and show again
		if (videoPlayer.loadVideo(videoFile.getAbsolutePath())) {
			getWindowManager().refreshScreen();
		}
		getWindowManager().toFront(videoPlayer);
	}

	@OnEdt
	public void startVideoPlayer(final File videoFile) {
		VideoPlayer videoPlayer = videoPlayerFactory.createVideoPlayer(videoFile.getAbsolutePath());
		videoPlayer.addPropertyChangeListener(MediaControls.this);
		videoComponents.add(videoPlayer);
		getWindowManager().addWindow(videoPlayer);
		getWindowManager().toFront(videoPlayer);
	}

	private Hashtable<Integer, JLabel> createLabelTable() {
		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		table.put(getSlider().getMinimum(), new JLabel("|"));
		table.put(getSlider().getMaximum(), new JLabel("|"));
		return table;
	}

	@OnEdt
	public void setSliderLabels(Recording recording) {
		Hashtable<Integer, JLabel> table = createLabelTable();
		if (recording != null) {
			JLabel asterisk = new JLabel("*");
			for (Keyframe e:recording.getKeyframes()) {
				table.put(getSliderPosition(e.getPosition()), asterisk);
			}
		}
		getSlider().setLabelTable(table);
		getSlider().updateUI();
		getSlider().revalidate();
	}

	private void clearStatusInfo() {
		setSliderLabels(null);
		setStatusInfo(0, 0);
	}

	private void setSliderPosition(int position) {
		getSlider().setValue(getSliderPosition(position));
	}

	private void updateTimeStamps(long position, long duration) {
		String elapsedTime = AppUtil.formatDate(new Date(position),AppUtil.EXTENDED_DURATION_FORMATTER);
		String totalTime = AppUtil.formatDate(new Date(duration), AppUtil.EXTENDED_DURATION_FORMATTER);
		elapsedTimeLabel.setText(elapsedTime + " / " + totalTime);
	}

	private void setStatusInfo(int position, int duration) {
		setSliderPosition(position);
		updateTimeStamps(position, duration);
	}

	private void setStatusInfo(Recording recording, int position, int duration) {
		setSliderLabels(recording);
		setStatusInfo(position, duration);
	}

	private void setSlider(JSlider scroll) {
		this.scroll = scroll;
	}

	private JSlider getSlider() {
		return scroll;
	}

	private int getSliderPosition(int position) {
		int result = 0;
		if (getFrontMostPlayer() != null ) {
			result = (int) ((double) 1000 *  position / getFrontMostPlayer().getDuration());
		}
		return result;
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof Timer) {
			if (getFrontMostCapturer() != null && isRecording()) {
				String videoPath = getFrontMostCapturer().getVideoPath();
				Recording recording = getVideoFileManager().getRecording(videoPath);
				// set duration for recording
				long executionDuration = recordTask.getExecutionDuration(TimeUnit.MILLISECONDS);
				recording.setDuration(executionDuration);	
				// only update status info if it is the fronmost capturer
				updateTimeStamps(executionDuration, 0);
			} else if (getFrontMostPlayer() != null && isPlaying()) {
				// only update status info for the frontmost player
				int position = getFrontMostPlayer().getPosition();
				if ((position == 0 || position >= getFrontMostPlayer().getDuration()) 
						&& getFrontMostPlayer().isPlaying()) {
					getLogger().debug("playback position set to 0");
					invokeAction(STOP_ACTION, this);
				}
				setStatusInfo(position, getFrontMostPlayer().getDuration());
			}
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		if (evt.getPropertyName().equals(VideoComponent.STATE)) {
			VideoComponent component = (VideoComponent) evt.getSource();
			State state = (State) newValue;
			if (component == frontMostComponent) {
				boolean fullScreenEnabled = getWindowManager().isToggleFullScreenEnabled(component);
				setToggleFullScreenEnabled(fullScreenEnabled);
			}
			if (state == State.DISPOSED) {
				videoComponents.remove(component);
				disableVideoComponentControls(component);
				component.removePropertyChangeListener(this);
			}
		} else if (evt.getPropertyName().equals(WindowConstants.VISIBLE)) {
			toggleVideoComponentControls((VideoComponent) evt.getSource(), (Boolean) evt.getNewValue());
		}
		//DSJ fires the following event for notifying that playing has stoppped..
		//DSJUtils.getEventType(evt) == DSMovie.FRAME_NOTIFY
	}

	private void toggleVideoComponentControls(final VideoComponent videoComponent, boolean enable) {
		if (enable) {
			enableVideoComponentControls(videoComponent);
		} else {
			disableVideoComponentControls(videoComponent);
		}
	}

	private void enableVideoComponentControls(final Component component) {
		VideoComponent videoComponent = getWindowManager().findVideoComponent(videoComponents, component);
		enableVideoComponentControls(videoComponent);
	}

	private void enableVideoComponentControls(final String title) {
		VideoComponent videoComponent = getWindowManager().findVideoComponent(videoComponents, title);
		getLogger().debug("Focus requested for " + title);
		enableVideoComponentControls(videoComponent);
	}

	/**
	 * Enabling a {@link VideoComponent}'s controls is only possible when the frontmost component is idle, otherwise the
	 * request will be ignored. All controls will be disabled when null is passed as the component argument.
	 * 
	 * @param videoComponent The videoComponent to enable
	 * @param enable <code>true</code> if the component needs to get button state
	 */
	private void enableVideoComponentControls(final VideoComponent videoComponent) {
		// a player or capturer is requesting the focus..
		boolean isActive = videoComponent != null && videoComponent.isActive();
		boolean isIdle = isActive && (frontMostComponent == null || frontMostComponent.isIdle());
		// check whether the component is not idle and if it is of a similar class as the frontmost component
		boolean isSimilarAndWorking = frontMostComponent != null && !frontMostComponent.isIdle() && 
				videoComponent != null && videoComponent.getClass() == frontMostComponent.getClass();
		if (isIdle || isSimilarAndWorking) {
			frontMostComponent = videoComponent;
			StringBuilder title = new StringBuilder(getTitle());
			title.append(TITLE_SEPARATOR).append(videoComponent.getTitle());
			if (isIdle) {
				if (videoComponent instanceof VideoCapturer) {
					clearStatusInfo();
					// only set if the videoImpl is not a self containing (native) window
					if (videoComponent.isFocusable()) {
						setRecordingEnabled(true);
					}
					setPlayerControlsEnabled(false);
					setStopEnabled(false);
				} else {
					setRecordingEnabled(false);
					setPlayerControlsEnabled(true);
					setStopEnabled(getFrontMostPlayer().getPosition() > 0);
					String videoPath = getFrontMostPlayer().getVideoPath();
					Recording recording = getVideoFileManager().getRecording(videoPath);
					setStatusInfo(recording, getFrontMostPlayer().getPosition(), getFrontMostPlayer().getDuration());
					title.append(TITLE_SEPARATOR ).append(recording.getVideoFileName());
				}
			}
			setToggleFullScreenEnabled(getWindowManager().isToggleFullScreenEnabled(videoComponent));
			getWindowManager().setTitle(this, title.toString());
		} /*else {
			// try to enable recording again, if no capturer is active, it will be disabled
			setRecordingEnabled(true);
		}*/
	}

	private void disableVideoComponentControls(final VideoComponent component) {
		if (frontMostComponent == component) {
			Iterable<? extends VideoComponent> components = getComponents(component.getClass());
			try {
				VideoComponent newComponent = Iterables.find(components, new Predicate<VideoComponent>() {

					public boolean apply(VideoComponent input) {
						return input != component && getWindowManager().isVisible(input);
					}

				});
				enableVideoComponentControls(newComponent);
			} catch(NoSuchElementException e) {
				disableMediaControls();
			}
		}
	}

	private void disableMediaControls() {
		frontMostComponent = null;
		clearStatusInfo();
		setStopEnabled(false);
		setPlayerControlsEnabled(false);
		setToggleFullScreenEnabled(false);
		setRecordingEnabled(false);
		getWindowManager().setTitle(this, getName());
	}

	public void setPlayerControlsEnabled(boolean playerControlsEnabled) {
		firePropertyChange(PLAYER_CONTROLS_ENABLED, this.playerControlsEnabled, this.playerControlsEnabled = playerControlsEnabled);
		// enabled capturer controls according to state of player controls
		boolean isFrontmostCapturer = frontMostComponent instanceof VideoCapturer;
		firePropertyChange(CAPTURER_CONTROLS_ENABLED, this.capturerControlsEnabled, this.capturerControlsEnabled = !playerControlsEnabled && isFrontmostCapturer);
	}

	public void capturersToFront() {
		for (VideoComponent videoCapturer : getCapturers()) {
			getWindowManager().toFront(videoCapturer);
			if (!videoCapturer.isFocusable()) {
				// atm the only way to request focus for this window type
				enableVideoComponentControls(videoCapturer);
			}
		}
	}

	private VideoCapturer getFrontMostCapturer() {
		return getFrontMostComponent(VideoCapturer.class);
	}

	private VideoPlayer getFrontMostPlayer() {
		return getFrontMostComponent(VideoPlayer.class);
	}

	private <T extends VideoComponent> T getFrontMostComponent(Class<T> concreteClass) {
		try {
			return concreteClass.cast(frontMostComponent);
		} catch(ClassCastException e) {
			return null;
		}
	}

	private List<VideoCapturer> getCapturers() {
		return getComponents(VideoCapturer.class);
	}

	private List<VideoPlayer> getPlayers() {
		return getComponents(VideoPlayer.class);
	}

	private <T extends VideoComponent> List<T> getComponents(Class<T> filterClass) {
		ImmutableList.Builder<T> result = ImmutableList.builder();
		for (VideoComponent videoComponent : videoComponents) {
			if (videoComponent.getClass() == filterClass) {
				result.add(filterClass.cast(videoComponent));
			}
		}
		return result.build();
	}

	public WindowManager getWindowManager() {
		return windowManager;
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public SettingsManager getAppSettings() {
		return appSettings;
	}

	public AbstractTablePanel<AnalysisModel> getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	public Component getComponent() {
		return this;
	}

	public boolean isResizable() {
		return false;
	}

	public String getTitle() {
		return TITLE;
	}

}

