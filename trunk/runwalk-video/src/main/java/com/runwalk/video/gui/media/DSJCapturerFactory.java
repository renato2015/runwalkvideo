package com.runwalk.video.gui.media;

import java.util.List;

import com.google.common.collect.Lists;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilter;
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
				DSFilter[] filters = graph.listFilters();
				for (DSFilter filter : filters) {
					String filterInfoName = filter.getFilterInfo().getName();
					if (filterInfoName.equals(oldDevice.getPath())) {
						return graph;
					}
				}
			}
		}
		return null;
	}
	
	public boolean isActiveCapturer(String capturerName) {
		return getFiltergraphByFilter(capturerName) != null;
	}
		
	/** {@inheritDoc} */
	public IVideoCapturer initializeCapturer(String capturerName) {
		// initialize the capturer's native resources
		return new DSJCapturer(capturerName);
	}

	/** {@inheritDoc} */
	public synchronized List<String> getCapturers() {
		List<String> result = Lists.newArrayList();
		// query first with bit set to 0 to get quick listing of available capture devices
		DSFilterInfo[][] dsi = DSCapture.queryDevices(0 | DSCapture.SKIP_AUDIO);
		for(DSFilterInfo dsFilterInfo : dsi[0]) {
			String filterName = dsFilterInfo.getName();
			// remove filters that are already added to a filtergraph
			if (!"none".equals(filterName)) {
				result.add(filterName);
			}
		}
		return result;
	}

}
