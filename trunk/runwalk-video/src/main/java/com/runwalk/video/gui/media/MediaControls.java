package com.runwalk.video.gui.media;

import java.awt.AWTEvent;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Hashtable;

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

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.AppInternalFrame;
import com.runwalk.video.gui.AppWindowWrapper;
import com.runwalk.video.gui.media.VideoComponent.State;
import com.runwalk.video.gui.panels.AnalysisTablePanel;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class MediaControls extends AppInternalFrame implements PropertyChangeListener {

	private static final String FULL_SCREEN_ENABLED = "fullScreenEnabled";
	public static final String STOP_ENABLED = "stopEnabled";
	public static final String RECORDING_ENABLED = "recordingEnabled";
	public static final String PLAYING_ENABLED = "playingEnabled";
	private static final String PLAYING_DISABLED = "playingDisabled";

	private Boolean recordingSelected = false;
	private boolean recordingEnabled, playingEnabled, stopEnabled, playingDisabled, fullScreenEnabled;

	private JLabel time;
	private JSlider scroll;
	private AbstractButton playButton;

	private VideoPlayer player;
	private VideoCapturer capturer;
	private VideoComponent frontmostComponent;

	public MediaControls() {
		super("Media controls", false);
		setLayout(new MigLayout("insets 10 10 0 10, nogrid, fill"));

		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<MediaControls, Boolean> playingEnabled = BeanProperty.create(PLAYING_ENABLED);
		Binding<?, Boolean, ?, Boolean> enabledBinding = null;

		ELProperty<AnalysisTablePanel, Boolean> isSelected = ELProperty.create("${!selectedItem.recording.recorded}");
		BeanProperty<MediaControls, Boolean> recordingEnabled = BeanProperty.create(RECORDING_ENABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getApplication().getAnalysisTablePanel(), isSelected, this, recordingEnabled);
		enabledBinding.addBindingListener(new AbstractBindingListener() {

			@Override
			@SuppressWarnings("unchecked")
			public void sourceChanged(Binding binding, PropertyStateEvent event) {
				recordingSelected = (Boolean) binding.getSourceValueForTarget().getValue();
			}			

		});
		enabledBinding.setSourceUnreadableValue(false);
		enabledBinding.setTargetNullValue(false);
		bindingGroup.addBinding(enabledBinding);
		
		ELProperty<MediaControls, Boolean> notPlayingEnabled = ELProperty.create("${!" + PLAYING_ENABLED + "}");
		BeanProperty<MediaControls, Boolean> playingDisabled = BeanProperty.create(PLAYING_DISABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, notPlayingEnabled, this, playingDisabled);
		bindingGroup.addBinding(enabledBinding);
		bindingGroup.bind();
		
		setSlider(new JSlider(JSlider.HORIZONTAL, 0, 1000, 0));
		getSlider().setPaintTicks(false);
		getSlider().setPaintLabels(true);
		getSlider().setSnapToTicks(false);
		BeanProperty<JSlider, Boolean> sliderEnabled = BeanProperty.create("enabled");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, playingEnabled, getSlider(), sliderEnabled);
		bindingGroup.addBinding(enabledBinding);
		
		// Listener for the scroll
		getSlider().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (player != null && player.isActive()) {
					int position = getSlider().getValue() * player.getDuration() / 1000;
					getLogger().debug("Slide stateChanged : " + position);
					player.setPosition(position);
				}
			}
		});
		add(getSlider(), "wrap, grow, gapbottom 10");
		
		//add control buttons
		createJButton("previousSnapshot"); 
		createJButton("slower"); 
		createJButton("record");

		createJButton("stop"); 
		playButton = createJToggleButton("togglePlay", "togglePlay.Action.pressedIcon"); 
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
		//		BeanProperty<AbstractButton, Boolean> buttonEnabled = BeanProperty.create("enabled");
		//		Binding<?, Boolean, ?, Boolean> enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, sourceProperty, button, buttonEnabled);
		//		bindingGroup.addBinding(enabledBinding);
		add(button, "gap 0");
		return button;
	}

	//TODO kan dit eventueel met een proxy action??
	@Action(enabledProperty=FULL_SCREEN_ENABLED, block=BlockingScope.APPLICATION)
	public void fullScreen() { 
		frontmostComponent.toggleFullscreen();
	}

	@Action(enabledProperty=STOP_ENABLED)
	public void stop() {
		if (capturer.isRecording()) {
			stopRecording();
			setRecordingEnabled(false);
		} else {
			player.stop();
			getApplication().showMessage("Afspelen gestopt.");
			setStatusInfo(0, player.getDuration());
		}
		setStopEnabled(false);
	}

	@Action(enabledProperty=PLAYING_ENABLED )
	public void togglePlay() {
		if (player.isPlaying()) {
			player.pause();
			getApplication().showMessage("Afspelen gepauzeerd");
		} else {
			player.play();
			setStopEnabled(true);
			getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void increaseVolume() {
		player.increaseVolume();
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void decreaseVolume() {
		player.decreaseVolume();
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void mute() {
		player.mute();
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void slower() {
		if (player.backward()) {
			getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
		} else if (player.isPlaying()) {
			togglePlay();
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void faster() {
		if (!player.isPlaying()) {
			togglePlay();
		} else {
			player.forward();
			getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
		}
	}

	@Action(enabledProperty=PLAYING_DISABLED)
	public void showCaptureSettings() {
		capturer.showCaptureSettings();
	}

	@Action(enabledProperty=PLAYING_DISABLED)
	public void showCameraSettings() {
		capturer.showCameraSettings();
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void nextSnapshot() {
		player.nextSnapshot();
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void previousSnapshot() {
		player.previousSnapshot();
	}

	/**
	 * This property is under two different conditions:
	 * 
	 * <ul>
	 * <li>When an {@link Analysis} is selected in the table then we have to perform a check on the activity of the capture graph here, too</li>
	 * <li>When the capture graph is activated by making it the frontmost window</li>
	 * </ul>
	 * 
	 * @return Whether recording is enabled or not
	 */
	public void setRecordingEnabled(boolean recordSelected) {
		boolean capturerReady = capturer != null && capturer.isActive() && !capturer.isRecording();
		boolean isRecordingEnabled = capturerReady && recordSelected && recordingSelected;
		if (player != null) {
			isRecordingEnabled &= player.isIdle();
		}
		this.firePropertyChange(RECORDING_ENABLED, this.recordingEnabled, this.recordingEnabled = isRecordingEnabled);
		if (isRecordingEnabled()) {
//			setPlayingEnabled(false);
			captureFrameToFront();
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

	public void startCapturer() {
		if (capturer == null) {
			capturer = VideoCapturer.createInstance(this);
			capturer.addAppWindowWrapperListener(new WindowStateChangeListener(capturer));
			getApplication().createOrShowComponent(capturer);
			//TODO eventueel een dialoog om het scherm en fullscreen of niet te kiezen..
		}
		captureFrameToFront();
	}

	@Action(enabledProperty=RECORDING_ENABLED)
	public void record() {
//		capturer.toFront();
		Analysis analysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
		if (analysis != null) {
			Recording recording = analysis.getRecording();
			if (recording == null) {
				recording = new Recording(analysis);
				analysis.setRecording(recording);
			}
			capturer.startRecording(recording);
			getApplication().getStatusPanel().setIndeterminate(true);
			//TODO zoek uit wat er met die selectedProperty mogelijk is
			setRecordingEnabled(false);
			setStopEnabled(true);
			getApplication().showMessage("Opname voor " + analysis.getClient().getName() + " " + 
					analysis.getClient().getFirstname() + " gestart..");
		}
	}

	public void stopRecording() {
		capturer.stopRecording();
		getApplication().showMessage("Opname voor " + capturer.getRecording().getVideoFileName() + " voltooid.");
		getApplication().getStatusPanel().setIndeterminate(false);
		getApplication().getAnalysisOverviewTable().setCompressionEnabled(true);
	}

	@SuppressWarnings("unchecked")
	@Action(enabledProperty=PLAYING_ENABLED)
	public void makeSnapshot() {
		int position = player.makeSnapshot();
		int sliderPosition = getSliderPosition(position);
		getSlider().getLabelTable().put(sliderPosition, new JLabel("*"));
		getSlider().updateUI();
		getSlider().revalidate();
		getApplication().showMessage("Snapshot genomen op " + 
				AppUtil.formatDate(new Date(position), AppUtil.EXTENDED_DURATION_FORMATTER)); 
	}

	public void playFile(final Recording recording) {
		try {
			if (player == null) {
				player = VideoPlayer.createInstance(this, recording);
				player.addAppWindowWrapperListener(new WindowStateChangeListener(player));
				getApplication().createOrShowComponent(player);
			} else {
				player.loadFile(recording);
				setSliderLabels(recording);
			}
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Het bestand dat u probeerde te openen kon niet worden gevonden",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);
			getLogger().error(e);
		}
		player.toFront();
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
		if (player != null ) {
			result = (int) ((double) 1000 *  position / player.getDuration());
		}
		return result;
	}

	public void disposeGraphs() {
		if (capturer != null) {
			capturer.dispose();
		}
		if (player != null) {
			player.dispose();
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		Object newValue = evt.getNewValue();
		if (evt.getPropertyName().equals(VideoComponent.STATE)) {
			State state = (State) evt.getNewValue();
			boolean enabled = state == VideoComponent.State.IDLE && evt.getSource() == frontmostComponent;
			firePropertyChange(FULL_SCREEN_ENABLED, fullScreenEnabled, fullScreenEnabled = enabled );
			playButton.setSelected(state == State.PLAYING);
		} else if (evt.getPropertyName().equals(VideoCapturer.TIME_RECORDING)) {
			AppWindowWrapper capturer = (AppWindowWrapper) evt.getSource();
			Long timeRecorded = (Long) newValue;
			if (this.capturer.equals(capturer)) {
				updateTimeStamps(timeRecorded, 0);
				if (!isStopEnabled()) {
					getLogger().warn("Stop button was set to false. re-enabling..");
					setStopEnabled(true);
				}
			}
		} else if (evt.getPropertyName().equals(VideoPlayer.POSITION))  {
			Integer position = (Integer) newValue;
			VideoPlayer player = (VideoPlayer) evt.getSource();
			if (this.player.equals(player)) {
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
		if (frontmostComponent == null || enable && frontmostComponent.isIdle()) {
			StringBuilder title = new StringBuilder(getName() + " > " + component.getTitle());
			if (component instanceof VideoCapturer) {
				clearStatusInfo();
				setRecordingEnabled(true);
				setPlayingEnabled(false);
				setStopEnabled(false);
			} else {
				setRecordingEnabled(false);
				setPlayingEnabled(true);
				setStopEnabled(player.getPosition() > 0);
				setStatusInfo(player.getRecording(), player.getPosition(), player.getDuration());
				title.append(" > " ).append(player.getRecording().getVideoFileName());
			}
			setTitle(title.toString());
			firePropertyChange(FULL_SCREEN_ENABLED, fullScreenEnabled, fullScreenEnabled = component.isIdle());	
			frontmostComponent = component;
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
	}

	public void setPlayingDisabled(boolean playingDisabled) {
		firePropertyChange(PLAYING_DISABLED, this.playingDisabled, this.playingDisabled = playingDisabled);
	}

	public void captureFrameToFront() {
		if (capturer != null) {
			capturer.toFront();
		}
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

