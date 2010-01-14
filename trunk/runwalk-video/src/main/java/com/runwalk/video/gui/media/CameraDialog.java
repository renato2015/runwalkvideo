package com.runwalk.video.gui.media;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.jdesktop.application.Action;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.gui.ComponentDecorator;

public class CameraDialog extends ComponentDecorator<JDialog> {

	private JComboBox captureDeviceComboBox;
	
	private int currentSelection;

	public CameraDialog(Frame parent) {
		super(new JDialog(parent, true));
		getComponent().addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent arg0) {
				System.exit(1);
			}
			
		});
		getComponent().setTitle(getResourceMap().getString("captureDeviceDlg.title")); // NOI18N
		getComponent().setResizable(false);
		setLayout(new AbsoluteLayout());
		
		captureDeviceComboBox = new JComboBox();
		add(captureDeviceComboBox, new AbsoluteConstraints(10, 30, 250, -1));

		JLabel captureDeviceLabel = new JLabel(getResourceMap().getString("captureDeviceLabel.text")); // NOI18N
		add(captureDeviceLabel, new AbsoluteConstraints(10, 10, 210, -1));

		JButton cancelButton = new JButton(getApplication().getApplicationActionMap().get("exit")); // NOI18N
		add(cancelButton, new AbsoluteConstraints(10, 60, 90, 25));
		JButton refreshButton = new JButton(getAction("refreshCaptureDevices")); // NOI18N
		add(refreshButton, new AbsoluteConstraints(105, 60, 90, 25));
		JButton okButton = new JButton(getAction("dismissDialog"));
		add(okButton, new AbsoluteConstraints(200, 60, 60, 25));
		getComponent().getRootPane().setDefaultButton(okButton);

		refreshCaptureDevices();
		captureDeviceComboBox.setSelectedIndex(currentSelection);
		setPreferredSize(new Dimension(275, 125));
		getComponent().pack();
	}
	
	public void setCurrentSelection(int previousSelection) {
		this.currentSelection = previousSelection;
	}

	@Action
	public void dismissDialog() {
		setVisible(false);
		int selectedIndex = captureDeviceComboBox.getSelectedIndex();
		firePropertyChange(VideoRecorder.CAPTURE_DEVICE, currentSelection, selectedIndex);
	}
	
    @Action
    public void refreshCaptureDevices() {
    	String[] captureDevices = VideoRecorder.queryCaptureDevices();
    	captureDeviceComboBox.setModel(new DefaultComboBoxModel(captureDevices));
    }

}
