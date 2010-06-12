package com.runwalk.video.gui.tasks;

import java.io.File;
import java.io.FileFilter;

import javax.swing.JOptionPane;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.AppUtil;

import de.humatic.dsj.DSJException;

public class CleanupRecordingsTask extends AbstractTask<Boolean, Void> {

		int filesDeleted = 0, fileCount = 0;
		
		private File scanDirectory;

		public CleanupRecordingsTask(File scanDirectory) {
			super("cleanupRecordingsTask");
			this.scanDirectory = scanDirectory;
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			message("startMessage");
			/*Query query = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager().createQuery("SELECT OBJECT(mov) from Movie mov WHERE mov.statuscode = " + MovieStatusCodes.COMPRESSED.getCode());
			List<Movie> movieList = new ArrayList<Movie>(query.getResultList());
			for (Movie movie : movieList) {
				if (movie.hasDuplicateFiles()) {
					movie.getUncompressedVideoFile().delete();
				}
			}*/
			//there is a possibility where this method can find more duplicate files
			//as the movie status is compressed but not saved at that very moment.
			
			FileFilter fileFilter = new FileFilter() {

				public boolean accept(File file) {
					boolean accept = false;
					try  {
						int uncompressedVideoDuration = AppUtil.getMovieDuration(file.getAbsolutePath());
						File compressedVideoFile = new File(AppSettings.getInstance().getVideoDir(), file.getName());
						if (compressedVideoFile.exists()) {
							int compressedVideoDuration = AppUtil.getMovieDuration(compressedVideoFile.getAbsolutePath());
							accept = uncompressedVideoDuration == compressedVideoDuration;
						}
					} catch(DSJException e) {
						getLogger().error("Exception occured while scanning for duplicate files", e);
						accept = false;
					}
					return accept;
				}

			};
			File[] listFiles = scanDirectory.listFiles(fileFilter);
			fileCount = listFiles.length;
			String dlogTitle = null;
			boolean success = fileCount >= 0;
			if (fileCount > 0) {
				dlogTitle = getResourceString("startMessage");
				int chosenOption = JOptionPane.showConfirmDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
						getResourceString("filesFoundMessage", fileCount), dlogTitle, JOptionPane.OK_CANCEL_OPTION);
				if (chosenOption == JOptionPane.OK_OPTION) {
					for (File file : listFiles) {
						if (file.delete()) {
							filesDeleted++;
						} else {
							success = false;
						}
					}
				}
			}
			message("endMessage");
			return success;
		}
		
		@Override
		protected void finished() {
			try {
				String dialogMsg = getResourceString("finishedMessage", filesDeleted); 
				String dialogTitle = getResourceString("endMessage");
				if (fileCount == 0) {
					JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
							getResourceString("noFilesFoundMessage"), dialogTitle, JOptionPane.INFORMATION_MESSAGE);
				} else if (get()) {
					JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
							dialogMsg, dialogTitle, JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
							dialogMsg + getResourceString("endErrorMessage", fileCount - filesDeleted),
							dialogTitle, JOptionPane.WARNING_MESSAGE); 
				}
			} catch (Exception e) {
				getLogger().error(e);
			} 
		}
	}