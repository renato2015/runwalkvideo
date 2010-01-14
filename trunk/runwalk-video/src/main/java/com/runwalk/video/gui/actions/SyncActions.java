package com.runwalk.video.gui.actions;

import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;

import com.runwalk.video.gui.tasks.PrepareSyncTask;
import com.runwalk.video.gui.tasks.SyncTask;

public class SyncActions extends AbstractBean {
	
	private static final String SCAN_ENABLED = "scanEnabled";
	private static final String SYNC_ENABLED = "syncEnabled";
	private boolean syncEnabled, scanEnabled;
	
	public boolean isScanEnabled() {
		return scanEnabled;
	}

	public void setScanEnabled(boolean scanEnabled) {
		this.scanEnabled = scanEnabled;
		this.firePropertyChange(SCAN_ENABLED, !isScanEnabled(), isScanEnabled());
	}
	
	@Action(enabledProperty = SYNC_ENABLED)
	public Task<Boolean, Void> synchronize() {
		setSyncEnabled(false);
		setScanEnabled(false);
		return new SyncTask();
	}
	
	public boolean isSyncEnabled() {
		return syncEnabled;
	}

	public void setSyncEnabled(boolean enabled) {
		this.syncEnabled = enabled;
		this.firePropertyChange(SYNC_ENABLED, !isSyncEnabled(), isSyncEnabled());
	}
	
	@Action(enabledProperty=SCAN_ENABLED)
	public Task<String, Void> preparesync() {
		return new PrepareSyncTask(null);
	}
}
