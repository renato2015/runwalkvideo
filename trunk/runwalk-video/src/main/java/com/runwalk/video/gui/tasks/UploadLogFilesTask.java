package com.runwalk.video.gui.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.runwalk.video.util.ClientHttpRequest;

public class UploadLogFilesTask extends AbstractTask<Void, Void> {

	private File logFile;

	public UploadLogFilesTask(File logFile) {
		super("upload");
		this.logFile = logFile;
	}

	@Override
	protected Void doInBackground() throws Exception {
		message("startMessage");
        InputStream serverInput = ClientHttpRequest.post(
                new java.net.URL(getResourceString("destfolder")), 
                new Object[] {"logfile", getLogFile()
                	});
        BufferedReader reader = new BufferedReader(new InputStreamReader(serverInput));
        String line = null;
        StringBuffer output = new StringBuffer();
        while ((line = reader.readLine()) != null) {
        	output.append(line);
        }
        getLogger().debug(output);
        reader.close();
        message("endMessage");
        return null;
	}
	
	public File getLogFile() {
		return logFile;
	}

}
