package com.runwalk.video.entities;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.runwalk.video.io.VideoFileManager;
import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@Entity
@Table(name="movies")
public class Recording extends SerializableEntity<Recording> {
	
	public static final String RECORDED = "recorded";

	public static final String COMPRESSED = "compressed";

	public static final String RECORDING_STATUS = "recordingStatus";

	public static final String DURATION = "duration";

	public static final String KEYFRAME_COUNT = "keyframeCount";

	public static final String VIDEO_CONTAINER_FORMAT = ".avi";
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne/*(cascade={CascadeType.MERGE, CascadeType.REFRESH})*/
	@JoinColumn(name="analysisid", nullable=false)
	private Analysis analysis;

	@Column(name = "newfilename")
	private String videoFileName;

	private long duration;

	@Column(name="lastmodified")
	private Long lastModified;
	
	private Integer statusCode = RecordingStatus.READY.getCode();

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="recording")
	private List<Keyframe> keyframes;

	/**
	 * Transient field containing the actual status code. The value of this code is initialized at {@link PostLoad} time.
	 */
	@Transient
	private RecordingStatus recordingStatus;

	protected Recording() { }

	public Recording(Analysis analysis) {
		String date = AppUtil.formatDate(analysis.getCreationDate(), AppUtil.FILENAME_DATE_FORMATTER);
		Client client = analysis.getClient();
		int totalRecordings = 0;
		for (Analysis an : client.getAnalyses()) {
			totalRecordings += an.getRecordingCount();
		}
		String prefix = totalRecordings == 0 ? "" : totalRecordings + "_";
		StringBuilder fileNameBuilder = new StringBuilder(prefix).append(client.getName()).append("_")
		.append(client.getFirstname()).append("_").append(date).append(Recording.VIDEO_CONTAINER_FORMAT);
		this.videoFileName = fileNameBuilder.toString().replaceAll(" ", "_");
		this.analysis = analysis;
	}

	public Long getId() {
		return id;
	}
	
	public Analysis getAnalysis() {
		return analysis;
	}

	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.firePropertyChange(DURATION, this.duration, this.duration = duration);
	}
	
	public void addKeyframe(Keyframe keyframe) {
		keyframes.add(keyframe);
		firePropertyChange(KEYFRAME_COUNT, getKeyframeCount() - 1, getKeyframeCount());
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
	
	@PostLoad
	@SuppressWarnings("unused")
	private void postLoad() {
		// initialize transient fields
		if (statusCode != null) {
			recordingStatus = RecordingStatus.getByCode(statusCode);
		}
	}

	/**
	 * This method should set the right {@link RecordingStatus} constant for this {@link Recording} 
	 * according to the {@link File} returned by {@link VideoFileManager#getVideoFile(Recording)}.
	 * The status code will not be persisted to the database if it is found to be erroneous, because
	 * the video files are always local to an application's file system.
	 * 
	 * @param recordingStatus The status to be applied
	 * @see VideoFileManager#getVideoFile(Recording)
	 */
	public void setRecordingStatus(RecordingStatus recordingStatus) {
//		boolean wasRecorded = isRecorded();
		if (recordingStatus != RecordingStatus.NON_EXISTANT_FILE && this.recordingStatus != null) {
			firePropertyChange(RECORDING_STATUS, this.recordingStatus, this.recordingStatus = recordingStatus);
			if (!recordingStatus.isErroneous()) {
				// don't change the statuscode if it is erroneous.
				this.statusCode = recordingStatus.getCode();
			}
		} else {
			this.recordingStatus = recordingStatus;
		}
//		firePropertyChange(RECORDED, wasRecorded, isRecorded());
	}
	
	public String getVideoFileName() {
		return videoFileName;
	}

	public boolean isCompressable() {
		return getRecordingStatus() != RecordingStatus.COMPRESSED && 
		getRecordingStatus() != RecordingStatus.FILE_NOT_ACCESSIBLE &&
		getRecordingStatus() != RecordingStatus.READY && 
//		getRecordingStatus() != RecordingStatus.NON_EXISTANT_FILE &&
		getRecordingStatus() != RecordingStatus.DSJ_ERROR;
	}
	
	public  boolean isUncompressed() {
		return getRecordingStatus() == RecordingStatus.UNCOMPRESSED;
	}

	public boolean isCompressed() {
		return getRecordingStatus() == RecordingStatus.COMPRESSED;
	}

	public boolean isRecorded() {
		return isUncompressed() || isCompressed();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + ((getVideoFileName() == null) ? 0 : getVideoFileName().hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Recording other = (Recording) obj;
			result = getVideoFileName() != null ? getVideoFileName().equals(other.getVideoFileName()) : other.getVideoFileName() == null;
		}
		return result;
	}

	public int compareTo(Recording o) {
		return this.equals(o) ? 0 : lastModified.compareTo(o.lastModified);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", statuscode=" + statusCode	+ ", videoFileName=" + videoFileName + "]";
	}


}
