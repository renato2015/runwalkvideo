package com.runwalk.video.gui.tasks;

import com.runwalk.video.RunwalkVideoApp;

public class RefreshTask extends AbstractTask<Void, Void> {

		public RefreshTask() {
			super("refresh");
		}

		@Override protected Void doInBackground() {
			try {
				setProgress(0, 0, 3);
				message("startMessage");
				RunwalkVideoApp.getApplication().getClientTableModel().update();
				setProgress(1, 0, 3);
				RunwalkVideoApp.getApplication().getAnalysisTableModel().update();
				setProgress(2, 0, 3);
				RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().update();
				setProgress(3, 0, 3);
				message("fetchMessage");
			} 
			catch(Exception ignore) {
				errorMessage(ignore.getLocalizedMessage());
			}
			return null;
		}
		@Override protected void finished() {
			message("endMessage");
			RunwalkVideoApp.getApplication().setSaveNeeded(false);
		}
	}