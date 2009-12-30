package com.runwalk.video.gui.mediaplayer;

import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public abstract class PlayerGUI implements PropertyChangeListener {

	protected JButton open, stop, mute, next, prev, fullscreen, fwd, bwd, captureSettingsBtn,
	cameraSettingsBtn, vol_up, vol_down, hide, record, snapshot;
	protected JToggleButton play_pause;
	protected JPanel playListPanel, plNorth;
	protected JPanel menu;
	protected JFileChooser fileChooser;
	protected JList playList_view;
	protected JScrollPane listScroller;
//	protected Timer playTimer;
	private JSlider scroll;
	
	protected PlayerEngine engine;
	
	public PlayerGUI(String name, PlayerEngine player) {
//		super(RunwalkVideoApp.getApplication());
//		super(name, false);
//		setTitle(name)
		engine = player;
//		playerCanvas = new Canvas();
//		videoPanel = new JPanel();
//		videoPanel.add(pslayerCanvas);
		initInterface();
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	protected abstract void initInterface();

	protected void setSlider(JSlider scroll) {
		this.scroll = scroll;
	}

	protected JSlider getSlider() {
		return scroll;
	}

}
