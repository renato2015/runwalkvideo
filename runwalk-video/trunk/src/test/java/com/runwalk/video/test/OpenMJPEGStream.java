package com.runwalk.video.test;

import javax.swing.JFrame;

import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSGraph;
import de.humatic.dsj.src.MJPGNetworkSource;
import junit.framework.TestCase;

public class OpenMJPEGStream extends TestCase {
	
	public static void main (String[] args) throws Exception {
		new OpenMJPEGStream().testOpenStream();
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
	
}
