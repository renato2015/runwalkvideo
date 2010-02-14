package com.runwalk.video.gui.media;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import com.runwalk.video.gui.MyInternalFrame;
import com.runwalk.video.util.AppUtil;

public class MediaControls extends MyInternalFrame implements PropertyChangeListener {
	
	public static final String STOP_ENABLED = "stopEnabled";

	public static final String RECORDING_ENABLED = "recordingEnabled";

	public static final String CONTROLS_ENABLED = "controlsEnabled";

	private static final String CONTROLS_DISABLED = "controlsDisabled";
	
	private JLabel time;
	private JSlider scroll;

	private VideoPlayer player;
	private VideoRecorder recorder;
	
	private Boolean recordingSelected = false;
	private boolean recordingEnabled, controlsEnabled, stopEnabled;

	private JPanel buttonPanel;

	private AbstractButton playButton;
	
	public MediaControls() {
		super("Media bediening", false);

		BindingGroup bindingGroup = new BindingGroup();
		BeanProperty<MediaControls, Boolean> controlsEnabled = BeanProperty.create("controlsEnabled");
//		ELProperty<PlayerInternalFrame, Boolean> controlsDisabled = ELProperty.create("${!controlsEnabled}");
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
		enabledBinding = Bindings.createAutoBinding(UpdateStrategy.READ, this, controlsEnabled, getSlider(), sliderEnabled);
		bindingGroup.addBinding(enabledBinding);
		bindingGroup.bind();
		clearLabels();

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

		time = new JLabel("00:00.000 / 00:00.000");
		
		JPanel playerPanel = new JPanel();
		playerPanel.setBorder(new EmptyBorder(new Insets(10,10,0,10)));
		playerPanel.setLayout(new BorderLayout());
		playerPanel.add(getSlider(), BorderLayout.NORTH);
		playerPanel.add(time, BorderLayout.EAST);
		playerPanel.add(buttonPanel, BorderLayout.CENTER);
		
		getComponent().add(playerPanel);
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
		//TODO bindings zijn niet meer nodig.. acties nemen dit over.
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
		recorder.toggleFullscreen(null);
	}
	
