package com.runwalk.video.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import junit.framework.TestCase;

import com.runwalk.video.media.VideoPlayer;
import com.runwalk.video.media.dsj.DSJAsyncPlayer;
import com.runwalk.video.media.settings.VideoPlayerSettings;

import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSGraph;
import de.humatic.dsj.src.MJPGNetworkSource;

public class OpenMJPEGStream extends TestCase {

	public static void main (String[] args) throws Exception {
		//new OpenMJPEGStream().testOpenAsyncSource();
		DSEnvironment.setDebugLevel(4);
		DSEnvironment.unlockDLL("aa", 2, 3, 4);
	}

	public void testOpenStream() throws Exception {
		DSEnvironment.setDebugLevel(4);
		MJPGNetworkSource source = new MJPGNetworkSource("http://runwalk-cam2.runwalk.be/videostream.cgi?user=visitor&pwd=run01", null);
		DSGraph createGraph = source.createGraph(0);
		createGraph.play();
		JFrame frame = new JFrame();
		frame.add(createGraph.asComponent());
		frame.pack();
		frame.setVisible(true);
	}

	public void testOpenAsyncSource() {
		DSEnvironment.setDebugLevel(4);
		DSEnvironment.unlockDLL("jeroen.peelaerts@vaph.be", 610280L, 1777185L, 0L);
		VideoPlayerSettings videoPlayerSettings = new VideoPlayerSettings();
		videoPlayerSettings.setPlayRate(1.0f);
		final DSJAsyncPlayer asyncPlayer = new DSJAsyncPlayer(videoPlayerSettings);
		asyncPlayer.loadVideo("D:\\Video's\\uncompressed\\1_Boven_Dries_02-10-12.avi");
		//asyncPlayer.getFiltergraph().dumpGraph(true);
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		frame.add(asyncPlayer.getComponent(), BorderLayout.CENTER);
		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JButton source = (JButton) e.getSource();
				switch(source.getText()) {
				case "play" : asyncPlayer.play(); break;
				case "stop" : asyncPlayer.stop(); break;
				case "pause" : asyncPlayer.pause(); break;
				case "slower" : setNewPlayRate(asyncPlayer, false); break;
				case "faster" : setNewPlayRate(asyncPlayer, true); break;
				}
			}
		};
		JPanel panel = new JPanel();
		addButton("slower", panel, actionListener);
		addButton("play", panel, actionListener);
		addButton("pause", panel, actionListener);
		addButton("stop", panel, actionListener);
		addButton("faster", panel, actionListener);
		frame.add(panel, BorderLayout.SOUTH);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private float setNewPlayRate(DSJAsyncPlayer asyncPlayer, boolean up) {
		Float playRate = asyncPlayer.getPlayRate();
		Float newPlayRate = up ? VideoPlayer.PLAY_RATES.higher(playRate) : VideoPlayer.PLAY_RATES.lower(playRate);
		if (newPlayRate != null) {
			asyncPlayer.setPlayRate(newPlayRate);
			System.out.println("PlayRate set to " + newPlayRate);
		}
		return playRate;
	}

	private void addButton(String text, Container container, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		container.add(button);
	}

}
