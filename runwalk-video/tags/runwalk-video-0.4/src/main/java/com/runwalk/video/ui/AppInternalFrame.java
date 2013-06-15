package com.runwalk.video.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.JInternalFrame;

import org.jdesktop.application.Action;
import org.jdesktop.application.session.PropertySupport;
import org.jdesktop.application.session.WindowState;

import com.runwalk.video.core.Containable;
import com.runwalk.video.core.SelfContained;
import com.tomtessier.scrollabledesktop.BaseInternalFrame;

/**
 * An class that can be used to wrap classes that implement the {@link Containable} interface.
 * @author Jeroen Peelaerts
 */
@SuppressWarnings("serial")
public class AppInternalFrame extends BaseInternalFrame implements SelfContained {
	/**
	 * Initially all internal frames will not be visible
	 */
	private boolean visible = false;
	
	private Dimension minimumSize;

	/**
	 * Create a new JInternalFrame.
	 * @param title set the frame's title.
	 * @param resizable set whether the frame should be resizable.
	 */
	public AppInternalFrame(String title, boolean resizable) {
		super(title, resizable, true);
		setName(title);
		setDefaultCloseOperation(BaseInternalFrame.HIDE_ON_CLOSE);
		setResizable(resizable);
	}
	
	public AppInternalFrame(String title, boolean resizable, final Dimension minimumSize) {
		this(title, resizable);
		if (resizable) {
			this.minimumSize = minimumSize;
		}
	}
	
	@Override
	public Dimension getMinimumSize() {
		if (minimumSize != null) {
			return minimumSize;
		}
		return super.getMinimumSize();
	}

	@Action(selectedProperty = VISIBLE)
	public void toggleVisibility(ActionEvent event) {
		// check if event is originating from a component that has selected state
		if (event.getSource() instanceof AbstractButton) {
			AbstractButton source = (AbstractButton) event.getSource();
			setVisible(source.isSelected());
		}
	}

	@Override
	public void setVisible(boolean visible) {
		// fire a pce as component does not do this by default
		firePropertyChange(VISIBLE, this.visible, this.visible = visible);
		super.setVisible(visible);
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
					} else {
						w.setLocation(windowState.getBounds().x, windowState.getBounds().y);
					}
				}
			}
		}

	}

	public void setMonitorId(Integer monitorId) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Integer getMonitorId() {
		return null;
	}

}