	@Action(enabledProperty=STOP_ENABLED)
	public void stop() {
		if (recorder.isRecording()) {
			stopRecording();
		} else if (player.isActive()) {
			player.stop();
			getSlider().setValue(0);
		}
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
	public void play() {
		if (player.togglePlay()) {
			getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
			setStopEnabled(true);
		}
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
	public void increaseVolume() {
		player.increaseVolume();
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
	public void decreaseVolume() {
		player.decreaseVolume();
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
	public void mute() {
		player.mute();
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
	public void slower() {
		player.backward();
		getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
	public void faster() {
		player.forward();
		getApplication().showMessage("Afspelen aan "+ player.getPlayRate() + "x gestart.");
	}
	
	@Action(enabledProperty=CONTROLS_DISABLED)
	public void showCaptureSettings() {
		recorder.showCaptureSettings();
	}
	
	@Action(enabledProperty=CONTROLS_DISABLED)
	public void showCameraSettings() {
		recorder.showCameraSettings();
	}

	@Action(enabledProperty=CONTROLS_ENABLED)
	public void nextSnapshot() {
		player.nextSnapshot();
	}
	
	@Action(enabledProperty=CONTROLS_ENABLED)
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
		boolean isRecordingEnabled = recordSelected && recorder.isActive() && recordingSelected;
		this.firePropertyChange(RECORDING_ENABLED, this.recordingEnabled, this.recordingEnabled = isRecordingEnabled);
		if (isRecordingEnabled()) {
			recorder.toFront();
		}
	}

	public boolean isRecordingEnabled() {
		return recordingEnabled;
	}

	public void setControlsEnabled(boolean enable) {
		if (enable) {
			getSlider().setValue(0);
			time.setText("00:00.000 / 00:00.000");
			clearLabels();
		} else if (player.isActive()) {
			updateTimeStamps();
			addLabels(player.getRecording());
		} else {
			enable = !enable;
		}
		firePropertyChange(CONTROLS_DISABLED, !this.controlsEnabled, enable);
		firePropertyChange(CONTROLS_ENABLED, this.controlsEnabled, this.controlsEnabled = !enable);
		//TODO dit kunt ge binden!
		setStopEnabled(!enable);
		setRecordingEnabled(enable);
	}
	
	public boolean isControlsEnabled() {
		return controlsEnabled;
	}
	
	public boolean isControlsDisabled() {
		return !controlsEnabled;
	}

	public void setStopEnabled(boolean stopEnabled) {
		this.firePropertyChange(STOP_ENABLED, this.stopEnabled, this.stopEnabled = stopEnabled);
	}

	public boolean isStopEnabled() {
		return this.stopEnabled;
	}

	public void startCapturer() {
		if (recorder == null) {
			recorder = new VideoRecorder(this);
			//TODO eventueel een dialoog om het scherm en fullscreen of niet te kiezen..
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			recorder.toggleFullscreen(gs[1]);
			recorder.getFullscreenFrame().addWindowListener(new WindowAdapter() {

				@Override
				public void windowGainedFocus(WindowEvent e) {
					if (player != null && player.isPlaying()) {
						player.stop();
					}
					if (!recorder.isRecording()) {
						setControlsEnabled(true);
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {
					if (player != null && player.isPlaying()) {
						player.stop();
					}
					if (!recorder.isRecording()) {
						setControlsEnabled(true);
					}
					getComponent().setTitle("Media Controls > " + recorder.getName());
				}
			});
		}
		recorder.toFront();
	}

	@Action(enabledProperty=RECORDING_ENABLED)
	public void record() {
		recorder.toFront();
		Analysis analysis = RunwalkVideoApp.getApplication().getSelectedAnalysis();
		if (analysis != null) {
			//TODO kijk of die null nog nodig is!
			Recording recording = analysis.getRecording() != null ? analysis.getRecording() : analysis.createRecording();
			recorder.startRecording(recording);
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
		setStopEnabled(false);
		setRecordingEnabled(false);
		recorder.stopRecording();
		getApplication().showMessage("Opname voor " + recorder.getRecording().getVideoFileName() + " voltooid.");
		getApplication().getStatusPanel().setIndeterminate(false);
		getApplication().setSaveNeeded(true);
		getApplication().getAnalysisOverviewTable().setCompressionEnabled(true);
	}

	@Action(enabledProperty=CONTROLS_ENABLED)
	public void makeSnapshot() {
		int position = player.makeSnapshot();
		getSlider().getLabelTable().put(getSliderPosition(position), new JLabel("*"));
		getSlider().updateUI();
		getSlider().revalidate();
		getApplication().showMessage("Snapshot genomen op " + 
				AppUtil.formatDate(new Date(position), new SimpleDateFormat("mm:ss.SSS"))); 
		getApplication().setSaveNeeded(true);
	}

	public void playFile(final Recording recording) {
		if (player == null) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();
			player = new VideoPlayer(this, recording);
			player.toggleFullscreen(gs[1]);
			player.getFullscreenFrame().addWindowListener(new WindowAdapter() {

				@Override
				public void windowGainedFocus(WindowEvent e) {
					if (recorder.isRecording()) {
						Toolkit.getDefaultToolkit().beep();
						stopRecording();
						JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
								"Opnemen kan enkel met videoscherm open!", "Opname beëindigd", JOptionPane.WARNING_MESSAGE);
					}
					if (!player.isPlaying() && player.isActive()) {
						setControlsEnabled(false);
					}
				}

				@Override
				public void windowActivated(WindowEvent e) {
					if (!player.isPlaying()) {
						setControlsEnabled(false);
					}
					getComponent().setTitle("Media Controls > " + player.getName() + " > " + recording.getVideoFileName());
				}
				
				@Override
				public void windowDeactivated(WindowEvent e) {
					setControlsEnabled(false);
				}

				@Override
				public void windowClosed(WindowEvent e) {
					setControlsEnabled(false);
				}

			});
		} else {
			player.loadFile(recording);
		}
		player.toFront();
		getSlider().setValue(0);
		addLabels(recording);
	}

	private Hashtable<Integer, JLabel> createLabelTable() {
		Hashtable<Integer, JLabel> table = new Hashtable<Integer, JLabel>();
		table.put(getSlider().getMinimum(), new JLabel("|"));
		table.put(getSlider().getMaximum(), new JLabel("|"));
		return table;
	}
	
	public void clearLabels() {
		getSlider().setLabelTable(createLabelTable());
	}

	public void addLabels(Recording recordin) {
		Hashtable<Integer, JLabel> table = createLabelTable();
		JLabel asterisk = new JLabel("*");
		for (Keyframe e:recordin.getKeyframes()) {
			table.put(getSliderPosition(e.getPosition()), asterisk);
		}
		getSlider().setLabelTable(table);
		getSlider().updateUI();
		getSlider().revalidate();
	}

	private void updateTimeStamps() {
		int position = player.getPosition();
		getSlider().setValue(getSliderPosition(position));
		String elapsedTime = AppUtil.formatDate(new Date(position), new SimpleDateFormat("mm:ss.SSS"));
		String totalTime = AppUtil.formatDate(new Date(player.getDuration()), new SimpleDateFormat("mm:ss.SSS"));
		time.setText(elapsedTime + " / " + totalTime);
	}

	private void setSlider(JSlider scroll) {
		this.scroll = scroll;
	}

	private JSlider getSlider() {
		return scroll;
	}
	
	private int getSliderPosition(int position) {
		return (int) ((double) 1000 *  position / player.getDuration());
	}
	
	public void disposeGraphs() {
		if (recorder != null) {
			recorder.disposeFiltergraph();
		}
		if (player != null) {
			player.disposeFiltergraph();
		}
	}


	public void propertyChange(PropertyChangeEvent evt) {
//		getLogger().debug("PropertyChangeEvent fired:" + evt.getSource() +  " name:" + evt.getPropertyName() + " value:" + evt.getNewValue());
		if (evt.getPropertyName().equals(VideoPlayer.POSITION)) {
			//			updateTimeStamps((Integer) evt.getNewValue());
			updateTimeStamps();
			//			getSlider().setValue(getSliderPosition((Integer) evt.getNewValue()));
			getLogger().debug("Position changed :" + evt.getNewValue());
		} else if (evt.getPropertyName().equals(VideoPlayer.PLAYING)) {
			playButton.setSelected((Boolean) evt.getNewValue());
		}
		//		if (engine.isPlaying()) {
		//		 if (DSJUtils.getEventType(evt) == DSMovie.FRAME_NOTIFY) {
		//				if (engine.getPosition() == 0) {
		//					engine.stop();
		//				}
		//				updateTimeStamps(DSJUtils.getEventValue_int(evt));
		////				updateTimeStamps((Integer) evt.getOldValue());
		//			}
		//		}

	}

	public void captureFrameToFront() {
		recorder.toFront();
	}
	
}

