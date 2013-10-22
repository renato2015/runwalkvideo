package com.runwalk.video.media.dsj;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.nio.file.Paths;

import com.runwalk.video.media.settings.VideoPlayerSettings;

import de.humatic.dsj.DSConstants;
import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSGraph;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.src.AsyncSource;

public class DSJAsyncPlayer extends AbstractDSJPlayer<DSGraph> implements PropertyChangeListener {
	
	private AsyncSource asyncSource;

	public DSJAsyncPlayer(VideoPlayerSettings videoPlayerSettings) {
		super(videoPlayerSettings);
	}

	public boolean loadVideo(String path) {
		// construct a new filtergraph from scratch?
		try {
			if (asyncSource != null) {
				dispose();
			}
			URI uri = Paths.get(path).toUri();
			asyncSource = new AsyncSource(uri.toURL(), this);
			DSGraph graph = asyncSource.createGraph(FLAGS);
			setFiltergraph(graph);
		} catch (Exception e) {
			getLogger().error("Recording initialization failed.", e);
		}
		// prolly will need to rebuild the graph??
		return true;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		int eventType = DSJUtils.getEventType(evt);
		String eventToString = DSConstants.eventToString(DSJUtils.getEventValue_int(evt));
		switch(eventType) {
		case DSFiltergraph.GRAPH_EVENT: System.out.println("Graph Event " + eventToString); break;
		case DSFiltergraph.GRAPH_ERROR: System.out.println("Graph Error " + eventToString); break;
		case DSFiltergraph.BUFFERING: System.out.println("Buffering.."); break;
		case DSFiltergraph.SOURCE_STATE_CHANGED: System.out.println("State changed: " +  eventToString); break;
		default: System.out.println(DSConstants.eventToString(eventType) + "(" + eventType + "): " + DSJUtils.getEventValue_int(evt));
		}
		
	}
	
}
