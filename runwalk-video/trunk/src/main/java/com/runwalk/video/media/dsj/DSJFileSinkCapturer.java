package com.runwalk.video.media.dsj;


import org.apache.log4j.Level;

import com.runwalk.video.media.settings.VideoCapturerSettings;

import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.sink.FileSink;

/**
 * This class has some extended capabilities for recording to some 
 * non default formats using a customer encoders and muxers.
 * 
 * @author Jeroen Peelaerts
 */
public class DSJFileSinkCapturer extends DSJCapturer {

	private FileSink sink;
	private boolean controllable = false;

	DSJFileSinkCapturer(VideoCapturerSettings videoCapturerSettings) {
		super(videoCapturerSettings);
	}

	@Override
	public void startRecording(String videoPath) {
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
