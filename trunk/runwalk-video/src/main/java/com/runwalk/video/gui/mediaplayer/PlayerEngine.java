package com.runwalk.video.gui.mediaplayer;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdesktop.application.AbstractBean;
import org.jdesktop.application.Action;

import com.runwalk.video.RunwalkVideoApp;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.util.ApplicationUtil;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilter;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJException;
import de.humatic.dsj.DSMediaType;
import de.humatic.dsj.DSMovie;
import de.humatic.dsj.DSCapture.CaptureDevice;
import de.humatic.dsj.DSFilter.DSPin;

public class PlayerEngine extends AbstractBean {
	//TODO maak het uitlezen/opslaan van codecs mogelijk.. verplaats naar ApplicationSettings
	private final static List<DSFilterInfo> VIDEO_ENCODERS = Collections.unmodifiableList(
			Arrays.asList(
					DSFilterInfo.doNotRender(), 
					DSFilterInfo.filterInfoForProfile("RunwalkVideoApp"),
					DSFilterInfo.filterInfoForName("XviD MPEG-4 Codec")
			)
	);

	private final static Logger logger = Logger.getLogger(PlayerEngine.class);

	private boolean isPlaying = false;
	private Recording movieMetaInfo;

	private DSCapture dsCaptureGraph = null;
	private DSMovie dsMovieGraph = null;

	private float rate = 0.5f;
	private float[] rates = new float[] {0.05f, 0.10f, 0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.50f, 1.75f, 2.0f};
	private int rateIndex = Arrays.binarySearch(rates, rate);

	private DSFilterInfo selectedDevice = null;
	private int selectedFormat = -1;
	private DSFilterInfo[][] dsi;
	private DSPin previewOut, captureOut, activeOut;

	private float savedVolume;

	private DSFilterInfo selectedCaptureEncoder = VIDEO_ENCODERS.get(0);

	private DSFilterInfo selectedTranscoder = VIDEO_ENCODERS.get(2);
	/**
	 * Don't reject for uEye capture...
	 */
	private boolean rejectPauseFilter = false;

	private CameraDialog cameraSelectionDialog;
	
	private boolean enableCustomFramerate = false;
	private float framerate;

	public void setPlaying(boolean b) {
		isPlaying = b;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	@Action
	public void insertFilter(String name) {

		//		DSFilterInfo filterInfo = DSFilterInfo.filterInfoForCLSID(clsid);
		DSFilterInfo filterinfo = DSFilterInfo.filterInfoForName(name);
		if (getDSMovieGraph() != null) {
			DSFilter[] installedFilters = getDSMovieGraph().listFilters();
			DSFilter filter = getDSMovieGraph().addFilterToGraph(filterinfo);
			filter.showPropertiesDialog();
			//			filter.connectDownstream(filter.getPin(0,0), installedFilters[1].getPin(1, 0), true);
			//			installedFilters[3].getPins();
			//			filter.dumpConnections();
			//			filter.dumpConnections();
			//			logger.debug("Inserting filter before " + installedFilters[1].getName() + " after " + installedFilters[3].getName());
			//			getDSMovie().insertFilter(installedFilters[1], installedFilters[3], filterinfo);
			//			getDSMovie().insertFilter(, arg1, filterInfo);
		}
	}

	public Object[] queryCaptureDevices() {
		dsi = DSCapture.queryDevices(1);
		Object[] devices = new Object[dsi[0].length];
		for (int i = 0; i < dsi[0].length; i++) {
			devices[i] = dsi[0][i].getName();
		}
		return devices;
	}

	public void selectCaptureFormat() {
		DSMediaType[] mf = selectedDevice.getDownstreamPins()[0].getFormats();
		Object[] formats = new String[mf.length];

		for (int i = 0; i < mf.length; i++) {
			formats[i] = mf[i].getDisplayString() + " @ " + mf[i].getFrameRate();
		}
		String selectedFormat = (String)JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				"Kies het opnameformaat:",
				"Kies opnameformaat..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				formats,
				formats[0]);
		this.selectedFormat = Arrays.asList(formats).indexOf(selectedFormat);
		//		selectedFilter.getDownstreamPins()[0].setPreferredFormat(this.selectedFormat);
	}

