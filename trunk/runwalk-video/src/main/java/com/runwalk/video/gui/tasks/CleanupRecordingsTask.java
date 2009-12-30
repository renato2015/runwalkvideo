package com.runwalk.video.gui.tasks;

import java.io.File;
import java.io.FileFilter;

import javax.swing.JOptionPane;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.util.ApplicationSettings;
import com.runwalk.video.util.ApplicationUtil;

import de.humatic.dsj.DSJException;

public class CleanupRecordingsTask extends AbstractTask<Boolean, Void> {

		int filesDeleted = 0, fileCount = 0;

		public CleanupRecordingsTask() {
			super("cleanupRecordingsTask");
		}

		@Override
		protected Boolean doInBackground() throws Exception {
			
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
						int uncompressedVideoDuration = ApplicationUtil.getMovieDuration(file.getAbsolutePath());
						File compressedVideoFile = ApplicationUtil.getCompressedVideoFile(file.getName());
						if (compressedVideoFile != null) {
							int compressedVideoDuration = ApplicationUtil.getMovieDuration(compressedVideoFile.getAbsolutePath());
							accept = uncompressedVideoDuration == compressedVideoDuration;
						}
					} catch(DSJException e) {
						accept = true;
					}
					return accept;
				}

			};

			File[] listFiles = ApplicationSettings.getInstance().getUncompressedVideoDir().listFiles(fileFilter);
			fileCount = listFiles.length;
			String dlogTitle = null;
			boolean success = true;
			if (fileCount == 0) {
				dlogTitle = getResourceString("endMessage");
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
						getResourceString("noFilesFoundMessage"), dlogTitle, JOptionPane.INFORMATION_MESSAGE);
				success = !cancel(true);
				RunwalkVideoApp.getApplication().getTableActions().setCleanupEnabled(success);
			} else {
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
				} else {
					success = !cancel(true);
				}
			}
			return success;
		}
		
		@Override
		protected void finished() {
			try {
				String dialogMsg = getResourceString("finishedMessage", filesDeleted); 
				String dialogTitle = getResourceString("endMessage");
				if (get()) {
					JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
							dialogMsg, dialogTitle, JOptionPane.PLAIN_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
							dialogMsg + getResourceString("endErrorMessage", fileCount - filesDeleted),
							dialogTitle, JOptionPane.WARNING_MESSAGE); 
				}
				RunwalkVideoApp.getApplication().getTableActions().setCleanupEnabled(!get());
			} catch (Exception e) {
				logger.error(e);
				RunwalkVideoApp.getApplication().getTableActions().setCleanupEnabled(true);
			} 
		}
	}