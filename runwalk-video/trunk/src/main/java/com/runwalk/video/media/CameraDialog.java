package com.runwalk.video.media;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
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

import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;

import com.google.common.collect.Iterables;
import com.runwalk.video.core.AppComponent;
import com.runwalk.video.core.SelfContained;
import com.runwalk.video.ui.WindowManager;
import com.runwalk.video.ui.actions.ApplicationActionConstants;

@SuppressWarnings("serial")
@AppComponent
public class CameraDialog extends JDialog implements ApplicationActionConstants {
	
	// bound class properties
	public static final String SELECTED_CAPTURER_NAME = "selectedCapturerName";

	private JComboBox capturerComboBox;

	private String selectedCapturerName;
	
	private final String defaultCapturerName;

	private final int capturerId;

	private String selectedMonitorId;

	private JPanel buttonPanel;
	
	private boolean cancelled = false;

	/**
	 * Create a dialog that allows the user to start a capture device. Selection notification will be done by firing {@link PropertyChangeEvent}s 
	 * to registered listeners. Note that the enableExitAction should only be enabled when there is no gui on screen yet. Otherwise one should call
	 * {@link SingleFrameApplication#exit()} to 'gracefully' shutdown.
	 * 
	 * @param parent The parent {@link Window} whose focusing behavior will be inherited. If null, then the exit action will be available in the {@link Dialog}
	 * @param actionMap An optional {@link ActionMap} which the {@link Dialog} can use to add extra {@link javax.swing.Action}s
	 * @param capturerId The unique id of the newly opened capturer. This will be used to determine the default monitor to run on
	 * @param defaultCapturer The name of the default selected capturer
	 */
	public CameraDialog(Window parent, ActionMap actionMap, int capturerId, String defaultCapturerName) {
		super(parent);
		setModal(true);
		this.capturerId = capturerId;
		this.defaultCapturerName = defaultCapturerName;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// is the application starting up or already started?
		final boolean enableExitAction = parent == null;
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (enableExitAction) {
					Runtime.getRuntime().exit(0);
				} else {
					dismissDialog();
				}
			}

		});
		setLayout(new MigLayout("fill, nogrid"));
		setTitle(getResourceMap().getString("captureDeviceDialog.title")); // NOI18N
		setResizable(false);

		JLabel captureDeviceLabel = new JLabel(getResourceMap().getString("captureDeviceLabel.text")); // NOI18N
		add(captureDeviceLabel, "wrap");

		capturerComboBox = new JComboBox();
		add(capturerComboBox, "wrap, grow");

		buttonPanel = new JPanel(new MigLayout("fill, gap 0, insets 0"));
		buttonPanel.setVisible(false);
		add(buttonPanel, "wrap, grow, hidemode 3");

		if (enableExitAction) {
			javax.swing.Action exitAction = getAction(EXIT_ACTION);
			JButton cancelButton = new JButton(exitAction); // NOI18N
			add(cancelButton, "grow");
		}
		JButton refreshButton = new JButton(getAction(REFRESH_CAPTURER_ACTION)); // NOI18N
		add(refreshButton, "align right");
		// add some extra actions to configure the capture device
		addAction(SHOW_CAMERA_SETTINGS_ACTION, actionMap, true);
		addAction(SHOW_CAPTURER_SETTINGS_ACTION, actionMap);
		JButton okButton = new JButton(getAction(DISMISS_DIALOG_ACTION));
		add(okButton, "align right");
		getRootPane().setDefaultButton(okButton);
		// populate combobox with the capture device list
		capturerComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				String capturerName = source.getSelectedItem().toString();
				setSelectedCapturerName(capturerName);
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

	public void setSelectedCapturerName(String selectedCapturerName) {
		firePropertyChange(SELECTED_CAPTURER_NAME, this.selectedCapturerName, this.selectedCapturerName = selectedCapturerName);
	}

	public String getSelectedCapturerName() {
		return selectedCapturerName;
	}
	
	public void dismissDialog() {
		dismissDialog(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "cancelled"));
	}

	@Action
	public void dismissDialog(ActionEvent actionEvent) {
		setCancelled(actionEvent.getActionCommand().equals("cancelled"));
		setVisible(false);
		// release native screen resources
		dispose();
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	/**
	 * This method refreshes the list with connected capture devices 
	 * and displaying devices. The layout of this dialog will be changed accordingly.
	 */
	@Action
	public boolean refreshCapturers() {
		// refresh capture devices by querying the capturer implementation for uninitialized capture devices
		Collection<String> capturerNames = VideoCapturerFactory.getInstance().getCapturerNames();
		// return if no capturers available
		if (capturerNames.isEmpty()) {
			JOptionPane.showMessageDialog(getParent(), getResourceMap().getString("refreshCapturers.errorDialog.message"), 
					getResourceMap().getString("refreshCapturers.errorDialog.title"), JOptionPane.ERROR_MESSAGE);
			dismissDialog();
			return false;
		}
		// add the capturers to the gui
		addCapturers(capturerNames);
		// add some extra gui elements depending on the number of connected monitors
		addMonitors();
		return true;
	}
	
	/**
	 * Add the available capturers to the {@link Dialog}.
	 * 
	 * @param capturerNames The names of the available capturers
	 */
	private void addCapturers(Collection<String> capturerNames) {
		String[] captureDevicesArray = Iterables.toArray(capturerNames, String.class);
		capturerComboBox.setModel(new DefaultComboBoxModel(captureDevicesArray));
		// determine the default capturer name as the passed name if available, otherwise use the default combobox model selection
		String defaultCapturerName = capturerComboBox.getSelectedItem().toString();
		// retain the previous selection if there was one. Otherwise use the default selected capturer name
		if (selectedCapturerName == null && this.defaultCapturerName != null && 
				capturerNames.contains(this.defaultCapturerName)) {
			defaultCapturerName = this.defaultCapturerName;
		} else if (selectedCapturerName != null) {
			defaultCapturerName = selectedCapturerName;
		}
		// set the selected item on the combobox
		capturerComboBox.setSelectedItem(defaultCapturerName);
		// make another call to the setter here to make sure the selected capturer will be started
		setSelectedCapturerName(defaultCapturerName);
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
			int defaultMonitorId = WindowManager.getDefaultMonitorId(graphicsDevices.length, capturerId);
			for (GraphicsDevice graphicsDevice : graphicsDevices) {
				String monitorIdString  = graphicsDevice.getIDstring();
				DisplayMode displayMode = graphicsDevice.getDisplayMode();
				final Rectangle position = graphicsDevice.getDefaultConfiguration().getBounds();
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
