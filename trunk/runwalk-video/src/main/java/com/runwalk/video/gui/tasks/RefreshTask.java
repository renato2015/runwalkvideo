package com.runwalk.video.gui.tasks;

import com.runwalk.video.gui.AbstractTablePanel;

public class RefreshTask extends AbstractTask<Boolean, Void> {

		private AbstractTablePanel<?> tablePanel;
		
		public RefreshTask(AbstractTablePanel<?> panel) {
			super("refresh");
			this.tablePanel = panel;
		}

		@Override 
		protected Boolean doInBackground() {
			boolean success = true;
			try {
				message("startMessage");
				setProgress(0, 0, 1);
				message("fetchMessage");
				tablePanel.update();
				setProgress(1, 0, 1);
				message("endMessage");
			} catch(Exception ignore) {
				errorMessage(ignore.getLocalizedMessage());
				success = false;
			}
			return success;
		}
	}