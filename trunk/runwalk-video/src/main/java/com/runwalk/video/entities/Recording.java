package com.runwalk.video.entities;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
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

import com.runwalk.video.entities.VideoFileImpl.CompressedVideoFile;
import com.runwalk.video.entities.VideoFileImpl.UncompressedVideoFile;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name="movies")
public class Recording extends SerializableEntity<Recording> {
	
	private static final String RECORDED = "recorded";

	private static final String COMPRESSED = "compressed";

	private static final String RECORDING_STATUS = "recordingStatus";

	private static final String DURATION = "duration";

	private static final String KEYFRAME_COUNT = "keyframeCount";

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

	@Transient
	private boolean compressed, recorded;

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

	public String getFormattedDuration(SimpleDateFormat format) {
		return AppUtil.formatDate(new Date(getDuration()), format);
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
	
	public void setDuration(long duration) {
		this.firePropertyChange(DURATION, this.duration, this.duration = duration);
	}
	
	public void addKeyframe(int stamp) {
		Keyframe snapshot = new Keyframe(this, stamp);
		AppUtil.persistEntity(snapshot);
		keyframes.add(snapshot);
		firePropertyChange(KEYFRAME_COUNT, getKeyframeCount()-1, getKeyframeCount());
	}

	public void sortKeyframes() {
		Collections.sort(keyframes);
	}

	public List<Keyframe> getKeyframes() {
		if (this.keyframes == null) {
			this.keyframes = Collections.emptyList();
		}
		return Collections.unmodifiableList(keyframes);
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
			this.firePropertyChange(RECORDING_STATUS, this.recordingStatus, this.recordingStatus = status);
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
					firePropertyChange(RECORDING_STATUS, this.recordingStatus, this.recordingStatus = RecordingStatus.COMPRESSED);
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
						firePropertyChange(RECORDING_STATUS, this.recordingStatus, this.recordingStatus = RecordingStatus.UNCOMPRESSED);
						videoFile = getUncompressedVideoFile();
					}
				}
			} else if (getUncompressedVideoFile().canReadAndExists()) {
				firePropertyChange(RECORDING_STATUS, this.recordingStatus, this.recordingStatus = RecordingStatus.UNCOMPRESSED);
				videoFile = getUncompressedVideoFile();
			} else {
				Logger.getLogger(Recording.class).warn("No videofile found for recording with filename " + getVideoFileName());
				firePropertyChange(RECORDING_STATUS, this.recordingStatus, this.recordingStatus = RecordingStatus.NON_EXISTANT_FILE);
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
		boolean compressed = getRecordingStatus() == RecordingStatus.COMPRESSED && getCompressedVideoFile().canReadAndExists();
		firePropertyChange(COMPRESSED, this.compressed, this.compressed = compressed);
		return this.compressed;
	}

	public boolean isRecorded() {
		boolean recorded = isUncompressed() || isCompressed();
		firePropertyChange(RECORDED, this.recorded, this.recorded = recorded);
		return this.recorded;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getVideoFileName() == null) ? 0 : getVideoFileName().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Recording other = (Recording) obj;
			result = getVideoFileName() != null ? getVideoFileName().equals(other.getVideoFileName()) : other.getVideoFileName() == null;
			result &= getId() != null ? getId().equals(other.getId()) : result;
		}
		return result;
	}

	public int compareTo(Recording o) {
		return this.equals(o) ? 0 : lastModified.compareTo(o.lastModified);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", statuscode=" + statuscode	+ ", videoFileName=" + videoFileName + "]";
	}


}
