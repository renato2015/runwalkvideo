package com.runwalk.video.entities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name="analysis")
public class Analysis extends SerializableEntity<Analysis> implements PropertyChangeListener {

	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne/*(cascade={CascadeType.MERGE, CascadeType.REFRESH})*/
	@JoinColumn(name="clientid", nullable=false)
	private Client client;
	
	@OneToOne
	@JoinColumn(name="articleid")
	private Article article;
	
	@OneToOne(cascade=CascadeType.ALL)
	@JoinColumn(name="movieid", unique = true)
	private Recording recording;
	
	@Column(name="date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date creationDate;

	@Lob
	private String comments;
	
	protected Analysis() { }
	
	public Analysis(Client client) {
		creationDate = new Date();
		this.client = client;
	}
	
	public Client getClient() {
		return client;
	}
	
	public Article getArticle() {
		return this.article;
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

	public void setArticle(Article art) {
		this.article = art;
	}

	public void setRecording(Recording recording) {
		if (this.recording != null) {
			this.recording.removePropertyChangeListener(this);
		}
		this.recording = recording;
		this.recording.addPropertyChangeListener(this);
	}

	public Recording getRecording() {
		return recording;
	}
	
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
		return this.equals(analysis) ? 0 : getCreationDate().compareTo(analysis.getCreationDate());
	}

	@Override
	public String toString() {
		return "Analysis [client=" + client.getFirstname() + " " + client.getName() + ", creationDate=" + creationDate	+ ", id=" + id + "]";	
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
	
	/**
	 * This entity implements a {@link PropertyChangeListener} to cascade {@link PropertyChangeEvent} firing from its recording.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
	}

}
