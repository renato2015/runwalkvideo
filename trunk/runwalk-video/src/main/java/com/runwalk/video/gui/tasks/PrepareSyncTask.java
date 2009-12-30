package com.runwalk.video.gui.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Analysis;
import com.runwalk.video.gui.actions.RecordingStatus;
import com.runwalk.video.util.ApplicationSettings;

public class PrepareSyncTask extends AbstractTask<String, Void> {

		public PrepareSyncTask() {
			super("preparesync");
		}

		@Override 
		protected String doInBackground() {
			RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().update();
			File cameraRoot  = ApplicationSettings.getInstance().getCameraDir();	
			if (cameraRoot.exists() && cameraRoot.isDirectory() && cameraRoot.canRead()) {
				message("startMessage");

				File[] movies = cameraRoot.listFiles();
				Pattern filenamePattern = Pattern.compile("[0]\\d\\d\\d\\d\\.MTS");
				Matcher matcher;
				List<File> movieList = new ArrayList<File>();
				for (int i = 0; i < movies.length; i++) {
					message("scanMessage");
					File movie = movies[i];
					matcher = filenamePattern.matcher(movie.getName());
					//check first whether the video is already in the database!
					Query query = RunwalkVideoApp.getApplication().createNativeQuery("SELECT lastmodified from movie WHERE lastmodified = '" + movie.lastModified() +"'");
					if (matcher.matches() && movie.isFile() && query.getResultList().size() == 0) {
						movieList.add(movie);
					}
					matcher.reset();
				}
				//now choose the most appropriate movie from the filtered list..
				int analysisCount = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().getItemCount();
				if (analysisCount == 0) return "noItemsFoundMessage";
				if (movieList.size() == 0) {
//					for(Analysis analysis : RunwalkVideoApp.getApplication().getConversionTableModel().getItemList()) {
//						analysis.setStatusCode(CameraStatusCodes.MOVIE_NOT_FOUND);
//					}
					//					RunwalkVideoApp.getApplication().getConversionTableModel().fireTableDataChanged();
					return "noMoviesFoundMessage";
				}
				for (int i = analysisCount - 1; i >= 0; i--) {
					message("mapProgressMessage");
					if (movieList.size() > 0) {
						Analysis analysis = RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().getItem(i);
						long smallestDifference = Long.MAX_VALUE;
						for (File movie : movieList) {
							long timeDifference = Math.abs(analysis.getCreationDate().getTime() - movie.lastModified());
							if (timeDifference < smallestDifference) {
								smallestDifference = timeDifference;
//								RunwalkVideoApp.getApplication().getConversionTableModel().putMovie(analysis, movie);
								analysis.getRecording().setRecordingStatus(RecordingStatus.READY);
							}
						}
//						movieList.remove(RunwalkVideoApp.getApplication().getConversionTableModel().getMovie(analysis));
						//RunwalkVideoApp.getApplication().getConversionTableModel().fireTableRowsUpdated(i, i);
						//check whether there are enough files available..
					}
					//					else 
					//						return "noMoviesFoundMessage";
				}
			}
			else 
				return "noCameraFoundMessage";
			return "endMessage";
		}

		@Override
		protected void finished() {
			super.finished();
			RunwalkVideoApp.getApplication().getAnalysisOverviewTableModel().update();
			try {
				if (get().startsWith("no")) 
					throw new Exception(get()); 
				else {
					message(get());
					RunwalkVideoApp.getApplication().getSyncActions().setSyncEnabled(true);
				}
			}
			catch(Exception e) {
				RunwalkVideoApp.getApplication().getSyncActions().setSyncEnabled(false);
				errorMessage(e.getMessage());
			}

		}

	}