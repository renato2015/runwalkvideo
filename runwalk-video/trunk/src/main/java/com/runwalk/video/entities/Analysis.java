package com.runwalk.video.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name="ospos_analysis")
public class Analysis extends SerializableEntity<Analysis> {

	public final static String COMMENTS = "comments";
	
	public final static String ITEM = "item";
	
	@Id
	@Column(name="id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne/*(cascade={CascadeType.MERGE, CascadeType.REFRESH})*/
	@JoinColumn(name="person_id", nullable=false )
	private Customer customer;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "analysis")
	@JoinFetch(JoinFetchType.OUTER)
	private List<Recording> recordings = new ArrayList<Recording>();
	
	@OneToOne
	@JoinColumn(name="item_id")
	private Item item;
	
	@Column(name="creation_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date creationDate;
	
	@Column(name="feedback_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date feedbackDate;
	
	@Column(name="start_date")
	@Temporal(value=TemporalType.TIMESTAMP)
	private Date startDate;

	@Lob
	private String comments;
	
	@Column(name="score")
	@Enumerated(EnumType.ORDINAL)
	private Progression progression;
	
	@Column(name="feedback_id")
	private Long feedbackId;
	
	@Column(name="feedback_token")
	private String tokenId;
	
	@Column(name="appointment_extref")
	private String appointmentExtref;
	
	@Column(name="appointment_cancelled")
	private boolean cancelled;
	
	protected Analysis() { }
	
	public Analysis(Customer customer, Date creationDate) {
		this.startDate = creationDate;
		this.creationDate = creationDate;
		this.customer = customer;
	}
	
	public Analysis(Customer customer, Analysis analysis, Date creationDate, Date startDate) {
		this.startDate = startDate;
		this.creationDate = creationDate;
		this.customer = customer;
		this.feedbackId = analysis.getId();
	}

	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Item getItem() {
		return item;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public Date getFeedbackDate() {
		return feedbackDate;
	}

	public void setFeedbackDate(Date feedbackDate) {
		this.feedbackDate = feedbackDate;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public void setItem(Item item) {
		this.item = item;
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
		return getRecordings().add(recording);
	}	
	
	/**
	 * Remove a {@link Recording} from the association. Fires a 'synthetic' PCE to notify listeners about this change.
	 * 
	 * @param recording The recording to remove
	 * @return <code>true</code> if the recording was removed
	 */
	public boolean removeRecording(Recording recording) {
		return getRecordings().remove(recording);
	}
	
	public Long getId() {
		return id;
	}

	protected void setId(Long id) {
		this.id = id;
	}
	
	public Progression getProgression() {
		return progression;
	}

	public void setProgression(Progression progression) {
		this.progression = progression;
	}

	public Long getFeedbackId() {
		return feedbackId;
	}
	
	public String getTokenId() {
		return tokenId;
	}
	
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public String getAppointmentExtref() {
		return appointmentExtref;
	}

	public void setAppointmentExtref(String appointmentExtref) {
		this.appointmentExtref = appointmentExtref;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result	+ ((getId() == null) ? 0 : getCreationDate().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Analysis other = (Analysis) obj;
			result = getId() != null ? getId().equals(other.getId()) : other.getId() == null;
//			result &= getCustomer() != null ? getCustomer().equals(other.getCustomer()) : result;
		}
		return result;
	}

	@Override
	public String toString() {
		return "Analysis [customer=" + customer.getFirstname() + " " + customer.getName() + ", creationDate=" + creationDate	+ ", id=" + id + "]";	
	}

	public enum Progression {
		NOK, CAVA, OK
	}

}
