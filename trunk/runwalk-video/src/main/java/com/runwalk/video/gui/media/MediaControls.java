package com.runwalk.video.gui.media;

import java.awt.AWTEvent;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;
import org.jdesktop.application.Task.BlockingScope;
import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.Binding.SyncFailure;

import com.google.common.collect.Lists;
import com.runwalk.video.VideoFileManager;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.media.VideoComponent.State;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class MediaControls extends AppInternalFrame implements PropertyChangeListener {

	private static final String FULL_SCREEN_ENABLED = VideoComponent.FULL_SCREEN_ENABLED;
	public static final String STOP_ENABLED = "stopEnabled";
	public static final String RECORDING_ENABLED = "recordingEnabled";
	public static final String PLAYING_ENABLED = "playingEnabled";
	private static final String PLAYING_DISABLED = "playingDisabled";

	private Boolean selectedRecordingRecordable = false;
	private boolean recordingEnabled, playingEnabled, stopEnabled, playingDisabled, fullScreenEnabled;

	private JLabel time;
	private JSlider scroll;
	private AbstractButton playButton;

	private List<VideoPlayer> players = Lists.newArrayList();
	private List<VideoCapturer> capturers = Lists.newArrayList();
	private VideoComponent frontMostComponent;
	private VideoPlayer frontMostPlayer;
	private VideoCapturer frontMostCapturer;
	
	private final VideoFileManager videoFileManager;
	private final AppSettings appSettings;
	private final AnalysisTablePanel analysisTablePanel;

	private boolean recording;
	
	public MediaControls(AnalysisTablePanel analysisTablePanel, AppSettings appSettings, VideoFileManager videoFileManager) {
		super("Media controls", false);
		this.videoFileManager = videoFileManager;
		this.appSettings = appSettings;
		this.analysisTablePanel = analysisTablePanel;
		
		setLayout(new MigLayout("insets 10 10 0 10, nogrid, fill"));
		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<MediaControls, Boolean> playingEnabled = BeanProperty.create(PLAYING_ENABLED);
		Binding<?, Boolean, ?, Boolean> enabledBinding = null;

		ELProperty<AnalysisTablePanel, Boolean> recorded = ELProperty.create("${rowSelected && !selectedItem.recorded}");
		BeanProperty<MediaControls, Boolean> recordingEnabled = BeanProperty.create(RECORDING_ENABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, analysisTablePanel, recorded, this, recordingEnabled);
		enabledBinding.addBindingListener(new AbstractBindingListener() {

			@Override
			@SuppressWarnings("unchecked")
			public void sourceChanged(Binding binding, PropertyStateEvent event) {
				selectedRecordingRecordable = (Boolean) binding.getSourceValueForTarget().getValue();
			}

			@Override
			public void synced(Binding binding) {
				// TODO Auto-generated method stub
				System.out.println("synced");
				super.synced(binding);
			}

			@Override
			public void syncFailed(Binding binding, SyncFailure failure) {
				// TODO Auto-generated method stub
				System.out.println("sync failed");
				super.syncFailed(binding, failure);
			}

			@Override
			public void targetChanged(Binding binding, PropertyStateEvent event) {
				System.out.println("target changed");
				super.targetChanged(binding, event);
			}

			@Override
			public void sourceEdited(Binding binding) {
				System.out.println("source edited");
				super.sourceEdited(binding);
			}

			@Override
			public void targetEdited(Binding binding) {
				System.out.println("target edited");
				super.targetEdited(binding);
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
	@Action(enabledProperty=FULL_SCREEN_ENABLED, block=BlockingScope.APPLICATION)
	public void fullScreen() { 
		frontMostComponent.toggleFullscreen();
	}

	@Action(enabledProperty=STOP_ENABLED)
	public void stop() {
		if (isRecording()) {
			stopRecording();
			setRecordingEnabled(false);
		} else {
			stopPlaying();
		}
		setStopEnabled(false);
	}

	@Action(enabledProperty=PLAYING_ENABLED )
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

	@Action(enabledProperty=PLAYING_ENABLED)
	public void increaseVolume() {
		for (VideoPlayer player : players) {
			player.increaseVolume();
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void decreaseVolume() {
		for (VideoPlayer player : players) {
			player.decreaseVolume();
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void mute() {
		for (VideoPlayer player : players) {
			player.mute();
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void slower() {
		for (VideoPlayer player : players) {
			if (!player.isPlaying()) {
				togglePlay();
			} else {
				float playRate = player.slower();
				// save play rate to settings
				getAppSettings().setPlayRate(playRate);
				getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
			} 
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
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
	@Action(enabledProperty=PLAYING_DISABLED)
	public void showCaptureSettings() {
		frontMostCapturer.showCapturerSettings();
	}

	@Action(enabledProperty=PLAYING_DISABLED)
	public void showCameraSettings() {
		frontMostCapturer.showCameraSettings();
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void nextSnapshot() {
		for (VideoPlayer player : players) {
			player.pauseIfPlaying();
			player.nextSnapshot();
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void previousSnapshot() {
		for (VideoPlayer player : players) {
			player.pauseIfPlaying();
			player.previousSnapshot();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Action(enabledProperty=PLAYING_ENABLED)
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
			AppUtil.persistEntity(snapshot);
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
	 * This property is changed two different conditions:
	 * 
	 * <ul>
	 * <li>When an {@link Analysis} is selected in the table then we have to perform a check on the activity of the capture graph here, too</li>
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
		boolean isRecordingEnabled = capturersReady && recordSelected && selectedRecordingRecordable && !isRecording();
		for (VideoPlayer player : players) {
			isRecordingEnabled &= player.isIdle();
		}
		this.firePropertyChange(RECORDING_ENABLED, this.recordingEnabled, this.recordingEnabled = isRecordingEnabled);
		if (isRecordingEnabled()) {
			setPlayingEnabled(false);
			capturersToFront();
		}
	}

	public boolean isRecordingEnabled() {
		return recordingEnabled;
	}

	public boolean isPlayingEnabled() {
		return playingEnabled;
	}

	public boolean isPlayingDisabled() {
		return !isPlayingEnabled();
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

	@Action(name = "Start Camera")
	public void startCapturer() {
		VideoCapturer capturer = VideoCapturer.createInstance(this);
		if (capturer != null) {
			capturer.addAppWindowWrapperListener(new WindowStateChangeListener(capturer));
			capturers.add(capturer);
			getApplication().createOrShowComponent(capturer);
			capturersToFront();
		}
	}

	@Action(enabledProperty=RECORDING_ENABLED)
	public void record() {
		Analysis analysis = getAnalysisTablePanel().getSelectedItem();
		if (analysis != null) {
			boolean isRecording = false;
			for (VideoCapturer capturer : capturers) {
				Recording recording = new Recording(analysis);
				// persist recording first, then add it to the analysis
				AppUtil.persistEntity(recording);
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
		for (VideoCapturer capturer : capturers) {
			capturer.stopRecording();
			getApplication().showMessage("Opnemen van " + capturer.getVideoFile().getName() + " voltooid.");
		}
		setRecording(false);
		// set this manually as such a specific propertyChangeEvent won't be fired
		selectedRecordingRecordable = false;
		getApplication().getStatusPanel().setIndeterminate(false);
		getApplication().getAnalysisOverviewTablePanel().setCompressionEnabled(true);
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
						player = players.get(recordingCount++);
						player.loadFile(recording, videoFile);
						getLogger().info("Videofile " + videoFile.getAbsolutePath() + " opened and ready for playback.");
						setSliderLabels(recording);
					} else {
						float playRate = getAppSettings().getPlayRate();
						player = VideoPlayer.createInstance(this, recording, videoFile, playRate);
						player.addAppWindowWrapperListener(new WindowStateChangeListener(player));
						getApplication().createOrShowComponent(player);
						players.add(player);
					} 
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null,
							"Het bestand dat u probeerde te openen kon niet worden gevonden",
							"Fout bij openen filmpje",
							JOptionPane.ERROR_MESSAGE);
					getLogger().error(e);
				}
				player.toFront();
			}
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

	private void setStatusInfo(long position, long duration) {
		setSliderPosition((int) position);
		updateTimeStamps(position, duration);
	}

	private void setStatusInfo(Recording recording, long position, long duration) {
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

	public void disposeGraphs() {
		for (VideoCapturer capturer : capturers) {
			capturer.dispose();
		}
		for (VideoPlayer player : players) {
			player.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		if (evt.getPropertyName().equals(VideoComponent.STATE)) {
			State state = (State) evt.getNewValue();
			boolean enabled = state == VideoComponent.State.IDLE && evt.getSource() == frontMostComponent;
			firePropertyChange(FULL_SCREEN_ENABLED, fullScreenEnabled, fullScreenEnabled = enabled );
			playButton.setSelected(state == State.PLAYING);
		} else if (evt.getPropertyName().equals(VideoCapturer.TIME_RECORDING)) {
			AppWindowWrapper capturer = (AppWindowWrapper) evt.getSource();
			Long timeRecorded = (Long) newValue;
			//only update status info if it is the fronmost capturer
			if (frontMostCapturer == capturer) {
				updateTimeStamps(timeRecorded, 0);
			}
		} else if (evt.getPropertyName().equals(VideoPlayer.POSITION))  {
			Integer position = (Integer) newValue;
			VideoPlayer player = (VideoPlayer) evt.getSource();
			//only update status info for the frontmost player
			if (frontMostPlayer == player) {
				if (position == 0) {
					stop();
				}
				setStatusInfo(position, player.getDuration());
			}
		}
		//DSJ fires the following event for notifying that playing has stoppped..
		//DSJUtils.getEventType(evt) == DSMovie.FRAME_NOTIFY
	}
	
	/**
	 * Enabling a {@link VideoComponent}'s controls is only possible when the frontmost component is idle,  otherwise the
	 * request will be ignored.
	 * 
	 * @param component The videoComponent to enable
	 * @param enable The enabled state
	 */
	private void enableVideoComponentControls(VideoComponent component, boolean enable) {
		//a player or capturer is requesting the focus.. this will only be given if the frontmost component is idle
		if (frontMostComponent == null || enable && frontMostComponent.isIdle()) {
			StringBuilder title = new StringBuilder(getName() + " > " + component.getTitle());
			if (component instanceof VideoCapturer) {
				frontMostCapturer = (VideoCapturer) component;
				clearStatusInfo();
				setRecordingEnabled(true);
				setPlayingEnabled(false);
				setStopEnabled(false);
			} else {
				frontMostPlayer = (VideoPlayer) component;
				setRecordingEnabled(false);
				setPlayingEnabled(true);
				setStopEnabled(frontMostPlayer.getPosition() > 0);
				setStatusInfo(frontMostPlayer.getRecording(), frontMostPlayer.getPosition(), frontMostPlayer.getDuration());
				title.append(" > " ).append(frontMostPlayer.getVideoFile().getName());
			}
			setTitle(title.toString());
			firePropertyChange(FULL_SCREEN_ENABLED, fullScreenEnabled, fullScreenEnabled = component.isFullScreenEnabled());	
			frontMostComponent = component;
		} /*else {
			if (isFrontmostVideoComponent(component)) {
				//disable all controls??
				setRecordingEnabled(false);
				setPlayingEnabled(false);
				setStopEnabled(false);
			}
		}*/
	}
	
//	private boolean isFrontmostVideoComponent(VideoComponent component) {
//		return frontmostComponent != null && frontmostComponent.equals(component);
//	}

	public void setPlayingEnabled(boolean playingEnabled) {
		firePropertyChange(PLAYING_ENABLED, this.playingEnabled, this.playingEnabled = playingEnabled);
		firePropertyChange(PLAYING_DISABLED, this.playingDisabled, this.playingDisabled = !playingEnabled);
	}

	public void capturersToFront() {
		for (VideoCapturer capturer : capturers) {
			capturer.toFront();
		}
	}
	
	public VideoFileManager getVideoFileManager() {
		return videoFileManager;
	}

	public AppSettings getAppSettings() {
		return appSettings;
	}
	
	public AnalysisTablePanel getAnalysisTablePanel() {
		return analysisTablePanel;
	}

	private class WindowStateChangeListener extends AppWindowWrapperListener {

		private VideoComponent enclosingWrapper;
		//TODO this can be coded better I guess!!.. can lead to memory leaks..
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
		
		/*public void appWindowDeactivated(AWTEvent e) {
			enableVideoComponentControls(getEnclosingWrapper(), false);
		}

		public void appWindowClosed(AWTEvent e) {
			enableVideoComponentControls(getEnclosingWrapper(), false);
		}*/
	}

}

