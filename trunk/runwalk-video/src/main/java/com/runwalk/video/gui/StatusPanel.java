package com.runwalk.video.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskMonitor;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.util.ApplicationSettings;

public class StatusPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private int busyIconIndex = 0;
	private final Icon[] busyIcons = new Icon[15];
	private final Timer busyIconTimer;

	private final Icon idleIcon;
	private final Timer messageTimer;
	private final JProgressBar progressBar;
	private final JLabel statusAnimationLabel;
	private JLabel statusMessageLabel;
	
	private final static Logger logger = Logger.getLogger(StatusPanel.class);

	private TaskMonitor taskMonitor;

	public StatusPanel() {
		super(new AbsoluteLayout());
		ResourceMap resourceMap = Application.getInstance().getContext().getResourceMap(StatusPanel.class);
		statusMessageLabel = new JLabel();
		statusMessageLabel.setFont(ApplicationSettings.MAIN_FONT);
		statusAnimationLabel = new JLabel();
		progressBar = new JProgressBar();
		// status bar initialization - message timeout, idle icon and busy
		// animation, etc

		add(statusMessageLabel, new AbsoluteConstraints(10, 0, -1, 30));
		//		statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
		
		add(statusAnimationLabel, new AbsoluteConstraints(420, 0, 30, 30));
		add(progressBar, new AbsoluteConstraints(440, 5, 140, 20));
	
		
		final int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
		messageTimer = new Timer(messageTimeout, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statusMessageLabel.setText("");
			}
		});
		messageTimer.setRepeats(false);
		final int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");

		for (int i = 0; i < busyIcons.length; i++)
			busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");

		busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
				statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
			}
		});
		idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
		statusAnimationLabel.setIcon(idleIcon);
//		progressBar.setVisible(false);
		
		// connecting action tasks to status bar via TaskMonitor
		taskMonitor = new TaskMonitor(RunwalkVideoApp.getApplication().getContext());
		taskMonitor.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				final String propertyName = evt.getPropertyName();
				if ("started".equals(propertyName)) {
					if (!busyIconTimer.isRunning()) {
						statusAnimationLabel.setIcon(busyIcons[0]);
						busyIconIndex = 0;
						busyIconTimer.start();
					}
//					progressBar.setVisible(true);
					progressBar.setIndeterminate(true);
//					progressBar.setValue(0);
				}
				else if ("done".equals(propertyName)) {
					busyIconTimer.stop();
					statusAnimationLabel.setIcon(idleIcon);
//					progressBar.setVisible(false);
					progressBar.setIndeterminate(false);
					progressBar.setValue(0);
				}
				else if ("message".equals(propertyName)) {
					final String text = (String) evt.getNewValue();
					showMessage(text == null ? "" : text);
					messageTimer.restart();
				}
				else if ("errorMessage".equals(propertyName)) {
					final String text = (String) evt.getNewValue();
					RunwalkVideoApp.getApplication().showError(text == null ? "" : text);
//					messageTimer.restart();
				}
				else if ("progress".equals(propertyName)) {
					final int value = (Integer) evt.getNewValue();
//					progressBar.setVisible(true);
					progressBar.setIndeterminate(false);
					progressBar.setValue(value);
				}
			}
		});
	}

	private void showMessage(Color theColor, String msg) {
		statusMessageLabel.setText(msg);
		statusMessageLabel.setForeground(theColor);
		messageTimer.restart();
	}
	
	public void showMessage(String msg) {
		logger.log(Level.INFO, msg);
		showMessage(Color.black, msg);
	}
	
	public void showErrorMessage(String error) {
		logger.log(Level.ERROR, error);
		showMessage(Color.red, error);
	}
	
	public void setIndeterminate(boolean indeterminate) {
		getProgressBar().setIndeterminate(indeterminate);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

}
