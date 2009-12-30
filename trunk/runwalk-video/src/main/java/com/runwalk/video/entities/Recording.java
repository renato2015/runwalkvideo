package com.runwalk.video.entities;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

import com.runwalk.video.entities.VideoFile.CompressedVideoFile;
import com.runwalk.video.entities.VideoFile.UncompressedVideoFile;
import com.runwalk.video.gui.actions.RecordingStatus;
import com.runwalk.video.util.ApplicationUtil;

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name="movies")
public class Recording extends SerializableEntity<Recording> {
	
	public static final String VIDEO_CONTAINER_FORMAT = ".avi";
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="oldfilename")
	private String oldFileName;

	@Column(name = "newfilename")
	private String videoFileName;

	private long duration;

	@Column(name="lastmodified")
	private Long lastModified;
	
	private Integer statuscode = RecordingStatus.NON_EXISTANT_FILE.getCode();

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="movie")
	private List<Keyframe> keyframes;

	@Transient
	private VideoFile uncompressedVideoFile, compressedVideoFile, videoFile;

	/**
	 * Transient field containing the actual status code. The value of this code is copied back
	 * to the persistent field at {@link PreUpdate} time.
	 */
	@Transient
	private RecordingStatus recordingStatus;

	protected Recording() { }

	public Recording(String fileName) {
		this.videoFileName = fileName;
		refreshVideoFiles();
	}

	public Recording(long lastModified, String oldFileName, String newFileName) {
		this(newFileName);
		this.oldFileName = oldFileName;
		this.lastModified = lastModified;
	}

	public Long getId() {
		return id;
	}

	public String formatDuration(SimpleDateFormat formatter) {
		return ApplicationUtil.formatDate(new Date(getDuration()), formatter);
	}

	public long getDuration() {
		if (duration == 0 && isRecorded()) {
			try {
				duration = getVideoFile().getDuration();
			} catch (Exception e) {
				Logger.getLogger(getClass()).error("Failed to read meta info from " + getClass().getSimpleName() +  " with name " + getVideoFileName(), e);
			}
		}
		return duration;
	}
	
	public void addKeyframe(Keyframe frame) {
		keyframes.add(frame);
	}

	public void sortKeyframes() {
		Collections.sort(keyframes);
	}

	public List<Keyframe> getKeyframes() {
		if (this.keyframes == null) {
			return Collections.emptyList();
		}
		return new ArrayList<Keyframe>(this.keyframes);
	}

	public int getKeyframeCount() {
		return getKeyframes().size();
	}

	public RecordingStatus getRecordingStatus() {
		return recordingStatus;
	}

	/**
	 * This method should set the right status code according to the available recordings on the disk.
	 * 
	 * @param status The status to be applied
	 */
	public void setRecordingStatus(RecordingStatus status) {
		if (status.refreshNeeded()) {
			videoFile = null;
			cacheVideoFile();
		} else {
			this.recordingStatus = status;
		}
		this.statuscode = recordingStatus.getCode();
	}

	@PostLoad
	private void refreshVideoFiles() {
		videoFile = null;
		compressedVideoFile = new CompressedVideoFile(getVideoFileName());
		uncompressedVideoFile = new UncompressedVideoFile(getVideoFileName());
		cacheVideoFile();
	}

	private VideoFile cacheVideoFile() {
		if (videoFile == null || !videoFile.canReadAndExists())  {
			if (getCompressedVideoFile().exists()) {
				if (getCompressedVideoFile().canRead()) {
					recordingStatus = RecordingStatus.COMPRESSED;
					videoFile = getCompressedVideoFile();
					/*try {
						if (hasDuplicateFiles() && getCompressedVideoFile().getDuration() != getUncompressedVideoFile().getDuration()) {
							 // bestand is leesbaar maar de lengtes van de twee versies zijn niet hetzelfde. Best om de compresseerde versie in quarantaine te zetten..
							getCompressedVideoFile().delete();
							recordingStatus = RecordingStatus.UNCOMPRESSED;
							videoFile = getUncompressedVideoFile();
						}
					} catch (DSJException e) {
						 // een exception hier wil zeggen dat de uncompressed version om zeep is.. voorlopig gewoon loggen
						Logger.getLogger(getClass()).error("Failed to read meta info from " + getClass().getSimpleName() +  " with name " + getUncompressedVideoFile().getName(), e);
					}*/
				} else {
					//bestand is helemaal niet leesbaar, best verwijderen..
					//TODO kuis code op..
					if (getCompressedVideoFile().delete()) {
						videoFile = cacheVideoFile();
					} else {
						recordingStatus = RecordingStatus.UNCOMPRESSED;
						videoFile = getUncompressedVideoFile();
					}
				}
			} else if (getUncompressedVideoFile().canReadAndExists()) {
				recordingStatus = RecordingStatus.UNCOMPRESSED;
				videoFile = getUncompressedVideoFile();
			} else {
				Logger.getLogger(Recording.class).warn("No videofile found for recording with filename " + getVideoFileName());
				recordingStatus = RecordingStatus.NON_EXISTANT_FILE;
				return videoFile = null;
			}
		}
		return videoFile;
	}
	
	public String getVideoFileName() {
		return videoFileName;
	}

	public String getVideoFilePath() throws FileNotFoundException {
		return getVideoFile().getAbsolutePath();
	}
	
	private VideoFile getVideoFile() throws FileNotFoundException {
		VideoFile videoFile = cacheVideoFile();
		if (videoFile == null) {
			throw new FileNotFoundException();
		}
		return videoFile;
	}

	public VideoFile getUncompressedVideoFile() {
		return uncompressedVideoFile;
	}

	public VideoFile getCompressedVideoFile() {
		return compressedVideoFile;
	}

	public boolean hasDuplicateFiles() {
		return getCompressedVideoFile().exists() && getUncompressedVideoFile().exists();
	}

	public boolean isCompressable() {
		return getRecordingStatus() != RecordingStatus.COMPRESSED && 
		getRecordingStatus() != RecordingStatus.FILE_NOT_ACCESSIBLE &&
		getRecordingStatus() != RecordingStatus.NON_EXISTANT_FILE &&
		getRecordingStatus() != RecordingStatus.DSJ_ERROR && 
		getUncompressedVideoFile().canReadAndExists();
	}
	
	public boolean isUncompressed() {
		return getRecordingStatus() == RecordingStatus.UNCOMPRESSED && getUncompressedVideoFile().canReadAndExists();
	}

	public boolean isCompressed() {
		return getRecordingStatus() == RecordingStatus.COMPRESSED && getCompressedVideoFile().canReadAndExists();
	}

	public boolean isRecorded() {
		return isUncompressed() || isCompressed();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((videoFileName == null) ? 0 : videoFileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Recording) {
			Recording other = (Recording) obj;
			return getId().equals(other.getId()) && getVideoFileName().equals(other.getVideoFileName());
		}
		return false;
	}

	public int compareTo(Recording o) {
		return lastModified.compareTo(o.lastModified);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", statuscode=" + statuscode	+ ", videoFileName=" + videoFileName + "]";
	}



}
