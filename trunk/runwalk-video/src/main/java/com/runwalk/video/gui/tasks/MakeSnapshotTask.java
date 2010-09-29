package com.runwalk.video.gui.tasks;

import java.util.Date;
import java.util.List;

import org.jdesktop.application.Task;

import com.runwalk.video.dao.DaoService;
import com.runwalk.video.entities.Keyframe;
import com.runwalk.video.entities.Recording;
import com.runwalk.video.gui.media.VideoPlayer;
import com.runwalk.video.util.AppUtil;

public class MakeSnapshotTask extends AbstractTask<Keyframe, Void> {

	private final VideoPlayer frontMostPlayer;
	private final List<VideoPlayer> videoPlayers;
	private final DaoService daoService;

	/**
	 * This {@link Task} will create {@link Keyframe}s for the given {@link VideoPlayer}s.
	 * It will return the created {@link Keyframe} for the given frontMostPlayer.
	 * 
	 * @author Jeroen Peelaerts
	 */
	public MakeSnapshotTask(DaoService daoService, VideoPlayer frontMostPlayer, List<VideoPlayer> videoPlayers) {
		super("makeSnapshot");
		this.daoService = daoService;
		this.videoPlayers = videoPlayers;
		this.frontMostPlayer = frontMostPlayer;
	}
	
	protected Keyframe doInBackground() throws Exception {
		message("startMessage", getVideoPlayers().size());
		Keyframe result = null;
		for (VideoPlayer videoPlayer : getVideoPlayers()) {
			videoPlayer.pauseIfPlaying();
			int position = videoPlayer.getKeyframePosition();
			// create a new Keyframe for the player's current recording
			final Recording recording = videoPlayer.getRecording();
			Keyframe keyframe = new Keyframe(recording, position);
			getDaoService().getDao(Keyframe.class).persist(keyframe);
			if (getFrontmostPlayer() == videoPlayer) {
				result = keyframe;
			}
		}
		Date date = new Date(result.getPosition());
		String formattedDate = AppUtil.formatDate(date, AppUtil.EXTENDED_DURATION_FORMATTER);
		message("endMessage", formattedDate);
		return result;
	}
	
	public DaoService getDaoService() {
		return daoService;
	}

	public VideoPlayer getFrontmostPlayer() {
		return frontMostPlayer;
	}

	public List<VideoPlayer> getVideoPlayers() {
		return videoPlayers;
	}
	
}
