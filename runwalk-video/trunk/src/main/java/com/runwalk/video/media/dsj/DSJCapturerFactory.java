package com.runwalk.video.media.dsj;

import java.util.List;
import java.util.ResourceBundle;

import org.jdesktop.application.utils.PlatformType;

import com.google.common.collect.Lists;
import com.runwalk.video.media.IVideoCapturer;
import com.runwalk.video.media.VideoCapturerFactory;
import com.runwalk.video.settings.VideoCapturerSettings;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSEnvironment;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;

public class DSJCapturerFactory extends VideoCapturerFactory.Adapter {
	
	private static final String DSJ_UNLOCK_NAME = "dsj.unlockName";
	private static final String DSJ_CODE3 = "dsj.code3";
	private static final String DSJ_CODE2 = "dsj.code2";
	private static final String DSJ_CODE1 = "dsj.code1";

	public DSJCapturerFactory() {
		// initialize and unlock dsj dll at class loading time
		DSEnvironment.setDebugLevel(4);
		// get dsj unlock code from resource bundle, processed by maven at compile time
		String packageName = DSJComponent.class.getPackage().getName();
		String className = DSJComponent.class.getSimpleName();
		// get class resource bundle using the bsaf naming convention
		ResourceBundle bundle = ResourceBundle.getBundle(packageName + ".resources." + className);
		String unlockName = bundle.getString(DSJ_UNLOCK_NAME);
		Long code1 = Long.parseLong(bundle.getString(DSJ_CODE1));
		Long code2 = Long.parseLong(bundle.getString(DSJ_CODE2));
		Long code3= Long.parseLong(bundle.getString(DSJ_CODE3));
		DSEnvironment.unlockDLL(unlockName, code1, code2, code3);
	}
	
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
	protected IVideoCapturer initializeCapturer(VideoCapturerSettings videoCapturerSettings) {
		// initialize the capturer's native resources
		return new DSJCapturer(videoCapturerSettings);
		//return new DSJFileSinkCapturer(capturerName);
	}

	/** {@inheritDoc} */
	public synchronized List<String> getVideoCapturerNames() {
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

	protected boolean isPlatformSupported(PlatformType platformType) {
		return platformType == PlatformType.WINDOWS;
	}

}
