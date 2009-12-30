package com.runwalk.video.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.util.ApplicationSettings;

public class VideoToolbar extends JToolBar {

	private static final long serialVersionUID = 1L;
	private JSlider progressSlider;
	private JFormattedTextField sliderValueField;
	private JButton rwndButton, stopButton, ffdButton, captureButton;
	private JToggleButton playButton;

	public VideoToolbar() {
		setOrientation(JToolBar.HORIZONTAL);
		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new AbsoluteLayout());

		JPanel sliderPanel = new JPanel(new BorderLayout());
		progressSlider = new JSlider();
		progressSlider.setFont(ApplicationSettings.MAIN_FONT);
		progressSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int value = progressSlider.getValue();
			}
		});
		progressSlider.setMinorTickSpacing(5);
		progressSlider.setPaintLabels(true);
		progressSlider.setPaintTicks(true);
		progressSlider.setValue(0);
		sliderPanel.add(progressSlider, BorderLayout.CENTER);

		captureButton = new JButton();
		captureButton.setPreferredSize(new Dimension(25, 25));
		sliderPanel.add(captureButton, BorderLayout.EAST);


		JPanel buttonPanel = new JPanel(new AbsoluteLayout());
		rwndButton = new JButton(RunwalkVideoApp.getApplication().getPlayerActionMap().get("backward"));
		buttonPanel.add(rwndButton, new AbsoluteConstraints(0, 5, 25, 25));
		stopButton = new JButton(RunwalkVideoApp.getApplication().getPlayerActionMap().get("stop"));
		buttonPanel.add(stopButton, new AbsoluteConstraints(25, 5, 25, 25));
		playButton = new JToggleButton(RunwalkVideoApp.getApplication().getPlayerActionMap().get("play"));
		buttonPanel.add(playButton, new AbsoluteConstraints(50, 5, 25, 25));
		ffdButton = new JButton(RunwalkVideoApp.getApplication().getPlayerActionMap().get("forward"));
		buttonPanel.add(ffdButton, new AbsoluteConstraints(75, 5, 25, 25));

		toolPanel.add(buttonPanel, new AbsoluteConstraints(0, 0, 120, 30));
		JSeparator toolSeparator = new JSeparator();
		toolSeparator.setOrientation(SwingConstants.VERTICAL);
		toolPanel.add(toolSeparator, new AbsoluteConstraints(120, 0, -1, 35));
		toolPanel.add(sliderPanel, new AbsoluteConstraints(130, 5, 300, 30));
		add(toolPanel);
		setName("Video Controls");
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		sliderValueField.setEnabled(enabled);
		progressSlider.setEnabled(enabled);
		rwndButton.setEnabled(enabled);
		stopButton.setEnabled(enabled);
		playButton.setEnabled(enabled);
		ffdButton.setEnabled(enabled);
	}

}
