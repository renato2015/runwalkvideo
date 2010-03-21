package com.runwalk.video.gui.media;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import org.jdesktop.application.Action;
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
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
public class MediaControls extends AppInternalFrame implements PropertyChangeListener {

	public static final String STOP_ENABLED = "stopEnabled";

	public static final String RECORDING_ENABLED = "recordingEnabled";

	public static final String PLAYING_ENABLED = "playingEnabled";

	private static final String PLAYING_DISABLED = "playingDisabled";

	private JLabel time;
	private JSlider scroll;

	private VideoPlayer player;
	private VideoCapturer capturer;

	private Boolean recordingSelected = false;
	private boolean recordingEnabled, playingEnabled, stopEnabled;

	private JPanel buttonPanel;

	private AbstractButton playButton;

	private Object playingDisabled;

	public MediaControls() {
		super("Media controls", false);

		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<MediaControls, Boolean> playingEnabled = BeanProperty.create(PLAYING_ENABLED);
		Binding<?, Boolean, ?, Boolean> enabledBinding = null;

		ELProperty<JTable, Boolean> isSelected = ELProperty.create("${!selectedElement.recording.recorded}");
		BeanProperty<MediaControls, Boolean> recordingEnabled = BeanProperty.create(RECORDING_ENABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, getApplication().getAnalysisTablePanel().getTable(), isSelected, this, recordingEnabled);
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

		buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(620,40));
		buttonPanel.setLayout(new java.awt.FlowLayout());

		createJButton("previousSnapshot"); 
		createJButton("slower"); 
		createJButton("record");

		createJButton("stop"); 
		playButton = createJToggleButton("play", "play.Action.pressedIcon"); 
		createJButton("faster"); 
		createJButton("nextSnapshot"); 
		createJButton("makeSnapshot"); 
		createJButton("decreaseVolume"); 
		createJButton("increaseVolume"); 
		createJToggleButton("mute", "mute.Action.pressedIcon"); 
		createJButton("showCaptureSettings"); 
		createJButton("showCameraSettings"); 
		createJButton("fullScreen"); 

