package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;

import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;

import com.google.common.collect.Iterables;
import com.runwalk.video.core.AppComponent;
import com.runwalk.video.core.SelfContained;
import com.runwalk.video.ui.WindowManager;
import com.runwalk.video.ui.actions.ApplicationActionConstants;

@SuppressWarnings("serial")
@AppComponent
public class VideoCapturerDialog extends JDialog implements ApplicationActionConstants {
	
	// bound class properties
	public static final String SELECTED_VIDEO_CAPTURER_NAME = "selectedVideoCapturerName";

	public static final String ABORTED = "aborted";

	private JComboBox videoCapturerComboBox;

	private String selectedVideoCapturerName;
	
	private final String defaultVideoCapturerName;

	private final int videoCapturerId;

	private String selectedMonitorId;

	private JPanel buttonPanel;
	
	private boolean aborted = false;

	/**
	 * Create a dialog that allows the user to start a capture device. Selection notification will be done by firing {@link PropertyChangeEvent}s 
	 * to registered listeners. Note that the enableExitAction should only be enabled when there is no gui on screen yet. Otherwise one should call
	 * {@link SingleFrameApplication#exit()} to 'gracefully' shutdown.
	 * 
	 * @param parent The parent {@link Window} whose focusing behavior will be inherited. If null, then the exit action will be available in the {@link Dialog}
	 * @param actionMap An optional {@link ActionMap} which the {@link Dialog} can use to add extra {@link javax.swing.Action}s
	 * @param videoCapturerId The unique id of the newly opened capturer. This will be used to determine the default monitor to run on
	 * @param defaultCapturer The name of the default selected capturer
	 */
	public VideoCapturerDialog(Window parent, ActionMap actionMap, int videoCapturerId, String defaultVideoCapturerName) {
		super(parent);
		setModal(true);
		this.videoCapturerId = videoCapturerId;
		this.defaultVideoCapturerName = defaultVideoCapturerName;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// is the application starting up or already started?
		final boolean enableQuitAction = parent == null;
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (enableQuitAction) {
					invokeAction(QUIT_ACTION, e.getSource());
				} else {
					dismissDialog();
				}
			}

		});
		setLayout(new MigLayout("fill, nogrid"));
		setTitle(getResourceMap().getString("videoCaptureDeviceDialog.title")); // NOI18N
		setResizable(false);

		JLabel videoCapturerDeviceLabel = new JLabel(getResourceMap().getString("videoCaptureDeviceLabel.text")); // NOI18N
		add(videoCapturerDeviceLabel, "wrap");

		videoCapturerComboBox = new JComboBox();
		add(videoCapturerComboBox, "wrap, grow");

		buttonPanel = new JPanel(new MigLayout("fill, gap 0, insets 0"));
		buttonPanel.setVisible(false);
		add(buttonPanel, "wrap, grow, hidemode 3");

		if (enableQuitAction) {
			javax.swing.Action quitAction = getAction(QUIT_ACTION);
			JButton quitButton = new JButton(quitAction); // NOI18N
			add(quitButton, "grow");
		}
		JButton refreshButton = new JButton(getAction(REFRESH_VIDEO_CAPTURERS_ACTION)); // NOI18N
		add(refreshButton, "align right");
		// add some extra actions to configure the capture device
		addAction(SHOW_CAMERA_SETTINGS_ACTION, actionMap, true);
		addAction(SHOW_CAPTURER_SETTINGS_ACTION, actionMap);
		JButton okButton = new JButton(getAction(DISMISS_DIALOG_ACTION));
		add(okButton, "align right");
		getRootPane().setDefaultButton(okButton);
		// populate combobox with the capture device list
		videoCapturerComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				String videoCapturerName = source.getSelectedItem().toString();
				setSelectedVideoCapturerName(videoCapturerName);
			}

		});
	}
	
	private void addAction(String actionName, ActionMap actionMap) {
		addAction(actionName, actionMap, false);
	}

	private void addAction(String actionName, ActionMap actionMap, boolean wrap) {
		if (actionMap != null) {
			final javax.swing.Action action = actionMap.get(actionName);
			if (action != null) {
				JButton button = new JButton(action);
				String wrapButton = wrap ? ", wrap" : "";
				add(button, "align right, grow" + wrapButton);
			}
		}
	}
	
	public void setSelectedVideoCapturerName(String selectedVideoCapturerName) {
		firePropertyChange(SELECTED_VIDEO_CAPTURER_NAME, this.selectedVideoCapturerName, this.selectedVideoCapturerName = selectedVideoCapturerName);
	}

	public String getSelectedCapturerName() {
		return selectedVideoCapturerName;
	}
	
	public void dismissDialog() {
		dismissDialog(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ABORTED));
	}

	@Action
	public void dismissDialog(ActionEvent actionEvent) {
		setAborted(actionEvent.getActionCommand().equals(ABORTED));
		setVisible(false);
		// release native screen resources
		dispose();
	}
	
	public void setAborted(boolean aborted) {
		firePropertyChange(ABORTED, this.aborted, this.aborted = aborted);
	}
	
	public boolean isAborted() {
		return aborted;
	}
	
	/**
	 * This method refreshes the list with connected capture devices 
	 * and displaying devices. The layout of this dialog will be changed accordingly.
	 * 
	 * The method will return <code>false</code> in case something goes wrong in the 
	 * initialization routines. If all is well, <code>true</code> will be returned.
	 * 
	 * @return <code>true</code> if initialization went well
	 */
	@Action
	public boolean refreshVideoCapturers() {
		boolean result = true;
		try {
			// refresh capture devices by querying the capturer implementation for uninitialized capture devices
			Collection<String> videoCapturerNames = VideoCapturerFactory.getInstance().getVideoCapturerNames();
			// return if no capturers available
			if (videoCapturerNames.isEmpty() && !isVisible()) {
				showErrorDialog();
			}
			// add the capturers to the gui
			addVideoCapturers(videoCapturerNames);
			// add some extra gui elements depending on the number of connected monitors
			addMonitors();
			pack();
			setLocationRelativeTo(null);
		} catch(Throwable exception) {
			// probably some native packages missing..
			Logger.getLogger(getClass()).error("Error while initializing capturers", exception);
			showErrorDialog();
			result = false;
		}
		return result;
	}
	
	private void showErrorDialog() {
		JOptionPane.showMessageDialog(getParent(), getResourceMap().getString("refreshVideoCapturers.errorDialog.text"), 
				getResourceMap().getString("refreshVideoCapturers.errorDialog.title"), JOptionPane.ERROR_MESSAGE);
		dismissDialog();
	}
	
	/**
	 * Add the available capturers to the {@link Dialog}.
	 * 
	 * @param videoCapturerNames The names of the available capturers
	 */
	private void addVideoCapturers(Collection<String> videoCapturerNames) {
		String[] captureDevicesArray = Iterables.toArray(videoCapturerNames, String.class);
		videoCapturerComboBox.setModel(new DefaultComboBoxModel(captureDevicesArray));
		// determine the default capturer name as the passed name if available, otherwise use the default combobox model selection
		String defaultVideoCapturerName = videoCapturerComboBox.getSelectedItem().toString();
		// retain the previous selection if there was one. Otherwise use the default selected capturer name
		if (selectedVideoCapturerName == null && this.defaultVideoCapturerName != null && 
				videoCapturerNames.contains(this.defaultVideoCapturerName)) {
			defaultVideoCapturerName = this.defaultVideoCapturerName;
		} else if (selectedVideoCapturerName != null) {
			defaultVideoCapturerName = selectedVideoCapturerName;
		}
		// set the selected item on the combobox
		videoCapturerComboBox.setSelectedItem(defaultVideoCapturerName);
		// make another call to the setter here to make sure the selected capturer will be started
		setSelectedVideoCapturerName(defaultVideoCapturerName);
	}
	
	/**
	 * Add extra monitor selection buttons, only if there are more than 1 connected.  
	 */
	public void addMonitors() {
		// get graphics environment
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		// let user choose on which screen to show the capturer, only if more than one is connected
		if (graphicsDevices.length > 1) {
			buttonPanel.removeAll();
			JLabel screenLabel = new JLabel(getResourceMap().getString("screenLabel.text"));
			buttonPanel.add(screenLabel, "wrap, grow, span");
			// create buttongroup for selecting monitor
			ButtonGroup screenButtonGroup = new ButtonGroup();
			// get the default monitor id for this capturer
			int defaultMonitorId = WindowManager.getDefaultMonitorId(graphicsDevices.length, videoCapturerId);
			for (GraphicsDevice graphicsDevice : graphicsDevices) {
				String monitorIdString  = graphicsDevice.getIDstring();
				DisplayMode displayMode = graphicsDevice.getDisplayMode();
				// final Rectangle position = graphicsDevice.getDefaultConfiguration().getBounds();
				String resolution = displayMode.getWidth() + "x" + displayMode.getHeight();
				JToggleButton button = new JToggleButton("<html><center>" + monitorIdString + "<br>" + resolution + "</center></html>");
				monitorIdString = monitorIdString.substring(monitorIdString.length() - 1);
				button.setActionCommand(monitorIdString);
				button.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						AbstractButton source = (AbstractButton) e.getSource();
						String monitorId = source.getActionCommand();
						// TODO pas position here instead of monitor id..
						firePropertyChange(SelfContained.MONITOR_ID, selectedMonitorId, selectedMonitorId = monitorId);
					}

				});
				button.setBackground(Color.WHITE);
				// set default screen selection
				int monitorId = Integer.parseInt(monitorIdString);
				if (defaultMonitorId == monitorId) {
					button.setSelected(true);
					screenButtonGroup.setSelected(button.getModel(), true);
				} else {
					screenButtonGroup.setSelected(button.getModel(), false);
				}
				screenButtonGroup.add(button);
				buttonPanel.add(button, "height 70!, width 70::, grow");
			}
			screenLabel.setVisible(true);
			buttonPanel.setVisible(true);
		}
	}

}
