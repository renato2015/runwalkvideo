package com.runwalk.video.gui.tasks;

import java.awt.Frame;
import java.io.File;
import java.io.FileFilter;

import javax.swing.JOptionPane;

import com.runwalk.video.VideoFileManager;

public class CleanupRecordingsTask extends AbstractTask<Boolean, Void> {

	int filesDeleted = 0, fileCount = 0;

	private File uncompressedVideoDir, videoDir;
	
	private Frame parentFrame;

	public CleanupRecordingsTask(Frame parentFrame, File videoDir, File uncompressedVideoDir) {
		super("cleanupRecordingsTask");
		this.parentFrame = parentFrame;
		this.videoDir = videoDir;
		this.uncompressedVideoDir = uncompressedVideoDir;
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
				long uncompressedVideoDuration = VideoFileManager.getDuration(file);
				File compressedVideoFile = new File(videoDir, file.getName());
				if (compressedVideoFile.exists()) {
					long compressedVideoDuration = VideoFileManager.getDuration(compressedVideoFile);
					accept = uncompressedVideoDuration == compressedVideoDuration;
				}
				return accept;
			}

		};
		File[] listFiles = uncompressedVideoDir.listFiles(fileFilter);
		fileCount = listFiles.length;
		String dlogTitle = null;
		boolean success = fileCount >= 0;
		if (fileCount > 0) {
			dlogTitle = getResourceString("startMessage");
			int chosenOption = JOptionPane.showConfirmDialog(parentFrame, 
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
				JOptionPane.showMessageDialog(parentFrame, getResourceString("noFilesFoundMessage"), dialogTitle, JOptionPane.INFORMATION_MESSAGE);
			} else if (get()) {
				JOptionPane.showMessageDialog(parentFrame,
						dialogMsg, dialogTitle, JOptionPane.PLAIN_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parentFrame,
						dialogMsg + getResourceString("endErrorMessage", fileCount - filesDeleted),
						dialogTitle, JOptionPane.WARNING_MESSAGE); 
			}
		} catch (Exception e) {
			getLogger().error(e);
		} 
	}

}