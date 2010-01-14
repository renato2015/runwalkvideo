package com.runwalk.video.entities;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableCollections.ObservableListHelper;

@Entity
@SuppressWarnings("serial")
@Table(schema = "testdb", name = "clients")
@NamedQuery(name="findAllClients", query="SELECT DISTINCT c FROM Client c LEFT JOIN FETCH c.unobservableAnalyses")
public class Client extends SerializableEntity<Client> {
	public static final String LAST_ANALYSIS_DATE = "lastAnalysisDate";
	public static final String ORGANIZATION = "organization";
	public static final String FIRSTNAME = "firstname";
	public static final String ADDRESS = "address";
	public static final String NAME = "name";
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = NAME)
	private String name;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "client")
	private List<Analysis> unobservableAnalyses;
	@Column(name = "address1")
	private String address;
	@ManyToOne
	@JoinColumn(name = "cityid")
	private City city;
	@Column(name = "postalCode")
	private String postalcode;
	@Column(name = "website")
	private String emailAdress;
	@Column(name = "btwnr")
	private Integer taxNumber;
	@Column(name = FIRSTNAME)
	private String firstname;
	@Column(name = "mail")
	private int mail;
	@Column(name = ORGANIZATION)
	private String organization;
	@Column(name = "phone")
	private String phoneNumber;
	@Column(name = "birthdate")
	@Temporal(value = TemporalType.DATE)
	private Date birthdate;
	@Column(name = "male")
	private Integer male;
	@Transient
	private Date lastAnalysisDate;
	@Transient
	private ObservableList<Analysis> analyses;

	public Client() {
		super();
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		firePropertyChange(NAME, this.name, this.name = name);
	}

	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		firePropertyChange(ADDRESS, this.address, this.address = address);
	}

	public boolean isMale() {
		return male != null && male == 1 ? true : false;
	}

	public void setMale(boolean male) {
		this.male = male ? 1 : 0;
	}

	public City getCity() {
		return this.city;
	}

	public void setCity(City city) {
		firePropertyChange("city", this.city, this.city = city);
	}

	public String getPostalcode() {
		return (postalcode == null && city != null) ? "" + city.getCode() : postalcode;
	}

	public String getEmailAddress() {
		return this.emailAdress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAdress = emailAddress;
	}

	public Integer getTaxNumber() {
		return this.taxNumber;
	}

	public void setTaxNumber(Integer taxNumber) {
		this.taxNumber = taxNumber;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		firePropertyChange(FIRSTNAME, this.firstname, this.firstname = firstname);
	}

	public int getMail() {
		return this.mail;
	}

	public void setMail(byte mail) {
		this.mail = mail;
	}

	@PostLoad
	private void initObservableList() {
		if (unobservableAnalyses == null) {
			unobservableAnalyses = new ArrayList<Analysis>();
		}
		if (analyses == null) {
			analyses = ObservableCollections.observableList(unobservableAnalyses);
		}
	}
	
	public int getAnalysisCount() {
		return getAnalyses().size();
	}
	
	public ObservableList<Analysis> getAnalyses() {
		if (analyses == null) {
			initObservableList();
		}
		return analyses;
	}

	public void removeAnalysis(Analysis analysis) {
		if (analysis != null && getAnalyses().contains(analysis)) {
			getAnalyses().remove(analysis);
			setLastAnalysisDate(getAnalyses().isEmpty() ? null : getAnalyses().get(getAnalysisCount() - 1).getCreationDate());
		}
	}

	public int addAnalysis(Analysis analysis) {
		getAnalyses().add(analysis);
		int index = getAnalyses().indexOf(analysis);
		setLastAnalysisDate(analysis.getCreationDate());
		return index;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		firePropertyChange(ORGANIZATION, this.organization, this.organization = organization);
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public Date getLastAnalysisDate() {
		if (lastAnalysisDate == null && !getAnalyses().isEmpty()) {
			Collections.sort(getAnalyses());
			lastAnalysisDate = getAnalyses().get(getAnalysisCount()-1).getCreationDate();
		}
		return lastAnalysisDate;
	}
	
	private void setLastAnalysisDate(Date lastAnalysisDate) {
		firePropertyChange(LAST_ANALYSIS_DATE, this.lastAnalysisDate, this.lastAnalysisDate = lastAnalysisDate);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getFirstname() == null) ? 0 : getFirstname().hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Client other = (Client) obj;
			result = getFirstname() != null ? getFirstname().equals(other.getFirstname()) : other.getFirstname() == null;
			result &= getName() != null ? getName().equals(other.getName()) : other.getName() == null;
			result &= getId() != null ? getId().equals(other.getId()) : result;
		}
		return result;
	}

	@Override
	public String toString() {
		return getFirstname() + " " + getName();
	}

	public int compareTo(Client o) {
		return this.equals(o) ? 0 : getId().compareTo(o.getId());
	}

}
