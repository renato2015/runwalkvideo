package com.runwalk.video.entities;

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

import com.runwalk.video.util.ApplicationUtil;

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name="analysis")
public class Analysis extends SerializableEntity<Analysis> {

	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long Id;
	
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
		return recording = new Recording(getMovieFileName());
	}
	
	public Client getClient() {
		return client;
	}
	
	public Articles getArticle() {
		return this.article;
	}
	
	public String getTimeStamp() {
		return ApplicationUtil.formatDate(getCreationDate(), ApplicationUtil.EXTENDED_DATE_FORMATTER);
	}
	
	public Date getCreationDate() {
		return creationDate;
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

	public int compareTo(Analysis analysis) {
		if (getCreationDate() == null || analysis.getCreationDate() == null) {
			return -1;
		}
		return getCreationDate().compareTo(analysis.getCreationDate());
	}

	public Recording getRecording() {
		return recording;
	}
	
	@Override
	public Long getId() {
		return Id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((Id == null) ? 0 : Id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Analysis) {
			Analysis other = (Analysis) obj;
			return getCreationDate().equals(other.getCreationDate()) && getId().equals(other.getId());
		}
		return false;
	}

	@Override
	public String toString() {
		return "Analysis [client=" + client.getFirstname() + " " + client.getName() + ", creationDate=" + creationDate	+ ", id=" + Id + "]";	
	}

	/**
	 *  1. Vanaf het moment dat je de filename hebt, zou je ook een link moeten hebben naar een Movie object.
	 *  2. statuscode is eigenlijk ook een veld van Movie object..
	 *  3. alle calls gerelateerd naar toestand van het bestand zou je naar de Movie entity moeten sturen (delegeren)
	 *  
	 * @return De fileName van het terug te vinden filmpje
	 */
	private String getMovieFileName() {
		String date = ApplicationUtil.formatDate(getCreationDate(), ApplicationUtil.DATE_FORMAT);
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
