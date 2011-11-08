package com.runwalk.video.gui.media;

import java.util.List;

import com.google.common.collect.Lists;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;

public class DSJCapturerFactory extends VideoCapturerFactory {

	/**
	 * Find a {@link DSFiltergraph} that contains a filter with the specified name.
	 * 
	 * @param filterInfo The name of the filter
	 * @return The {@link DSFiltergraph} that contains the filter
	 */
	private DSFiltergraph getFiltergraphByFilter(String filterInfo) {
		if (filterInfo != null && !filterInfo.equals("none")) {
			DSFilterInfo oldDevice = DSFilterInfo.filterInfoForName(filterInfo);
			DSFiltergraph[] activeGraphs = DSEnvironment.getActiveGraphs();
			for (int i = 0; activeGraphs != null && i < activeGraphs.length; i++) {
				DSFiltergraph graph = activeGraphs[i];
				if (graph.findFilterByName(oldDevice.getPath()) != null) {
					return graph;
				}
			}
		}
		return null;
	}
		
	/** {@inheritDoc} */
	public IVideoCapturer initializeCapturer(String capturerName, String captureEncoderName) {
		// initialize the capturer's native resources
		return new DSJCapturer(capturerName, captureEncoderName);
	}

	/** {@inheritDoc} */
	public synchronized List<String> getCapturerNames() {
		List<String> result = Lists.newArrayList();
		// query first with bit set to 0 to get quick listing of available capture devices
		DSFilterInfo[][] dsi = DSCapture.queryDevices(0 | DSCapture.SKIP_AUDIO);
		for(DSFilterInfo dsFilterInfo : dsi[0]) {
			String filterName = dsFilterInfo.getName();
			// remove filters that are already added to a filtergraph
			DSFiltergraph graph = getFiltergraphByFilter(filterName);
			boolean graphNotRunning = graph == null || !graph.asComponent().isShowing();
			if (!"none".equals(filterName) && graphNotRunning) {
				result.add(filterName);
			}
		}
		return result;
	}

}
