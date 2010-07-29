package com.runwalk.video.gui.media;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

	private JComboBox captureDeviceComboBox;

	private int selectedDeviceIndex = -1, capturerId;
	
	private String selectedMonitorId;

	private IVideoCapturer capturerImpl;

	private JPanel buttonPanel;

	public CameraDialog(Frame parent, IVideoCapturer capturerImpl, int capturerId) {
		super(parent, true);
		this.capturerId = capturerId;
		this.capturerImpl = capturerImpl;
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
			add(cancelButton);
		}
		JButton refreshButton = new JButton(getAction("refreshCaptureDevices")); // NOI18N
		add(refreshButton, "align right");
		JButton okButton = new JButton(getAction("dismissDialog"));
		add(okButton, "align right");
		getRootPane().setDefaultButton(okButton);
		// populate combobox with the capture device list
		captureDeviceComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				JComboBox source = (JComboBox) e.getSource();
				int deviceIndex = source.getSelectedIndex();
				firePropertyChange(VideoCapturer.CAPTURE_DEVICE, selectedDeviceIndex, selectedDeviceIndex = deviceIndex);
			}
			
		});
		pack();
		toFront();
	}

	@Action
	public void dismissDialog() {
		setVisible(false);
		// clean up so the dialog can be garbage collected
		capturerImpl = null;
		// release native screen resources
		dispose();
	}

	/**
	 * This method refreshes the list with connected capture devices 
	 * and displaying devices. The layout of this dialog will be changed accordingly.
	 */
	@Action
	public void refreshCaptureDevices() {
		// refresh capture devices by querying the capturer implementaion
		String[] captureDevices = capturerImpl.getCaptureDevices();
		captureDeviceComboBox.setModel(new DefaultComboBoxModel(captureDevices));
		// notify listeners about default selection
		int deviceIndex = captureDeviceComboBox.getSelectedIndex();
		firePropertyChange(VideoCapturer.CAPTURE_DEVICE, selectedDeviceIndex, selectedDeviceIndex = deviceIndex);

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

	public void setCapturerImpl(IVideoCapturer capturerImpl) {
		this.capturerImpl = capturerImpl;
	}

}
