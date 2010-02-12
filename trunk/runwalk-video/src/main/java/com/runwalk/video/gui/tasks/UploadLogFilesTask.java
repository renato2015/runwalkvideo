package com.runwalk.video.gui.tasks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.runwalk.video.util.AppSettings;
import com.runwalk.video.util.ClientHttpRequest;

public class UploadLogFilesTask extends AbstractTask<Void, Void> {

	public UploadLogFilesTask() {
		super("upload");
	}

	@Override
	protected Void doInBackground() throws Exception {
		message("startMessage");
        InputStream serverInput = ClientHttpRequest.post(
                new java.net.URL(getResourceString("destfolder")), 
                new Object[] {"logfile", AppSettings.getInstance().getLogFile()
                	});
        BufferedReader reader = new BufferedReader(new InputStreamReader(serverInput));
        String line = null;
        StringBuffer output = new StringBuffer();
        while ((line = reader.readLine()) != null) {
        	output.append(line);
        }
        getLogger().debug(output);
        message("endMessage");
        return null;
	}

}
