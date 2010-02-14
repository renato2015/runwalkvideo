package com.runwalk.video.entities;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.runwalk.video.util.AppUtil;

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name="analysis")
public class Analysis extends SerializableEntity<Analysis> {

	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne/*(cascade={CascadeType.MERGE, CascadeType.REFRESH})*/
	@JoinColumn(name="clientid", nullable=false)
	private Client client;
	
	@OneToOne
	@JoinColumn(name="articleid")
	private Articles article;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="movieid", unique = true)
	private Recording recording;
	
	@Column(name="date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date creationDate;

	@Lob
	private String comments;
	
	protected Analysis() {
		super();
	}
	
	public Analysis(Client client) {
		creationDate = new Date();
		this.client = client;
		createRecording();
	}
	
	public Recording createRecording() {
		return recording = new Recording(getRecordingFileName());
	}
	
	public Client getClient() {
		return client;
	}
	
	public Articles getArticle() {
		return this.article;
	}
	
	/**
	 * @deprecated
	 * @return timestamp in a string
	 */
	public String getTimeStamp() {
		return AppUtil.formatDate(getCreationDate(), AppUtil.EXTENDED_DATE_FORMATTER);
	}
	
	public Date getCreationDate() {
		return AppUtil.granularity(creationDate, Calendar.MILLISECOND);
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setArticle(Articles art) {
		this.article = art;
	}


	public Recording getRecording() {
		return recording;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((getCreationDate() == null) ? 0 : getCreationDate().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Analysis other = (Analysis) obj;
			result = getCreationDate() != null ? getCreationDate().equals(other.getCreationDate()) : other.getCreationDate() == null;
			result &= getId() != null ? getId().equals(other.getId()) : result;
		}
		return result;
	}

	public int compareTo(Analysis analysis) {
		return this.equals(analysis) ? 0 : getId().compareTo(analysis.getId());
	}

	@Override
	public String toString() {
		return "Analysis [client=" + client.getFirstname() + " " + client.getName() + ", creationDate=" + creationDate	+ ", id=" + id + "]";	
	}

	/**
	 *  1. Vanaf het moment dat je de filename hebt, zou je ook een link moeten hebben naar een Movie object.
	 *  2. statuscode is eigenlijk ook een veld van Movie object..
	 *  3. alle calls gerelateerd naar toestand van het bestand zou je naar de Recording entity moeten sturen (delegeren)
	 *  
	 *  TODO alle spaties in de bestandsnaam zouden naar een _ moeten geconverteerd worden.
	 *  
	 * @return De fileName van het terug te vinden filmpje
	 */
	private String getRecordingFileName() {
		String date = AppUtil.formatDate(getCreationDate(), AppUtil.DATE_FORMATTER);
		int analysisCount = getClient().getAnalyses().size();
		String prefix = analysisCount == 0 ? "" : analysisCount + "_";
		return prefix + getClient().getName() + "_" + getClient().getFirstname() + "_" + date + Recording.VIDEO_CONTAINER_FORMAT;
	}
	
	public boolean hasRecording() {
		return getRecording() != null && getRecording().isRecorded();
	}

	public boolean hasCompressableRecording() {
		return getRecording() != null && getRecording().isCompressable();
	}

	public boolean hasCompressedRecording() {
		return getRecording() != null && getRecording().isCompressed();
	}

}
