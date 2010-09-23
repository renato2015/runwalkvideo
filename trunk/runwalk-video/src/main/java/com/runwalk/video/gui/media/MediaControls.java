package com.runwalk.video.gui.media;

import java.awt.AWTEvent;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyStateEvent;

import com.google.common.collect.Lists;
import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.media.VideoComponent.State;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class MediaControls extends AppInternalFrame implements PropertyChangeListener {

	private static final String FULL_SCREEN_ENABLED = VideoComponent.FULL_SCREEN_ENABLED;
	public static final String STOP_ENABLED = "stopEnabled";
	public static final String RECORDING_ENABLED = "recordingEnabled";
	public static final String PLAYER_CONTROLS_ENABLED = "playerControlsEnabled";
	private static final String CAPTURER_CONTROLS_ENABLED = "capturerControlsEnabled";

	private Boolean selectedRecordingRecordable = false;
	private boolean recordingEnabled, playerControlsEnabled, stopEnabled, capturerControlsEnabled, fullScreenEnabled;

	private JLabel time;
	private JSlider scroll;
	private AbstractButton playButton;

	private List<VideoPlayer> players = Lists.newArrayList();
	private List<VideoCapturer> capturers = Lists.newArrayList();
	private VideoComponent frontMostComponent;
	private VideoPlayer frontMostPlayer;
	private VideoCapturer frontMostCapturer;

	private final AppSettings appSettings;
	private final AnalysisTablePanel analysisTablePanel;

	private final VideoFileManager videoFileManager;
	private final DaoService daoService;

	private boolean recording;

	public MediaControls(AnalysisTablePanel analysisTablePanel, AppSettings appSettings, VideoFileManager videoFileManager, DaoService daoService) {
		super("Media controls", false);
		this.videoFileManager = videoFileManager;
		this.daoService = daoService;
		this.appSettings = appSettings;
		this.analysisTablePanel = analysisTablePanel;

		setLayout(new MigLayout("insets 10 10 0 10, nogrid, fill"));
		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<MediaControls, Boolean> playingEnabled = BeanProperty.create(PLAYER_CONTROLS_ENABLED);
		Binding<?, Boolean, ?, Boolean> enabledBinding = null;

		ELProperty<AnalysisTablePanel, Boolean> recorded = ELProperty.create("${rowSelected && !selectedItem.recorded}");
		BeanProperty<MediaControls, Boolean> recordingEnabled = BeanProperty.create(RECORDING_ENABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, analysisTablePanel, recorded, this, recordingEnabled);
		enabledBinding.addBindingListener(new AbstractBindingListener() {

			@Override
			public void sourceChanged(@SuppressWarnings("rawtypes") Binding binding, PropertyStateEvent event) {
				selectedRecordingRecordable = (Boolean) binding.getSourceValueForTarget().getValue();
				System.out.println("source for binding changed");
			}

			@Override
			public void synced(@SuppressWarnings("rawtypes") Binding binding) {
				// TODO Auto-generated method stub
				super.synced(binding);
				System.out.println("binding synced");
			}
			
			

		});
		enabledBinding.setSourceUnreadableValue(false);
		enabledBinding.setTargetNullValue(false);
		bindingGroup.addBinding(enabledBinding);

		setSlider(new JSlider(JSlider.HORIZONTAL, 0, 1000, 0));
		getSlider().setPaintTicks(false);
		getSlider().setPaintLabels(true);
		getSlider().setSnapToTicks(false);
		// Listener for the scroll
		getSlider().addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				if (frontMostComponent instanceof VideoPlayer && frontMostComponent.isActive()) {
					VideoPlayer player = (VideoPlayer) frontMostComponent;
					int position = getSlider().getValue() * player.getDuration() / 1000;
					getLogger().debug("Slide stateChanged : " + position);
					player.setPosition(position);
				}
			}
		});

		BeanProperty<JSlider, Boolean> enabled = BeanProperty.create("enabled");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, playingEnabled, getSlider(), enabled);
		bindingGroup.addBinding(enabledBinding);
		add(getSlider(), "wrap, grow, gapbottom 10");

		//add control buttons
		createJButton("previousSnapshot"); 
		createJButton("slower"); 
		createJButton("record");

		createJButton("stop"); 
		playButton = createJToggleButton("togglePlay", "togglePlay.Action.pressedIcon"); 
		playButton.setRolloverEnabled(false);
		createJButton("faster"); 
		createJButton("nextSnapshot"); 
		createJButton("makeSnapshot"); 
		createJButton("decreaseVolume"); 
		createJButton("increaseVolume"); 
		createJToggleButton("mute", "mute.Action.pressedIcon"); 
		createJButton("showCaptureSettings"); 
		createJButton("showCameraSettings"); 
		createJButton("fullScreen"); 

		time = new JLabel();
		clearStatusInfo();
		add(time, "gapleft 15");
		// bind these bindings!
		bindingGroup.bind();
	}

	private AbstractButton createJToggleButton(String actionName, String iconResourceName) {
		AbstractButton button = createJButton(actionName, true);
		button.setSelectedIcon(getResourceMap().getIcon(iconResourceName));
		return button;
	}

	private AbstractButton createJButton(String actionName) {
		return createJButton(actionName, false);
	}

	private AbstractButton createJButton(String actionName, boolean toggleButton) {
		javax.swing.Action action = getAction(actionName);
		AbstractButton button = toggleButton ? new JToggleButton(action) : new JButton(action);
		button.setMargin(new Insets(2, 2, 2, 2));
		add(button, "gap 0");
		return button;
	}

	//TODO kan dit eventueel met een proxy action??
	@Action(enabledProperty = FULL_SCREEN_ENABLED, block = BlockingScope.APPLICATION)
	public void fullScreen() { 
		frontMostComponent.toggleFullscreen();
	}

	@Action(enabledProperty = STOP_ENABLED)
	public void stop() {
		if (isRecording()) {
			stopRecording();
		} else {
			stopPlaying();
		}
		setStopEnabled(false);
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED )
	public void togglePlay() {
		for (VideoPlayer player : players) {
			if (player.isPlaying()) {
				player.pause();
				getApplication().showMessage("Afspelen gepauzeerd");
			} else {
				player.play();
				setStopEnabled(true);
				getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
			}
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void increaseVolume() {
		for (VideoPlayer player : players) {
			player.increaseVolume();
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void decreaseVolume() {
		for (VideoPlayer player : players) {
			player.decreaseVolume();
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void mute() {
		for (VideoPlayer player : players) {
			player.mute();
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void slower() {
		for (VideoPlayer player : players) {
			if (!player.isPlaying()) {
				togglePlay();
			} else {
				float playRate = player.slower();
				// save play rate to settings
				getAppSettings().setPlayRate(playRate);
				getApplication().showMessage("Afspelen aan " + player.getPlayRate() + "x gestart.");
			} 
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void faster() {
		for (VideoPlayer player : players) {
			if (!player.isPlaying()) {
				togglePlay();
			} else {
				float playRate = player.faster();
				// save play rate to settings
				getAppSettings().setPlayRate(playRate);
				getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
			}
		}
	}

	//TODO this action should not be available when there is no capturer open
	@Action(enabledProperty = CAPTURER_CONTROLS_ENABLED)
	public void showCaptureSettings() {
		frontMostCapturer.showCapturerSettings();
	}

	@Action(enabledProperty = CAPTURER_CONTROLS_ENABLED)
	public void showCameraSettings() {
		frontMostCapturer.showCameraSettings();
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void nextSnapshot() {
		for (VideoPlayer player : players) {
			player.pauseIfPlaying();
			player.nextSnapshot();
		}
	}

	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void previousSnapshot() {
		for (VideoPlayer player : players) {
			player.pauseIfPlaying();
			player.previousSnapshot();
		}
	}

	@SuppressWarnings("unchecked")
	@Action(enabledProperty = PLAYER_CONTROLS_ENABLED)
	public void makeSnapshot() {
		int newPosition = 0;
		int duration = 0;
		for (VideoPlayer player : players) {
			player.pauseIfPlaying();
			int position = player.getKeyframePosition();
			if (frontMostPlayer == player) {
				newPosition = position;
				duration = player.getDuration();
			}
			// create a new Keyframe for the player's current recording
			Recording recording = player.getRecording();
			Keyframe snapshot = new Keyframe(recording, position);
			getDaoService().getDao(Keyframe.class).persist(snapshot);
			recording.addKeyframe(snapshot);
		}
		// set the slider's position to that of the frontmost player's newly created Keyframe
		int sliderPosition = getSliderPosition(newPosition);
		getSlider().getLabelTable().put(sliderPosition, new JLabel("*"));
		setStatusInfo(newPosition, duration);
		getSlider().updateUI();
		String formattedDate = AppUtil.formatDate(new Date(newPosition), AppUtil.EXTENDED_DURATION_FORMATTER);
		getApplication().showMessage("Snapshot genomen op " + formattedDate); 
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
		for (VideoCapturer capturer : capturers) {
			capturersReady = capturer.isActive() && !capturer.isRecording();
		}
		boolean recordingEnabled = capturersReady && recordSelected && selectedRecordingRecordable && !isRecording();
		for (VideoPlayer player : players) {
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
		this.firePropertyChange(STOP_ENABLED, this.stopEnabled, this.stopEnabled = stopEnabled);
	}

	public boolean isStopEnabled() {
		return stopEnabled;
	}

	public boolean isFullScreenEnabled() {
		return fullScreenEnabled;
	}

	@Action
	public void startCapturer() {
		String capturerName = getAppSettings().getCapturerName();
		String captureEncoderName = getAppSettings().getCaptureEncoderName();
		VideoCapturer capturer = VideoCapturerFactory.getInstance().createCapturer(this, capturerName, captureEncoderName);
		if (capturer != null) {
			// save chosen name only if this is the first chosen capturer
			if (capturers.isEmpty()) {
				getAppSettings().setCapturerName(capturer.getVideoImpl().getTitle());
			}
			capturer.addAppWindowWrapperListener(new WindowStateChangeListener(capturer));
			capturers.add(capturer);
			getApplication().createOrShowComponent(capturer);
			capturersToFront();
		}
	}

	@Action(enabledProperty = RECORDING_ENABLED)
	public void record() {
		Analysis analysis = getAnalysisTablePanel().getSelectedItem();
		if (analysis != null) {
			boolean isRecording = false;
			for (VideoCapturer capturer : capturers) {
				Recording recording = new Recording(analysis);
				// persist recording first, then add it to the analysis
				getDaoService().getDao(Recording.class).persist(recording);
				analysis.addRecording(recording);
				File videoFile = getVideoFileManager().getUncompressedVideoFile(recording);
				capturer.startRecording(recording, videoFile);
				isRecording = true;
			}
			if (isRecording) {
				getApplication().getStatusPanel().setIndeterminate(true);
				//TODO zoek uit wat er met die selectedProperty mogelijk is
				setRecordingEnabled(false);
				setStopEnabled(true);
				setRecording(true);
				getApplication().showMessage("Opname voor " + analysis.getClient().getName() + " " + 
						analysis.getClient().getFirstname() + " gestart..");
			}
		}
	}

	private void setRecording(boolean b) {
		recording = b;
	}

	private boolean isRecording() {
		return recording;
	}

	private void stopRecording() {
		boolean filesRecorded = false;
		for (VideoCapturer capturer : capturers) {
			capturer.stopRecording();
			File recordedFile = getVideoFileManager().getVideoFile(capturer.getRecording());
			filesRecorded = filesRecorded |= recordedFile != null;
			if (!filesRecorded) {
				getLogger().error("Check file consistency for " + recordedFile.getAbsolutePath()) ;
			}
			getApplication().showMessage("Opnemen van " + recordedFile.getName() + " voltooid.");
		}
		setRecording(false);
		// set this manually as such a specific propertyChangeEvent won't be fired
		selectedRecordingRecordable = false;
		getApplication().getStatusPanel().setIndeterminate(false);
		getApplication().getAnalysisOverviewTablePanel().setCompressionEnabled(filesRecorded);
	}

	private void stopPlaying() {
		for (VideoPlayer player : players) {
			player.stop();
		}
		setStatusInfo(0, frontMostPlayer.getDuration());
		getApplication().showMessage("Afspelen gestopt.");
	}

	public void playRecordings(final Analysis analysis) {
		int recordingCount = 0;
		for (Recording recording : analysis.getRecordings()) {
			if (recording.isRecorded()) {
				VideoPlayer player = null;
				try {
					File videoFile = getVideoFileManager().getVideoFile(recording);
					if (recordingCount < players.size()) {
						player = players.get(recordingCount);
						player.loadVideo(recording, videoFile.getAbsolutePath());
						getLogger().info("Videofile " + videoFile.getAbsolutePath() + " opened and ready for playback.");
						setSliderLabels(recording);
					} else {
						float playRate = getAppSettings().getPlayRate();
						player = VideoPlayer.createInstance(this, recording, videoFile.getAbsolutePath(), playRate);
						player.addAppWindowWrapperListener(new WindowStateChangeListener(player));
						players.add(player);
					} 
					recordingCount++;
					// show players that have new loaded files
					getApplication().createOrShowComponent(player);
					player.toFront();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this),
							"De opname die u probeerde te openen kon niet worden gevonden",
							"Fout bij openen opname",
							JOptionPane.ERROR_MESSAGE);
					getLogger().error(e);
				}
			}
		}
		getLogger().info("Opened " + recordingCount + " recording(s) for " + analysis.toString());
		// hide players that don't show any opened files, eventually draw black overlay later on
		for (int i = recordingCount; i < players.size(); i++) {
			getApplication().hideComponent(players.get(i));
		}
		setSliderPosition(0);
	}

	private Hashtable<Integer, JLabel> createLabelTable() {
		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		table.put(getSlider().getMinimum(), new JLabel("|"));
		table.put(getSlider().getMaximum(), new JLabel("|"));
		return table;
	}

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
		time.setText(elapsedTime + " / " + totalTime);
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
		if (frontMostPlayer != null ) {
			result = (int) ((double) 1000 *  position / frontMostPlayer.getDuration());
		}
		return result;
	}

	public void disposeVideoComponents() {
		// create new lists to iterate over as the dispose() method will remove them from their lists
		for (VideoCapturer capturer : Lists.newArrayList(capturers)) {
			capturer.dispose();
		}
		for (VideoPlayer player : Lists.newArrayList(players)) {
			player.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		if (evt.getPropertyName().equals(VideoComponent.STATE)) {
			State state = (State) newValue;
			boolean enabled = state == VideoComponent.State.IDLE && evt.getSource() == frontMostComponent;
			firePropertyChange(FULL_SCREEN_ENABLED, fullScreenEnabled, fullScreenEnabled = enabled );
			playButton.setSelected(state == State.PLAYING);
			if (state == State.DISPOSED) {
				// remove vide component from its list
				if (evt.getSource() instanceof VideoCapturer) {
					capturers.remove(evt.getSource());
					if (evt.getSource() == frontMostCapturer) {
						frontMostCapturer = null;
						if (frontMostComponent == evt.getSource()) {
							enableVideoComponentControls(null, true);
						}
					}
				} else if (evt.getSource() instanceof VideoPlayer) {
					players.remove(evt.getSource());
					if (evt.getSource() == frontMostPlayer) {
						frontMostPlayer = null;
						if (frontMostComponent == evt.getSource()) {
							enableVideoComponentControls(null, true);
						}
					}
				}
			}
		} else if (evt.getPropertyName().equals(VideoCapturer.TIME_RECORDING)) {
			AppWindowWrapper capturer = (AppWindowWrapper) evt.getSource();
			Long timeRecorded = (Long) newValue;
			// only update status info if it is the fronmost capturer
			if (frontMostCapturer == capturer) {
				updateTimeStamps(timeRecorded, 0);
			}
		} else if (evt.getPropertyName().equals(VideoCapturer.CAPTURE_ENCODER_NAME)) {
			// save capture encoder name for the given videoCapturer?	
		} else if (evt.getPropertyName().equals(VideoPlayer.POSITION))  {
			Integer position = (Integer) newValue;
			VideoPlayer player = (VideoPlayer) evt.getSource();
			// only update status info for the frontmost player
			if (frontMostPlayer == player) {
				if (position == 0) {
					getLogger().debug("playback position set to 0");
					stop();
				}
				setStatusInfo(position, player.getDuration());
			}
		}
		//DSJ fires the following event for notifying that playing has stoppped..
		//DSJUtils.getEventType(evt) == DSMovie.FRAME_NOTIFY
	}

	/**
	 * Enabling a {@link VideoComponent}'s controls is only possible when the frontmost component is idle, otherwise the
	 * request will be ignored. All controls will be disabled when null is passed as the component argument.
	 * 
	 * @param component The videoComponent to enable
	 * @param enable The enabled state
	 */
	private void enableVideoComponentControls(VideoComponent component, boolean enable) {
		//a player or capturer is requesting the focus.. this will only be given if the frontmost component is idle
		if (frontMostComponent == null || enable && frontMostComponent.isIdle() || component == null) {
			frontMostComponent = component;
			StringBuilder title = new StringBuilder(getName());
			if (component != null) {
				title.append(" > ").append(component.getTitle());
				if (component instanceof VideoCapturer) {
					frontMostCapturer = (VideoCapturer) component;
					clearStatusInfo();
					setRecordingEnabled(true);
					setPlayerControlsEnabled(false);
					setStopEnabled(false);
				} else {
					frontMostPlayer = (VideoPlayer) component;
					setRecordingEnabled(false);
					setPlayerControlsEnabled(true);
					setStopEnabled(frontMostPlayer.getPosition() > 0);
					setStatusInfo(frontMostPlayer.getRecording(), frontMostPlayer.getPosition(), frontMostPlayer.getDuration());
					title.append(" > " ).append(frontMostPlayer.getRecording().getVideoFileName());
				}
				firePropertyChange(FULL_SCREEN_ENABLED, fullScreenEnabled, fullScreenEnabled = component.isFullScreenEnabled());	
			} else {
				clearStatusInfo();
				setRecording(false);
				setStopEnabled(false);
				setPlayerControlsEnabled(false);
			}
			setTitle(title.toString());
		}
	}

	public void setPlayerControlsEnabled(boolean playerControlsEnabled) {
		firePropertyChange(PLAYER_CONTROLS_ENABLED, this.playerControlsEnabled, this.playerControlsEnabled = playerControlsEnabled);
		// enabled capturer controls according to state of player controls
		boolean isFrontmostCapturer = frontMostComponent instanceof VideoCapturer;
		firePropertyChange(CAPTURER_CONTROLS_ENABLED, this.capturerControlsEnabled, this.capturerControlsEnabled = !playerControlsEnabled && isFrontmostCapturer);
	}

	public void capturersToFront() {
		// TODO flickering??
		for (VideoCapturer capturer : capturers) {
			capturer.toFront();
		}
	}

	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public DaoService getDaoService() {
		return daoService;
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}

	public AnalysisTablePanel getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	private class WindowStateChangeListener extends AppWindowWrapperListener {

		//TODO this can be coded better.. can lead to memory leaks..
		private VideoComponent enclosingWrapper;
		
		public WindowStateChangeListener(VideoComponent enclosingWrapper) {
			this.enclosingWrapper = enclosingWrapper;
		}

		public VideoComponent getEnclosingWrapper() {
			return enclosingWrapper;
		}

		public void appWindowGainedFocus(AWTEvent e) {
			enableVideoComponentControls(getEnclosingWrapper(), true);
		}

		public void appWindowActivated(AWTEvent e) {
			enableVideoComponentControls(getEnclosingWrapper(), true);
		}

/*		public void appWindowDeactivated(AWTEvent e) {
			enableVideoComponentControls(null, false);
		}

		public void appWindowClosed(AWTEvent e) {
			enableVideoComponentControls(null, false);
		}
*/
	}

}

