package com.runwalk.video.ui.actions;

import java.awt.Toolkit;
import java.io.File;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.apache.log4j.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.jdesktop.application.Task.BlockingScope;

import com.runwalk.video.core.PropertyChangeSupport;
import com.runwalk.video.io.VideoFileManager;

public class ApplicationActions implements PropertyChangeSupport {
	private static final String REDO_ENABLED = "redoEnabled";
	private static final String UNDO_ENABLED = "undoEnabled";
	private UndoManager undo = new UndoManager();
	private boolean undoEnabled;
	private boolean redoEnabled;
	
	private VideoFileManager videoFileManager;
	
	private class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			setUndoEnabled(e.getEdit().canUndo());
			setRedoEnabled(e.getEdit().canRedo());
		}
	}
	
	public ApplicationActions(VideoFileManager videoFileManager) {
		this.videoFileManager = videoFileManager;
	}

	public MyUndoableEditListener getUndoableEditListener() {
		return new MyUndoableEditListener();
	}

	private void updateUndoActions() {
		setRedoEnabled(undo.canRedo());
		setUndoEnabled(undo.canUndo());
	}

	@Action(enabledProperty = UNDO_ENABLED)
	public void undo() {
		try {
			undo.undo();
		}
		catch( CannotUndoException ex) {
			Logger.getLogger(ApplicationActions.class).error(ex);
			Toolkit.getDefaultToolkit().beep();
		}
		updateUndoActions();
	}

	@Action(enabledProperty = REDO_ENABLED)
	public void redo() {
		try {
			undo.redo();
		}
		catch( CannotUndoException ex) {
			Logger.getLogger(ApplicationActions.class).error(ex);
			Toolkit.getDefaultToolkit().beep();
		}
		updateUndoActions();
	}
	
	/*@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean, Void> selectUncompressedVideoDir() {
		Task<Boolean, Void> result = null;
		File oldDir = getAppSettings().getUncompressedVideoDir();
		javax.swing.Action action = getAction(SELECT_UNCOMPRESSED_VIDEO_DIR_ACTION);
		String title = action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString();
		File newDir = selectDirectory(oldDir, title);
		if (!newDir.equals(oldDir)) {
			getAppSettings().setUncompressedVideoDir(newDir);
			result = refreshVideoFiles();
		}
		return result;
	}

	@Action(block = BlockingScope.APPLICATION)
	public Task<Boolean,Void> selectVideoDir() {
		Task<Boolean, Void> result = null;
		File oldDir = getAppSettings().getVideoDir();
		javax.swing.Action action = getAction(SELECT_VIDEO_DIR_ACTION);
		String title = action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString();
		File newDir = selectDirectory(oldDir, title);
		if (!newDir.equals(oldDir)) {
			getAppSettings().setVideoDir(newDir);
			result = refreshVideoFiles();
		}
		return result;
	}*/
	
	public boolean isUndoEnabled() {
		return undoEnabled;
	}

	public boolean isRedoEnabled() {
		return redoEnabled;
	}

	public void discardAllEdits() {
		undo.discardAllEdits();
	}

	public void setUndoEnabled(boolean undoEnabled) {
		this.firePropertyChange(UNDO_ENABLED, this.undoEnabled, this.undoEnabled = undoEnabled);
	}

	public void setRedoEnabled(boolean redoEnabled) {
		this.firePropertyChange(REDO_ENABLED, this.redoEnabled, this.redoEnabled = redoEnabled);
	}
	
}
