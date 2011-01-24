package com.runwalk.video.gui.media;

import java.io.File;

import org.apache.log4j.Level;

import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.sink.FileSink;

public class DSJFileSinkCapturer extends DSJCapturer {

	private FileSink sink;
	private boolean controllable = false;

	DSJFileSinkCapturer(String capturerName) {
		super(capturerName, null);
	}

	@Override
	public void startRecording(File destFile) {
		if (sink == null || !controllable) {
			try {
				getLogger().log(Level.DEBUG, "Reading sink definitions from " + DSEnvironment.getSetupPath());
				sink = FileSink.fromXML("ATI_FileSink", DSEnvironment.getSetupPath());
				sink.setFlags(FileSink.DISPLAY_LOCAL);
				controllable = getFiltergraph().connectSink(sink) == FileSink.CONTROLABLE;
			} catch(DSJException e) {
				getLogger().error("Exception occurend when connnecting sink to filtergraph", e);
				// capture the normal way
			}
		}
		if (controllable) {
			getFiltergraph().record();
		}
		showFrameDropInfo();
	}

	@Override
	public void stopRecording() {
		if (!controllable) {
			getFiltergraph().removeSink();
		} else {
			super.stopRecording();
		}
	}
	
}