		setSlider(new JSlider(JSlider.HORIZONTAL, 0, 1000, 0));
		getSlider().setPaintTicks(false);
		getSlider().setPaintLabels(true);
		getSlider().setSnapToTicks(false);
		getSlider().setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		BeanProperty<JSlider, Boolean> sliderEnabled = BeanProperty.create("enabled");
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, playingEnabled, getSlider(), sliderEnabled);
		bindingGroup.addBinding(enabledBinding);

		ELProperty<MediaControls, Boolean> notPlayingEnabled = ELProperty.create("${!" + PLAYING_ENABLED + "}");
		BeanProperty<MediaControls, Boolean> playingDisabled = BeanProperty.create(PLAYING_DISABLED);
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, notPlayingEnabled, this, playingDisabled);
		bindingGroup.addBinding(enabledBinding);
		bindingGroup.bind();

		// Listener for the scroll
		getSlider().addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (player != null && player.isActive()) {
					double pos = getSlider().getValue() * player.getDuration() / 1000;
					getLogger().debug("Slide stateChanged : " + (int) pos);
					player.setPosition((int) pos);
				}
			}
		});

		time = new JLabel();
		clearStatusInfo();

		JPanel playerPanel = new JPanel();
		playerPanel.setBorder(new EmptyBorder(new Insets(10,10,0,10)));
		playerPanel.setLayout(new BorderLayout());
		playerPanel.add(getSlider(), BorderLayout.NORTH);
		playerPanel.add(time, BorderLayout.EAST);
		playerPanel.add(buttonPanel, BorderLayout.CENTER);

		add(playerPanel);
		//		bindingGroup.bind();
	}

	private AbstractButton createJToggleButton(String actionName, String iconResourceName) {
		AbstractButton button = createButton(actionName, true);
		button.setSelectedIcon(getResourceMap().getIcon(iconResourceName));
		return button;
	}

	private AbstractButton createJButton(String actionName) {
		return createButton(actionName, false);
	}

	private AbstractButton createButton(String actionName, boolean toggleButton) {
		javax.swing.Action action = getAction(actionName);
		AbstractButton button = toggleButton ? new JToggleButton(action) : new JButton(action);
		button.setMargin(new Insets(0, 0, 0, 0));
		//		BeanProperty<AbstractButton, Boolean> buttonEnabled = BeanProperty.create("enabled");
		//		Binding<?, Boolean, ?, Boolean> enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, sourceProperty, button, buttonEnabled);
		//		bindingGroup.addBinding(enabledBinding);
		buttonPanel.add(button);
		return button;
	}

	@Action
	public void fullScreen() { 
		capturer.toggleFullscreen(null);
	}

	@Action(enabledProperty=STOP_ENABLED)
	public void stop() {
		if (capturer.isRecording()) {
			stopRecording();
			setStopEnabled(false);
			setRecordingEnabled(false);
		} else {
			player.stop();
			setStatusInfo(0, player.getDuration());
			setStopEnabled(false);
		}
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void play() {
		if (player.togglePlay()) {
			getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
			setStopEnabled(true);
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
		player.backward();
		getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void faster() {
		player.forward();
		getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
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
	 * This properties are changed in two different situations:
	 * 
	 * <ul>
	 * <li>When an {@link Analysis} is selected in the table then we have to perform a check on the activity of the capture graph here, too</li>
	 * <li>When the capture graph is activated by making it the frontmost window</li>
	 * </ul>
	 * 
	 * @return Whether recording is enabled or not
	 */
	public void setRecordingEnabled(boolean recordSelected) {
		boolean isRecordingEnabled = recordSelected && capturer.isActive() && recordingSelected && !capturer.isRecording();
		this.firePropertyChange(RECORDING_ENABLED, this.recordingEnabled, this.recordingEnabled = isRecordingEnabled);
		if (isRecordingEnabled()) {
			setPlayingEnabled(false);
			capturer.toFront();
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
		return this.stopEnabled;
	}

	public void startCapturer() {
		if (capturer == null) {
			capturer = VideoCapturer.createInstance(this);
			//TODO eventueel een dialoog om het scherm en fullscreen of niet te kiezen..
		}
		capturer.toFront();
	}

	@Action(enabledProperty=RECORDING_ENABLED)
	public void record() {
		capturer.toFront();
		Analysis analysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
		if (analysis != null) {
			//TODO kijk of die null nog nodig is!
			Recording recording = analysis.getRecording() != null ? analysis.getRecording() : analysis.createRecording();
			capturer.startRecording(recording);
			getApplication().getStatusPanel().setIndeterminate(true);
			//TODO zoek uit wat er met die selectedProperty mogelijk is
			setRecordingEnabled(false);
			setStopEnabled(true);
			getApplication().showMessage("Opname voor " + analysis.getClient().getName() + " " + 
					analysis.getClient().getFirstname() + " gestart..");
			getApplication().setSaveNeeded(true);
		}
	}

	public void stopRecording() {
		capturer.stopRecording();
		getApplication().showMessage("Opname voor " + capturer.getRecording().getVideoFileName() + " voltooid.");
		getApplication().getStatusPanel().setIndeterminate(false);
		getApplication().setSaveNeeded(true);
		getApplication().getAnalysisOverviewTable().setCompressionEnabled(true);
	}

	@Action(enabledProperty=PLAYING_ENABLED)
	public void makeSnapshot() {
		int position = player.makeSnapshot();
		int sliderPosition = getSliderPosition(position);
		getSlider().getLabelTable().put(sliderPosition, new JLabel("*"));
		getSlider().updateUI();
		getSlider().revalidate();
		getApplication().showMessage("Snapshot genomen op " + 
				AppUtil.formatDate(new Date(position), new SimpleDateFormat("mm:ss.SSS"))); 
		getApplication().setSaveNeeded(true);
	}

	public void playFile(final Recording recording) {
		try {
			if (player == null) {
				player = VideoPlayer.createInstance(this, recording);
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
		String elapsedTime = AppUtil.formatDate(new Date(position), new SimpleDateFormat("mm:ss.SSS"));
		String totalTime = AppUtil.formatDate(new Date(duration), new SimpleDateFormat("mm:ss.SSS"));
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
		if (evt.getPropertyName().equals(VideoPlayer.PLAYING)) {
			Boolean playing = (Boolean) newValue;
			playButton.setSelected(playing);
		} else if (evt.getPropertyName().equals(VideoCapturer.TIME_RECORDING)) {
			Long timeRecorded = (Long) newValue;
			VideoCapturer capturer = (VideoCapturer) evt.getSource();
			if (this.capturer.equals(capturer)) {
				updateTimeStamps(timeRecorded, 0);
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
		} else if (evt.getPropertyName().equals(VideoComponent.CONTROLS_ENABLED)) {
			Boolean enabled = (Boolean) newValue;
			VideoComponent component = (VideoComponent) evt.getSource();
			//a player or capturer is requesting the focus
			if (enabled && !capturer.isRecording()) {
				StringBuilder title = new StringBuilder(getName() + " > " + component.getTitle());
				if (component instanceof VideoCapturer) {
					clearStatusInfo();
					setRecordingEnabled(true);
					setPlayingEnabled(false);
					setStopEnabled(false);
					if (player != null) {
						if (player.isPlaying()) {
							player.stop();
						}
						player.setControlsEnabled(false);
					}
				} else {
					setRecordingEnabled(false);
					setStopEnabled(false);
					setPlayingEnabled(true);
					setStatusInfo(player.getRecording(), 0, player.getDuration());
					capturer.setControlsEnabled(false);
					title.append(" > " ).append(player.getRecording().getVideoFileName());
				}
				setTitle(title.toString());
			} else {
				component.setControlsEnabled(false);
			}
		}

		//DSJ fires the following event for notifying that playing has stoppped..
		//DSJUtils.getEventType(evt) == DSMovie.FRAME_NOTIFY
	}

	public void setPlayingEnabled(boolean playingEnabled) {
		firePropertyChange(PLAYING_ENABLED, this.playingEnabled, this.playingEnabled = playingEnabled);
	}

	public void setPlayingDisabled(boolean playingDisabled) {
		firePropertyChange(PLAYING_DISABLED, this.playingDisabled, this.playingDisabled = playingDisabled);
	}

	public void captureFrameToFront() {
		capturer.toFront();
	}

}

