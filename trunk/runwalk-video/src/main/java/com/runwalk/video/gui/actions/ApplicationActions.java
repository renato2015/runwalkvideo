package com.runwalk.video.gui.actions;

import java.awt.Toolkit;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.RunwalkVideoAboutBox;
import com.runwalk.video.gui.tasks.RefreshTask;
import com.runwalk.video.gui.tasks.SaveTask;
import com.runwalk.video.gui.tasks.UploadLogFilesTask;
import com.runwalk.video.util.ApplicationSettings;

public class ApplicationActions extends AbstractBean {
	private UndoManager undo = new UndoManager();
	private JDialog aboutBox;
	private boolean undoEnabled;
	private boolean redoEnabled;
	private boolean saveNeeded = false;

	private class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			setUndoEnabled(e.getEdit().canUndo());
			setRedoEnabled(e.getEdit().canRedo());
		}
	}
	
	public MyUndoableEditListener getUndoableEditListener() {
		return new MyUndoableEditListener();
	}

	private void updateUndoActions() {
		setRedoEnabled(undo.canRedo());
		setUndoEnabled(undo.canUndo());
	}

	@Action(enabledProperty="undoEnabled")
	public void undo() {
		try {
			undo.undo();
		}
		catch( CannotUndoException ex) {
			Toolkit.getDefaultToolkit().beep();
		}
		updateUndoActions();
	}

	@Action(enabledProperty="redoEnabled")
	public void redo() {
		try {
			undo.redo();
		}
		catch( CannotUndoException ex) {
			Toolkit.getDefaultToolkit().beep();
		}
		updateUndoActions();
	}

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
		this.undoEnabled = undoEnabled;
		this.firePropertyChange("undoEnabled", !isUndoEnabled(), isUndoEnabled());
	}

	public void setRedoEnabled(boolean redoEnabled) {
		this.redoEnabled = redoEnabled;
		this.firePropertyChange("redoEnabled", !isRedoEnabled(), isRedoEnabled());
	}

	@Action
	public void exit() {
		RunwalkVideoApp.getApplication().exit();
	}

	@Action
	public void about() {
		if (aboutBox == null) {
			aboutBox = new RunwalkVideoAboutBox(RunwalkVideoApp.getApplication().getMainFrame());
		}
		aboutBox.setLocationRelativeTo(RunwalkVideoApp.getApplication().getMainFrame());
		RunwalkVideoApp.getApplication().show(aboutBox);
	}
	
	@Action
	public void selectVideoDir() {
		File chosenDir = ApplicationSettings.getInstance().getVideoDir();
		final JFileChooser chooser = chosenDir == null ? new JFileChooser() : new JFileChooser(chosenDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showDialog(RunwalkVideoApp.getApplication().getMainFrame(), "Selecteer");
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	    	ApplicationSettings.getInstance().setVideoDir(chooser.getSelectedFile());
	    }
	}
	
	@Action(enabledProperty="saveNeeded")
	public Task<Void, Void> save() {
		return new SaveTask();
	}

	public boolean isSaveNeeded() {
		return saveNeeded;
	}

	public void setSaveNeeded(boolean saveNeeded) {
		this.saveNeeded = saveNeeded;
		this.firePropertyChange("saveNeeded", !isSaveNeeded(), isSaveNeeded());
	}

	@Action(block=Task.BlockingScope.ACTION)
	public Task<Void, Void> refresh() {
		return new RefreshTask();
	}
	
	@Action
	public Task<Void, Void> uploadLogFiles() {
		return new UploadLogFilesTask();
	}

}
