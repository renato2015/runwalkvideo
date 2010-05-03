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

import com.runwalk.video.gui.AppDialog;

@SuppressWarnings("serial")
public class CameraDialog extends AppDialog {

	private JComboBox captureDeviceComboBox;
	
	private int currentSelection;
	
	private IVideoCapturer capturerImpl;

	public CameraDialog(Frame parent, IVideoCapturer capturerImpl) {
		super(parent, true);
		this.capturerImpl = capturerImpl;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				Runtime.getRuntime().exit(0);
			}
			
		});
		setTitle(getResourceMap().getString("captureDeviceDlg.title")); // NOI18N
		setResizable(false);
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
		getRootPane().setDefaultButton(okButton);

		refreshCaptureDevices();
		captureDeviceComboBox.setSelectedIndex(currentSelection);
		setPreferredSize(new Dimension(275, 125));

		pack();
	}
	
	public void setCurrentSelection(int previousSelection) {
		this.currentSelection = previousSelection;
	}

	@Action
	public void dismissDialog() {
		setVisible(false);
		int selectedIndex = captureDeviceComboBox.getSelectedIndex();
		firePropertyChange(VideoCapturer.CAPTURE_DEVICE, currentSelection, currentSelection = selectedIndex);
	}
	
    @Action
    public void refreshCaptureDevices() {
    	String[] captureDevices = capturerImpl.getCaptureDevices();
    	captureDeviceComboBox.setModel(new DefaultComboBoxModel(captureDevices));
    }

	public void setCapturerImpl(IVideoCapturer capturerImpl) {
		this.capturerImpl = capturerImpl;
	}

}
