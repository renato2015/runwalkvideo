package com.runwalk.video.gui.media;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.jdesktop.application.Action;

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
		setLayout(new MigLayout("fill, nogrid"));
		setTitle(getResourceMap().getString("captureDeviceDlg.title")); // NOI18N
		setResizable(false);
		
		JLabel captureDeviceLabel = new JLabel(getResourceMap().getString("captureDeviceLabel.text")); // NOI18N
		add(captureDeviceLabel, "wrap");

		captureDeviceComboBox = new JComboBox();
		add(captureDeviceComboBox, "wrap, grow");

		JButton cancelButton = new JButton(getApplication().getApplicationActionMap().get("exit")); // NOI18N
		add(cancelButton);
		JButton refreshButton = new JButton(getAction("refreshCaptureDevices")); // NOI18N
		add(refreshButton);
		JButton okButton = new JButton(getAction("dismissDialog"));
		add(okButton);
		getRootPane().setDefaultButton(okButton);

		refreshCaptureDevices();
		captureDeviceComboBox.setSelectedIndex(currentSelection);
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
