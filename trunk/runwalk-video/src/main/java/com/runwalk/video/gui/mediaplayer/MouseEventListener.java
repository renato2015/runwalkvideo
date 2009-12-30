package com.runwalk.video.gui.mediaplayer;

import java.util.EventListener;
import java.util.EventObject;

public interface MouseEventListener extends EventListener {
	public void GlobalMouseMoved(MouseEvent event);
}

class MouseEvent extends EventObject {
	private static final long serialVersionUID = 2341653211621224652L;
	int x, y;
	int clickCount;

	public MouseEvent(Object source, int x, int y) {
		super(source);
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
}
