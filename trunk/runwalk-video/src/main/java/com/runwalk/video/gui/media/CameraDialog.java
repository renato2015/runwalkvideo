package com.runwalk.video.gui.media;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;

import com.runwalk.video.gui.AppDialog;

@SuppressWarnings("serial")
public class CameraDialog extends AppDialog {

	public static final String CAPTURER_INITIALIZED = "capturerInitialized";
	public static final String REFRESH_CAPTURE_DEVICES = "refreshCaptureDevices";
	public static final String SELECTED_CAPTURE_DEVICE = "selectedCaptureDevice";
	private static final String SHOW_CAMERA_SETTINGS = "showCameraSettings";
	private static final String SHOW_CAPTURER_SETINGS = "showCapturerSettings";
	private static final String TOGGLE_PREVIEW = "togglePreview";
	private static final String DISMISS_DIALOG = "dismissDialog";

	private JComboBox captureDeviceComboBox;

	private String selectedCaptureDevice;
	
	private int capturerId;
	
	private boolean capturerInitialized;
	
	private String selectedMonitorId;

	private JPanel buttonPanel;

	/**
	 * Create a dialog that allows the user start a capture device. A {@link IVideoCapturer} implementation must be passed
	 * which it will use to query for the available capture devices. Selection notification will be done by firing {@link PropertyChangeEvent}s 
	 * to registered listeners. 
	 * 
	 * @param parent The parent {@link Frame} whose focusing behavior will be inherited
	 * @param actionMap An optional {@link ActionMap} which the {@link Dialog} can use to add extra {@link javax.swing.Action}s
	 * @param capturerImpl A {@link IVideoCapturer} implementation which will be queried for available capture devices 
	 * @param capturerId The unique id of the newly opened capturer. This will be used to determine the default monitor to run on
	 */
	public CameraDialog(Frame parent, ActionMap actionMap, int capturerId) {
		super(parent, true);
		this.capturerId = capturerId;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// is the application starting up or already started?
		final boolean isReady = getApplication().isReady();
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (isReady) {
					dismissDialog();
				} else {
					Runtime.getRuntime().exit(0);
				}
			}

		});
		setLayout(new MigLayout("fill, nogrid"));
		setTitle(getResourceMap().getString("captureDeviceDlg.title")); // NOI18N
		setResizable(false);

		JLabel captureDeviceLabel = new JLabel(getResourceMap().getString("captureDeviceLabel.text")); // NOI18N
		add(captureDeviceLabel, "wrap");

		captureDeviceComboBox = new JComboBox();
		add(captureDeviceComboBox, "wrap, grow");

		buttonPanel = new JPanel(new MigLayout("fill, gap 0, insets 0"));
		buttonPanel.setVisible(false);
		add(buttonPanel, "wrap, grow, hidemode 3");

		if (!isReady) {
			javax.swing.Action cancelAction = getApplication().getApplicationActionMap().get("exit");
			JButton cancelButton = new JButton(cancelAction); // NOI18N
			add(cancelButton, "grow");
		}
		JButton refreshButton = new JButton(getAction(REFRESH_CAPTURE_DEVICES)); // NOI18N
		add(refreshButton, "align right, grow");
		JButton initButton = new JButton(getAction(CAPTURER_INITIALIZED)); // NOI18N
		add(initButton, "grow, wrap");
		// add some extra actions to configure the capture device with
		addAction(SHOW_CAMERA_SETTINGS, actionMap);
		addAction(SHOW_CAPTURER_SETINGS, actionMap);
//		addAction(TOGGLE_PREVIEW, actionMap);
		JButton okButton = new JButton(getAction(DISMISS_DIALOG));
		add(okButton, "align right");
		getRootPane().setDefaultButton(okButton);
		// populate combobox with the capture device list
		captureDeviceComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				String captureDevice = source.getSelectedItem().toString();
				firePropertyChange(SELECTED_CAPTURE_DEVICE, selectedCaptureDevice, selectedCaptureDevice = captureDevice);
			}
			
		});
		pack();
		toFront();
	}
	
	private void addAction(String actionName, ActionMap actionMap) {
		if (actionMap != null) {
			final javax.swing.Action action = actionMap.get(actionName);
			if (action != null) {
				action.setEnabled(false);
				addPropertyChangeListener(new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						if (evt.getPropertyName().equals(CAPTURER_INITIALIZED)) {
							action.setEnabled((Boolean) evt.getNewValue());
						}
					}
					
				});
				JButton chooseCapturerSettings = new JButton(action);
				add(chooseCapturerSettings, "align right");
			}
		}
	}
	
	@Action
	public void initializeCamera() {
		firePropertyChange(CAPTURER_INITIALIZED, capturerInitialized, capturerInitialized = true);
		getAction("initializeCamera").setEnabled(false);
	}
	
	@Action
	public void dismissDialog() {
		setVisible(false);
		// release native screen resources
		dispose();
		// initialize if that didn't already happen
		initializeCamera();
	}

	/**
	 * This method refreshes the list with connected capture devices 
	 * and displaying devices. The layout of this dialog will be changed accordingly.
	 */
	@Action
	public void refreshCaptureDevices() {
		// refresh capture devices by querying the capturer implementaion
		String[] captureDevices = VideoCapturerFactory.getInstance().getCaptureDevices();
		captureDeviceComboBox.setModel(new DefaultComboBoxModel(captureDevices));
		// notify listeners about default selection
		String selectedCapturer = captureDeviceComboBox.getSelectedItem().toString();
		firePropertyChange(SELECTED_CAPTURE_DEVICE, selectedCaptureDevice, selectedCaptureDevice = selectedCapturer);
		// get graphics environment
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		// let user choose on which screen to show the capturer, only if more than one is connected
		if (graphicsDevices.length > 2) {
			buttonPanel.removeAll();
			
			JLabel screenLabel = new JLabel("Kies een scherm ");
			buttonPanel.add(screenLabel, "wrap, grow, span");
			
			JButton button = null;
			ButtonGroup screenButtonGroup = new ButtonGroup();
			// get the default monitor id for this capturer
			int defaultMonitorId = VideoComponent.getDefaultScreenId(graphicsDevices.length, capturerId);
			for (GraphicsDevice graphicsDevice : graphicsDevices) {
				String monitorIdString  = graphicsDevice.getIDstring();
				monitorIdString = monitorIdString.substring(monitorIdString.length() - 1);
				button = new JButton(monitorIdString);
				button.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						JButton source = (JButton) e.getSource();
						String monitorId = source.getText();
						firePropertyChange(VideoComponent.MONITOR_ID, selectedMonitorId, selectedMonitorId = monitorId);
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