	public void selectFormat() {
		/** Now the capture device can tell us, which pin is capture and which is preview **/
		CaptureDevice vDev = dsCaptureGraph.getActiveVideoDevice();
		previewOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_PREVIEW);
		captureOut = vDev.getDeviceOutput(DSCapture.CaptureDevice.PIN_CATEGORY_CAPTURE);
		/**
		We're only interested in the preview output for this demo, but a lot of devices (webcams amongst others)
		do not have a separate preview pin (preview is then built via a Tee filter)
		 **/
		activeOut = previewOut != null ? previewOut : captureOut;
		int pinIndex = activeOut.getIndex();
		logger.debug("Currently active pin : "  + activeOut.getName());
		DSFilterInfo.DSPinInfo usedPinInfo = selectedDevice.getDownstreamPins()[pinIndex];
		DSMediaType[] mf = usedPinInfo.getFormats();
		Object[] formats = new String[mf.length];

		for (int i = 0; i < mf.length; i++) {
			formats[i] = mf[i].getDisplayString() + " @ " + mf[i].getFrameRate();
		}
		String selectedFormat = (String)JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				"Kies het opnameformaat:",
				"Kies opnameformaat..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				formats,
				formats[0]);
		if (selectedFormat == null) {
			RunwalkVideoApp.getApplication().exit();
		} else {
			this.selectedFormat = Arrays.asList(formats).indexOf(selectedFormat);
			dsCaptureGraph.getActiveVideoDevice().setOutputFormat(activeOut, this.selectedFormat);
			dsCaptureGraph.getActiveVideoDevice().setOutputFormat(this.selectedFormat);
			logger.debug((previewOut != null ? "preview" : "capture")+" fps: "+vDev.getFrameRate(activeOut));
		}
	}

	public void setCaptureDevice(int selectedIndex) {
		this.selectedDevice = dsi[0][selectedIndex];
	}

	public void selectCaptureDevice() {
		Object[] devices = queryCaptureDevices();
		String selectedDevice =  (String) JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				"Kies een opname-apparaat:",
				"Kies opname-apparaat..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				devices ,
				devices[0]);
		if (selectedDevice == null) {
			RunwalkVideoApp.getInstance().exit();
		}
		setCaptureDevice(Arrays.asList(devices).indexOf(selectedDevice));
		//		this.selectedDevice = dsi[0][];
		//		selectedDevice.setPreferredFormat(DSFilterInfo.SHOW_DLG_SAVE);
	}

	@Action
	public void initCaptureGraph() {
		//initialize capture component.
		ApplicationUtil.disposeDSGraph(getCaptureGraph());
		//		setCaptureDevice();
		if (cameraSelectionDialog == null) {
			cameraSelectionDialog = new CameraDialog(RunwalkVideoApp.getApplication().getMainFrame());
		}
		cameraSelectionDialog.setLocationRelativeTo(RunwalkVideoApp.getApplication().getMainFrame());
		RunwalkVideoApp.getApplication().show(cameraSelectionDialog);
		dsCaptureGraph = new DSCapture(DSFiltergraph.D3D9, selectedDevice, false, DSFilterInfo.doNotRender(), getPropertyChangeListeners()[0]);
		dsCaptureGraph.lockAspectRatio(true);
	}


	public DSFilterInfo getSelectedDevice() {
		return selectedDevice;
	}

	public  void switchPlay() {
		if (isPlaying()) {
			pause();
		} else {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			ImageIcon playIcon = new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_pause.png"));
			RunwalkVideoApp.getApplication().getPlayerGUI().play_pause.setIcon(playIcon);
			RunwalkVideoApp.getApplication().getPlayerGUI().playTimer.restart();
			getDSMovieGraph().play();
			getDSMovieGraph().setRate(rate);
			setPlaying(true);
			RunwalkVideoApp.getApplication().showMessage("Afspelen aan "+ getRate() + "x gestart.");
		}
	}

	@Action
	public void toggleCamera() {
		if (getCaptureGraph().getState() == DSCapture.PREVIEW) {
			getCaptureGraph().stop();
		} else {
			getCaptureGraph().setPreview();
		}
	}


	public DSCapture getCaptureGraph() {
		return dsCaptureGraph;
	}

	@Action
	public void viewFilterProperties() {
		//		if (getMovieGraph() != null) {
		DSFilter[] filters = getCaptureGraph().listFilters();
		String[] filterInfo = new String[filters.length];
		for(int i  = 0; i < filters.length; i++) {
			filterInfo[i] = filters[i].getName();
		}
		String selectedString =  (String) JOptionPane.showInputDialog(
				RunwalkVideoApp.getApplication().getMainFrame(),
				"Kies een filter:",
				"Bekijk filter..",
				JOptionPane.PLAIN_MESSAGE,
				null,
				filterInfo,
				filterInfo[0]);
		if (selectedString == null) {

		} else {
			int selectedIndex = Arrays.asList(filterInfo).indexOf(selectedString);
			DSFilter selectedFilter = filters[selectedIndex];
			selectedFilter.showPropertiesDialog();
		}
		//		}
	}

	public DSFilterInfo getTranscoder() {
		return selectedTranscoder;
	}

	@Action
	public void setCaptureEncoder() {
		if (getCaptureGraph() != null) {
			String[] filterInfo = new String[VIDEO_ENCODERS.size()];
			int i = 0;
			for (DSFilterInfo fInfo : VIDEO_ENCODERS) {
				filterInfo[i] = fInfo.getName();
				i++;
			}

			String selectedEncoder =  (String) JOptionPane.showInputDialog(
					RunwalkVideoApp.getApplication().getMainFrame(),
					"Kies een video encoder: ",
					"Video encoder wijzigen..",
					JOptionPane.PLAIN_MESSAGE,
					null,
					filterInfo,
					this.selectedCaptureEncoder.getName());
			if (selectedEncoder == null) {

			} else {
				int selectedIndex = Arrays.asList(filterInfo).indexOf(selectedEncoder);
				this.selectedCaptureEncoder = VIDEO_ENCODERS.get(selectedIndex);
				logger.debug("Video encoder changed to " + this.selectedCaptureEncoder.getName());
			}
		}
	}

	@Action
	public void setFrameRate() {
		try {
			if (getDSMovieGraph() != null && getDSMovieGraph().getActive()) {
				enableCustomFramerate = true;
				String prefferredRate = JOptionPane.showInputDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
						"Geef een framerate in..", "Set framerate op capture device", JOptionPane.PLAIN_MESSAGE);
				framerate = Float.parseFloat(prefferredRate);
				getDSMovieGraph().setMasterFrameRate(framerate);
			} else {
				JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
						"Geen actieve filtergraph gevonden..");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(), 
					"Framerate was ongeldig: " + e.getMessage());
		}
	}

	public void initMovieComponent(Recording movie) {
		setMovie(movie);
		String path = null;
		try {
			path = movie.getVideoFilePath();
			if (getDSMovieGraph() == null) {
				createDSMovieGraph(path);
			} else {
				getDSMovieGraph().loadFile(path , 0);
			}
			//			insertFilter("LEAD Video Frame Rate Controller Filter (2.0)");
			//			insertFilter("LEAD Video Motion Detection Filter (2.0)");
		} catch(DSJException e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Er heeft zich een fout voorgedaan bij het openen van een filmpje.\n" +
					"Probeer het gerust nog eens opnieuw.",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);
			logger.error("Movie initialization failed.", e);
			disposeDSMovieGraph();
			createDSMovieGraph(path);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(RunwalkVideoApp.getApplication().getMainFrame(),
					"Het bestand dat u probeerde te openen kon niet worden gevonden",
					"Fout bij openen filmpje",
					JOptionPane.ERROR_MESSAGE);
		}
		RunwalkVideoApp.getApplication().showMessage("Bestand \"" + movie.getVideoFileName() + "\" afspelen...");
		RunwalkVideoApp.getApplication().getPlayerGUI().play_pause.setEnabled(true);
	}

	public void disposeDSMovieGraph() {
		ApplicationUtil.disposeDSGraph(getDSMovieGraph());
		dsMovieGraph = null;
	}

	private void createDSMovieGraph(String path) {
		logger.debug("Movie path opened : " + path);
		int flags = DSFiltergraph.D3D9 | DSMovie.INIT_PAUSED;
		if (enableCustomFramerate) {
			flags = flags | DSMovie.INIT_EDITABLE;
		}
		dsMovieGraph = new DSMovie(path, flags, getPropertyChangeListeners()[0]);
		if (enableCustomFramerate) {
			getDSMovieGraph().setMasterFrameRate(framerate);
		}
		getDSMovieGraph().lockAspectRatio(true);
		getDSMovieGraph().setRecueOnStop(true);
	}

	public void open() {

	}

	private void suspendPlayer() {
		RunwalkVideoApp.getApplication().getPlayerGUI().playTimer.stop();
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		ImageIcon pauseIcon = new ImageIcon(contextClassLoader.getResource("com/runwalk/video/gui/mediaplayer/resources/icons/player_play.png"));
		RunwalkVideoApp.getApplication().getPlayerGUI().play_pause.setIcon(pauseIcon);
		setPlaying(false);
	}

	public void pause() {
		suspendPlayer();
		getDSMovieGraph().pause();
		RunwalkVideoApp.getApplication().showMessage("Afspelen gepauzeerd");
	}

	public void stop() {
		suspendPlayer();
		getDSMovieGraph().stop();
		getDSMovieGraph().setTimeValue(0);
		RunwalkVideoApp.getApplication().showMessage("Afspelen gestopt");
	}

	public void forward() {
		if (isPlaying() && rateIndex < rates.length - 1) {
			setRate(rates[++rateIndex]);
		} else if (!isPlaying()) {
			switchPlay();
		}
		getDSMovieGraph().setRate(getRate());
	}

	public void backward() {
		if (rateIndex > 0) {
			setRate(rates[--rateIndex]);
			if (isPlaying()) {
				getDSMovieGraph().setRate(getRate());
			}
		} else {
			pause();
		}
	}

	public void nextItem() {
		if (isPlaying()) {
			switchPlay();
		}
		getMovie().sortKeyframes();
		for (Keyframe frame : getMovie().getKeyframes()) {
			if (frame.getPosition() > getPosition()) {
				setPosition(frame.getPosition());
				logger .debug("NEXT: Keyframe position " + getPosition() + " " + frame.getPosition());
				return;
			}
		}
	}

	public void previousItem() {
		if (isPlaying()) {
			switchPlay();
		}
		getMovie().sortKeyframes();
		for (int i = getMovie().getKeyframeCount()-1; i >= 0; i--) {
			Keyframe frame = getMovie().getKeyframes().get(i);
			if (frame.getPosition() < getPosition()) {
				setPosition(frame.getPosition());
				logger.debug(movieMetaInfo.getVideoFileName() + " Vorige: Keyframe " + i + " " + getPosition() + " " + frame.getPosition());
				return;
			}
		}
		setPosition(0);
	}

	public void showCaptureSettings() {
		getCaptureGraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_DEVICE);
	}

	public void showCameraSettings() {
		getCaptureGraph().getActiveVideoDevice().showDialog(DSCapture.CaptureDevice.WDM_CAPTURE);
	}

	public void increaseVolume() {
		getDSMovieGraph().setVolume(1.25f * getDSMovieGraph().getVolume());
	}

	public void decreaseVolume() {
		getDSMovieGraph().setVolume( getDSMovieGraph().getVolume() / 1.25f);
	}

	public boolean switchVolume() {
		if (getDSMovieGraph().getVolume() > 0) {
			savedVolume = getDSMovieGraph().getVolume();
			getDSMovieGraph().setVolume(0);
			RunwalkVideoApp.getApplication().showMessage("Volume staat af.");
			return true;
		}
		else {
			getDSMovieGraph().setVolume(savedVolume);
			RunwalkVideoApp.getApplication().showMessage("Volume staat terug aan.");
			return false;
		}
	}

	public void toggleFullScreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice selectedDevice = gs[gs.length-1];
		if (getDSMovieGraph() != null) {
			if (dsMovieGraph.isFullScreen()) {
				dsMovieGraph.leaveFullScreen();
			}
			else {
				dsMovieGraph.goFullScreen(selectedDevice, 2);
			}
		}
		else {
			if (dsCaptureGraph.isFullScreen()) {
				dsCaptureGraph.leaveFullScreen();
			}
			else {
				dsCaptureGraph.goFullScreen(selectedDevice, 2);
			}

		}
	}

	public void record() {
		getCaptureGraph().record();
		logger.debug((previewOut != null ? "preview" : "capture")+" fps: "+getCaptureGraph().getEffectiveFrameRate());
	}       

	public int getDuration(){
		return getDSMovieGraph().getDuration();
	}

	public int getPosition(){
		return getDSMovieGraph().getTime();
	}

	public int setPosition(int f) {
		int oldPosition = getPosition();
		//		getDSMovie().setSelection(f, getDSMovie().getDuration());
		//		getDSMovie().setPlaySelection(true);
		getDSMovieGraph().setTimeValue(f);
		firePropertyChange("position", oldPosition, f);
		return getPosition();
	}

	public DSMovie getDSMovieGraph() {
		return dsMovieGraph;
	}

	public Recording getMovie() {
		return movieMetaInfo;
	}

	private void setMovie(Recording movie) {
		this.movieMetaInfo = movie;
	}

	private void setRate(float rate) {
		this.rate = rate;
		RunwalkVideoApp.getApplication().showMessage("Afspelen aan " + ApplicationUtil.round(getRate(), 3) + " x");
	}

	private float getRate() {
		return rate;
	}

	public DSFilterInfo getCaptureEncoder() {
		return selectedCaptureEncoder;
	}

	public boolean rejectPauseFilter() {
		return rejectPauseFilter;
	}

	public void setRejectPauseFilter(boolean rejectPauseFilter2) {
		rejectPauseFilter = rejectPauseFilter2;
		logger.debug("Pause filter rejection now set to " + rejectPauseFilter2);
	}

}
