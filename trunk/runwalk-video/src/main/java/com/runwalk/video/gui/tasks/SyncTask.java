package com.runwalk.video.gui.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.gui.actions.RecordingStatus;
import com.runwalk.video.util.ApplicationUtil;

public class SyncTask extends AbstractTask<Boolean, Void>{ 
	private int errorCount = 0;

	public SyncTask() {
		super("synchronize");
	}

	@Override
	protected Boolean doInBackground() {
		/*			int listSize = RunwalkVideoApp.getApplication().getConversionTableModel().getMovieMap().size();
			int i = 0;
			Iterator<Analysis> it = RunwalkVideoApp.getApplication().getConversionTableModel().getMovieMap().keySet().iterator();

			while(it.hasNext()) {
				setProgress(i, 0, listSize);
				message("progressMessage", i+1 , listSize);
				i++;

				Analysis analysis = it.next();
				File fromFile = RunwalkVideoApp.getApplication().getConversionTableModel().getMovie(analysis);
				String date = analysis.getFormattedDate(new SimpleDateFormat("dd-MM-yy"));
				String newFileName = analysis.getID() + "_" + analysis.getClient().getName() + "_" + date + ".MTS";
				File toFile = new File(RunwalkVideoApp.getVideoDir(), newFileName);
				MovieStatusCodes statusCode = copy(fromFile, toFile);
				analysis.getMovie().setStatusCode(statusCode);
				Movie movie = null;
				if (statusCode != MovieStatusCodes.COMPRESSED) {
					movie = new Movie();
					errorCount++;
				} else {
					movie = new Movie(fromFile.lastModified(), fromFile.getName(), newFileName);
				}
				//save to database..
				EntityManager em = RunwalkVideoApp.getApplication().getEntityManagerFactory().createEntityManager();
				em.getTransaction().begin();
				em.persist(movie);
				analysis.setMovie(movie);
				Analysis mergedAnalysis = em.merge(analysis);
				em.getTransaction().commit();
				em.close();

				//TODO change analysis fetching type to EAGER and fix this 'issue'
				int itemIndex = RunwalkVideoApp.getApplication().getConversionTableModel().getItemIndex(analysis);
				RunwalkVideoApp.getApplication().getConversionTableModel().setItem(itemIndex, mergedAnalysis);
				int tableIndex = RunwalkVideoApp.getApplication().getConversionTablePanel().getTable().convertRowIndexToModel(itemIndex);
				RunwalkVideoApp.getApplication().getConversionTableModel().fireTableRowsUpdated(tableIndex, tableIndex);
			}
			return (errorCount == 0);*/
		return true;
	}

	@Override
	protected void finished() {
		try {
			if (get()) {
				message("endMessage");
			}
		} catch (Exception e) {
			errorMessage(getResourceMap().getString(resourceName("endErrorMessage"), new Integer(errorCount)));
		} finally {
			String syncMsg = getResourceMap().getString(resourceName("lastSyncMessage")) + ApplicationUtil.formatDate(new Date(System.currentTimeMillis()), new SimpleDateFormat("dd/MM/yyyy HH:mm"));
			RunwalkVideoApp.getApplication().getAnalysisOverviewTable().setStatusMessage(syncMsg);
		}
	}

	private RecordingStatus copy(File fromFile, File toFile) {
		FileInputStream from = null;
		FileOutputStream to = null;

		if (fromFile == null) 
			return RecordingStatus.NON_EXISTANT_FILE;
		if (!fromFile.exists()) 
			return RecordingStatus.NON_EXISTANT_FILE;
		if (!fromFile.canRead()) 
			return RecordingStatus.FILE_NOT_ACCESSIBLE;

		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;
			int counter = 0;
			int curProgress = getProgress();
			while ((bytesRead = from.read(buffer)) != -1){
				to.write(buffer, 0, bytesRead); // write
				counter ++;
				float done = (float) ((counter * 4096) / fromFile.getTotalSpace());
				//					setProgress(curProgress + done, 0, RunwalkVideoApp.getApplication().getConversionTableModel().getMovieMap().size());
				//set progress here!!
				System.out.println(done);
				//					float progress = this.getProgress();
				//TODO implement progress bar updates
			}

		}
		catch(Exception e) { 
			toFile.delete();
			return RecordingStatus.DSJ_ERROR;
		}
		finally {
			if (from != null)
				try {
					from.close();
				} 
			catch (IOException e) { 
				toFile.delete();
				return RecordingStatus.DSJ_ERROR;
			}
			if (to != null)
				try {
					to.close();
				} 
			catch (IOException e) { 
				toFile.delete();
				return RecordingStatus.DSJ_ERROR;
			}
		}
		return RecordingStatus.COMPRESSED;
	}

}