package com.runwalk.video.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;

import org.jdesktop.application.session.PropertySupport;
import org.jdesktop.application.session.WindowState;

import com.tomtessier.scrollabledesktop.BaseInternalFrame;

/**
 * An abstract class that can be ineherited to create an in application frame.
 * @author Jeroen Peelaerts
 */
public class MyInternalFrame extends ComponentDecorator<BaseInternalFrame> {

	/**
	 * Create a new JInternalFrame.
	 * @param title set the frame's title.
	 * @param resizable set whether the frame should be resizable.
	 */
	public MyInternalFrame(boolean resizable) {
		this("", resizable);
	}
	
	public MyInternalFrame(String title, boolean resizable) {
		super(new BaseInternalFrame(title, resizable, true));
		getComponent().setName(title);
		getComponent().setDefaultCloseOperation(BaseInternalFrame.HIDE_ON_CLOSE);
		getComponent().setResizable(resizable);
	}
	
	public void pack() {
		getComponent().pack();
	}
	
	public Container getContentPane() {
		return getComponent().getContentPane();
	}
	
	public static class InternalFrameState extends WindowState {

		public InternalFrameState() {
			super();
		}
		public InternalFrameState(Rectangle bounds, Rectangle gcBounds, 
				int screenCount) {
			super(bounds, gcBounds, screenCount, Frame.NORMAL);
		}

	}


	public static class InternalFrameProperty implements PropertySupport {

		private void checkComponent(Component component) {
			if (component == null) {
				throw new IllegalArgumentException("null component");
			}
			if (!(component instanceof JInternalFrame)) {
				throw new IllegalArgumentException("invalid component");
			}
		}

		private int getScreenCount() {
			return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
		}

		public Object getSessionState(Component c) {
			checkComponent(c);
			JInternalFrame frame = (JInternalFrame) c;
			GraphicsConfiguration gc = c.getGraphicsConfiguration();
			Rectangle gcBounds = (gc == null) ? null : gc.getBounds();
			Rectangle frameBounds = c.getBounds();
			if ((c instanceof JInternalFrame) && ((JInternalFrame) c).isMaximum()) {
				frameBounds = frame.getNormalBounds();
			}
			return new WindowState(frameBounds, gcBounds, getScreenCount(), Frame.NORMAL);
		}

		public void setSessionState(Component c, Object state) {
			checkComponent(c);
			if ((state != null) && !(state instanceof WindowState)) {
				throw new IllegalArgumentException("invalid state");
			}
			JInternalFrame w = (JInternalFrame) c;
			if ((state != null)) {
				WindowState windowState = (WindowState)state;
				Rectangle gcBounds0 = windowState.getGraphicsConfigurationBounds();
				int sc0 = windowState.getScreenCount();
				GraphicsConfiguration gc = c.getGraphicsConfiguration();
				Rectangle gcBounds1 = (gc == null) ? null : gc.getBounds();
				int sc1 = getScreenCount();
				if ((gcBounds0 != null) && (gcBounds0.equals(gcBounds1)) && (sc0 == sc1)) {
					if (w.isResizable()) {
						w.setBounds(windowState.getBounds());
					}
				}
//				boolean maximized = windowState.getFrameState() == 0 ? false : true;
//				try {
//					w.setMaximum(windowState.getMaximized());
//					w.setClosed(windowState.getClosed());
//				} catch (PropertyVetoException e) {
//					e.printStackTrace();
//				}
			}
		}

	}

}

