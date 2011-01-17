package com.runwalk.video.entities;

import java.util.ArrayList;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

@SuppressWarnings("serial")
@Entity
@Table(schema="testdb", name="analysis")
public class Analysis extends SerializableEntity<Analysis> {

	public final static String RECORDING_COUNT = "recordingCount";
		
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne/*(cascade={CascadeType.MERGE, CascadeType.REFRESH})*/
	@JoinColumn(name="clientid", nullable=false )
	private Client client;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "analysis")
	@JoinFetch(JoinFetchType.OUTER)
	private List<Recording> recordings = new ArrayList<Recording>();
	
	@OneToOne
	@JoinColumn(name="articleid")
	private Article article;
	
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
	
	protected void setClient(Client client) {
		this.client = client;
	}

	public Article getArticle() {
		return this.article;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	protected void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.firePropertyChange("comments", this.comments, this.comments = comments);
	}

	public void setArticle(Article art) {
		this.firePropertyChange("article", this.article, this.article = art);
	}

	public List<Recording> getRecordings() {
		return recordings;
	}

	/**
	 * Add a recording to the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to add
	 */
	public boolean addRecording(Recording recording) {
		int oldSize = getRecordingCount();
		boolean result = getRecordings().add(recording);
		firePropertyChange(RECORDING_COUNT, oldSize, getRecordingCount());
		return result;
	}	
	
	/**
	 * Remove a {@link Recording} from the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to remove
	 * @return <code>true</code> if the recording was removed
	 */
	public boolean removeRecording(Recording recording) {
		int oldSize = getRecordingCount();
		boolean result = getRecordings().remove(recording);
		firePropertyChange(RECORDING_COUNT, oldSize, getRecordingCount());
		return result;
	}
	
	public int getRecordingCount() {
		return getRecordings().size();
	}
	
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((getCreationDate() == null) ? 0 : getCreationDate().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Analysis other = (Analysis) obj;
			result = getCreationDate() != null ? getCreationDate().equals(other.getCreationDate()) : other.getCreationDate() == null;
//			result &= getClient() != null ? getClient().equals(other.getClient()) : result;
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

	public boolean hasRecordings() {
		return getRecordings() != null && !getRecordings().isEmpty();
	}
	
	public boolean isRecorded() {
		for (Recording recording : getRecordings()) {
			if (recording.isRecorded()) {
				return true;
			}
		}
		return false;
	}

}
