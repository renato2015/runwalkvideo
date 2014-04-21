package com.runwalk.video.entities;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@Entity
@Table(name="movies")
public class Recording extends SerializableEntity<Recording> {
	
	public static final String RECORDING_STATUS = "recordingStatus";

	public static final String DURATION = "duration";

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
	
	@Column(name = "statusCode")
	private Integer statusCode = RecordingStatus.READY.getCode();

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="recording")
	private List<Keyframe> keyframes;

	protected Recording() { }

	public Recording(Analysis analysis) {
		// TODO should move this logic out of here!!
		Client client = analysis.getClient();
		int totalRecordings = 0;
		for (Analysis an : client.getAnalyses()) {
			totalRecordings += an.getRecordings().size();
		}
		this.videoFileName = buildFileName(analysis.getClient(), analysis.getCreationDate(), totalRecordings);
		this.analysis = analysis;
	}
	
	private String buildFileName(Client client, Date creationDate, int totalRecordings) {
		String date = AppUtil.formatDate(creationDate, AppUtil.FILENAME_DATE_FORMATTER);
		String prefix = totalRecordings == 0 ? "" : totalRecordings + "_";
		return new StringBuilder(prefix).append(client.getName()).append("_")
				.append(client.getFirstname()).append("_").append(date)
				.append(Recording.VIDEO_CONTAINER_FORMAT)
				.toString().replaceAll(" ", "_");
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
		this.duration = duration;
	}
	
	public void sortKeyframes() {
		Collections.sort(keyframes);
	}

	public List<Keyframe> getKeyframes() {
		return keyframes;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	public String getVideoFileName() {
		return videoFileName;
	}
	
	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
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
		int result = 0;
		if (!equals(o)) {
			if (lastModified != null && o != null && o.lastModified != null) {
				result = lastModified.compareTo(o.lastModified);
			} else {
				result = super.compareTo(o);
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [id=" + id + ", statuscode=" + statusCode	+ ", videoFileName=" + videoFileName + "]";
	}


}
