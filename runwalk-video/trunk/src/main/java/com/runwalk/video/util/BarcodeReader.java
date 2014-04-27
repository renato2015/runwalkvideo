package com.runwalk.video.util;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class BarcodeReader {

	private static final String BARCODE_RECEIVED = "barcodeReceived";
	private static final long THRESHOLD = 100;
	private static final int MIN_BARCODE_LENGTH = 8;

	private final StringBuffer barcode = new StringBuffer();
	private final List<ActionListener> actionListeners = new CopyOnWriteArrayList<ActionListener>();
	private long lastEventTimeStamp = 0L;
	
	private ConcurrentLinkedQueue<String> barcodeStack = new ConcurrentLinkedQueue<String>();

	public BarcodeReader() {

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() != KeyEvent.KEY_RELEASED) {
					return false;
				}
				

				if (e.getWhen() - lastEventTimeStamp > THRESHOLD) {
					barcode.delete(0, barcode.length());
				}

				lastEventTimeStamp = e.getWhen();

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (barcode.length() >= MIN_BARCODE_LENGTH) {
						barcodeStack.offer(barcode.toString());
						fireActionEvent();
					}
					barcode.delete(0, barcode.length());
				} else {
					barcode.append(e.getKeyChar());
				}
				return false;
			}
		});

	}
	
	public String getBarcode() {
		return barcodeStack.poll();
	}

	protected void fireActionEvent() {
		for (ActionListener actionListener : actionListeners) {
			actionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, BARCODE_RECEIVED));
		}
	}

	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}

	public void removeBarcodeListener(ActionListener actionListener) {
		actionListeners.remove(actionListener);
	}

}