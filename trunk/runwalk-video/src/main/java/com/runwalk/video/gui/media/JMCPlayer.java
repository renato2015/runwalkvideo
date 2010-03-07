package com.runwalk.video.gui.media;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.net.URI;

import com.sun.media.jmc.MediaProvider;
import com.sun.media.jmc.control.AudioControl;
import com.sun.media.jmc.control.VideoRenderControl;
import com.sun.media.jmc.event.VideoRendererEvent;
import com.sun.media.jmc.event.VideoRendererListener;

public class JMCPlayer implements IVideoPlayer, VideoRendererListener {

	AudioControl ac;
	VideoRenderControl vrc;
	MediaProvider mp = null;

	public JMCPlayer() {
		this.mp = new MediaProvider();

		ac = mp.getControl(AudioControl.class);
		vrc = mp.getControl(VideoRenderControl.class);

		vrc.addVideoRendererListener(this);
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

	public void loadFile(String path) {
		try {
			mp.setSource(new URI(path));
		} catch (Exception e) {
//			getLogger().error(e);
			e.printStackTrace();
		}
	}

	public void setMuted(boolean mute) {
		ac.setMute(mute);
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
		// TODO Auto-generated method stub

	}

	public float getVolume() {
		return ac.getVolume();
	}

	public boolean isMuted() {
		return ac.isMuted();
	}

	public void setPlayRate(float rate) {
		mp.setRate(rate);
	}

	public void setVolume(float volume) {
		ac.setVolume(volume); 
	}

	public Container getComponent() {
		return null;
	}

	public Frame getFullscreenFrame() {
		return null;
	}

	public String getName() {
		return "JMC player";
	}

	public boolean isActive() {
		return false;
	}

	public void toggleFullScreen(GraphicsDevice graphicsDevice, boolean b) {
		//FIXME not supported yet??
	}

	
}
