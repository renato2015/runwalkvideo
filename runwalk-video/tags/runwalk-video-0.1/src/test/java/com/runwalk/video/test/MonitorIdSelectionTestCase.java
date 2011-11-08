package com.runwalk.video.test;

import com.runwalk.video.gui.media.VideoComponent;

import junit.framework.TestCase;

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
		for (int i = 2; i < 10; i++) {
			assertEquals(0, VideoComponent.getDefaultScreenId(1, i));
		}
		
		//test for two attached screens, default screenId should be 1 always
		for (int i = 2; i < 10; i++) {
			assertEquals(1, VideoComponent.getDefaultScreenId(2, i));
		}

		//test for three attached screens, default screenId should alternate between 1 and 2
		for (int i = 2; i < 10; i++) {
			int expected = i % 2 == 0 ? 2 : 1;
			assertEquals(expected, VideoComponent.getDefaultScreenId(3, i));
		}
		
		//test for four attached screens, default screenId should alternate between 1, 2 and 3
		assertEquals(1, VideoComponent.getDefaultScreenId(4, 1));
		assertEquals(2, VideoComponent.getDefaultScreenId(4, 2));
		assertEquals(3, VideoComponent.getDefaultScreenId(4, 3));
		assertEquals(1, VideoComponent.getDefaultScreenId(4, 4));
		assertEquals(2, VideoComponent.getDefaultScreenId(4, 5));
		assertEquals(3, VideoComponent.getDefaultScreenId(4, 6));
		assertEquals(1, VideoComponent.getDefaultScreenId(4, 7));
		
	}
}
