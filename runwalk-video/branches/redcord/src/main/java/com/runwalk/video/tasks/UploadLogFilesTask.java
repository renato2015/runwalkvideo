package com.runwalk.video.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.runwalk.video.util.ClientHttpRequest;

public class UploadLogFilesTask extends AbstractTask<Void, Void> {

	private final File logFile;
	private final String logFileUploadUrl;
	private final String application;

	public UploadLogFilesTask(File logFile, String logFileUploadUrl, String application) {
		super("uploadLogFiles");
		this.logFile = logFile;
		this.logFileUploadUrl = logFileUploadUrl;
		this.application = application;
	}

	@Override
	protected Void doInBackground() throws Exception {
		message("startMessage");
        InputStream serverInput = ClientHttpRequest.post(
                new java.net.URL(getLogFileUploadUrl()), 
                new Object[] {"logfile", getLogFile(),
                	"application", application
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
	
	public String getLogFileUploadUrl() {
		return logFileUploadUrl;
	}

}
