package com.runwalk.video.test;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import junit.framework.TestCase;

import com.runwalk.video.media.VideoComponent;

/**
 * A simple {@link TestCase} that tests the default screen id calculation for numbered instances of {@link VideoComponent}.
 * The tested code assigns a screen for display to each {@link VideoComponent} when there's none specified by the user.
 * 
 * @author Jeroen Peelaerts
 */
public class MonitorIdSelectionTestCase extends TestCase {

	//test whether default screen ids are calculated correctly based on the number of available screens and a component's count
	public void testScreenIdCalculation() throws Exception {
		//test for one attached screen, default screen id should be 0 always
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] graphicsDevices = graphicsEnvironment.getScreenDevices();
		GraphicsDevice defaultGraphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
		int defaultMonitorId = Arrays.asList(graphicsDevices).indexOf(defaultGraphicsDevice);
		int freeMonitorId = graphicsDevices.length - defaultMonitorId;

		for (int i = 2; i < 10; i++) {
			assertEquals(defaultMonitorId, VideoComponent.getDefaultMonitorId(1, i));
		}
		
		//test for two attached screens, default screenId should be equal to the monitor free at that moment
		for (int i = 2; i < 10; i++) {
			assertEquals(freeMonitorId, VideoComponent.getDefaultMonitorId(2, i));
		}

		//test for three attached screens, default screenId should alternate between 1 and 2
		for (int i = 2; i < 10; i++) {
			int expected = i % 2 == 0 ? 2 : 1;
			assertEquals(expected, VideoComponent.getDefaultMonitorId(3, i));
		}
		
		//test for four attached screens, default screenId should alternate between 1, 2 and 3
		assertEquals(1, VideoComponent.getDefaultMonitorId(4, 1));
		assertEquals(2, VideoComponent.getDefaultMonitorId(4, 2));
		assertEquals(3, VideoComponent.getDefaultMonitorId(4, 3));
		assertEquals(1, VideoComponent.getDefaultMonitorId(4, 4));
		assertEquals(2, VideoComponent.getDefaultMonitorId(4, 5));
		assertEquals(3, VideoComponent.getDefaultMonitorId(4, 6));
		assertEquals(1, VideoComponent.getDefaultMonitorId(4, 7));
		
	}
}
