package com.runwalk.video.media.jmc;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.runwalk.video.media.IVideoPlayer;
import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.AudioControl;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.VideoRendererEvent;
import com.sun.media.jmc.event.VideoRendererListener;

public class JMCPlayer implements IVideoPlayer, VideoRendererListener {

	AudioControl ac;
	VideoRenderControl vrc;
	MediaProvider mp = null;

	private BufferedImage bufferedImage = null;
	private Graphics2D g2d = null;
	private Dimension videoSize = null;
	private int imageType = BufferedImage.TYPE_INT_RGB;

	private int vw = 0;
	private int vh = 0;
	
	private JPanel videoPanel;

	public JMCPlayer(float rate) {
		this.mp = new MediaProvider();
		setPlayRate(rate);
	}
	
	public void startRunning() {
		
	}

	public void stopRunning() {

	}
	
	public boolean loadFile(File videoFile) {
		return loadVideo(videoFile.toURI().toString());
	}
	
	public boolean loadVideo(String path) {
		boolean rebuilt = false;
		try {
			mp.setSource(new File(path).toURI());
			if (vrc == null) {
				ac = mp.getControl(AudioControl.class);
				vrc = mp.getControl(VideoRenderControl.class);
				vrc.addVideoRendererListener(this);
				videoPanel = new JPanel();
				videoPanel.setPreferredSize(new Dimension(786, 584));
			}
		} catch (Exception e) {
			Logger.getLogger(JMCPlayer.class).error(e);
			rebuilt = true;
		}
		return rebuilt;
	}

	public void dispose() {
		if (vrc != null) {
			vrc.removeVideoRendererListener(this);
		}
		if (mp != null) {
			//			mp.removeBufferDownloadListener(this);
			setPosition(0);
			mp.setSource(null); //this will call mp.close()
		}
	}

	public int getDuration() {
		return (int) this.mp.getDuration();
	}

	public float getPlayRate() {
		return (float) mp.getRate();
	}

	public int getPosition() {
		return (int) mp.getMediaTime();
	}

	public void setPosition(int pos) {
		mp.setMediaTime(pos);
	}

	public void play() {
		this.mp.play();
	}

	public void pause() {
		this.mp.pause();
	}

	public void stop() {
		pause();
		setPosition(0);
	}

	public void videoFrameUpdated(VideoRendererEvent arg0) {
		paintBufferedImage();
	}

	/**
	 * Paints the newest video frame onto a BufferedImage.
	 */
	public void paintBufferedImage()
	{
		if (bufferedImage == null || videoSize == null || vw <= 0 || vh <= 0)
		{
			setupBufferedImage();
			return;
		}
		
		g2d = bufferedImage.createGraphics();
		vrc.paintVideoFrame(g2d, new Rectangle(0, 0, vw, vh));
		videoPanel.paintComponents(g2d);
		g2d.dispose();
	}

	/**
	 * Creates a BufferedImage the same size as the video and initializes the PImage pixel buffer.
	 */
	public void setupBufferedImage()
	{
		videoSize = vrc.getFrameSize();
		bufferedImage = new BufferedImage((int) videoSize.getWidth(), (int) videoSize.getHeight(), imageType);

		vw = (int) videoSize.getWidth();
		vh = (int) videoSize.getHeight();
	}

	public float getVolume() {
		return ac.getVolume();
	}

	public void setPlayRate(float rate) {
		mp.setRate(rate);
	}

	public void setVolume(float volume) {
		ac.setVolume(volume); 
	}

	public Component getComponent() {
		return videoPanel;
	}

	public String getTitle() {
		return "JMC player";
	}

	public boolean isActive() {
		return true;
	}

	public BufferedImage getImage() {
		return bufferedImage;
	}

	@Override
	public Dimension getDimension() {
		return new Dimension(getImage().getWidth(), getImage().getHeight());
	}

	public void clearOverlay() {
		// TODO Auto-generated method stub
	}

	public boolean isFullScreenEnabled() {
		return false;
	}

	public boolean isFullScreen() {
		return false;
	}

	public void setFullScreen(boolean fullScreen, Integer monitorId) {
		throw new UnsupportedOperationException("not implemented");
	}

	public boolean isVisible() {
		return videoPanel.isVisible();
	}

	public void setVisible(boolean visible) {
		videoPanel.setVisible(visible);
	}

	public void toFront() {
		//videoPanel.toFront();
	}

	public Frame getFullscreenFrame() {
		return null;
	}

	@Override
	public void setOverlayImage(BufferedImage image, Color alphaColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNativeWindowing() {
		return false;
	}

}
