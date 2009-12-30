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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(schema = "testdb", name = "clients")
//@SecondaryTable(name = "all_analysis_dates")
@NamedQuery(name="findAllClients", query="SELECT distinct c from Client c LEFT JOIN FETCH c.analyses")
/*@NamedNativeQuery(
		name="findAllClientInfo",
		query="SELECT MAX(analysis.date) AS last_analysis, analysis.clientid FROM analysis, clients WHERE clients.id = analysis.clientid GROUP BY clientid",
		resultSetMapping="ClientMapping"
)
@NamedQuery(name = "findAllClients", query = "SELECT cl FROM Client cl")

@SqlResultSetMapping(name="ClientMapping", 
		entities={@EntityResult(entityClass=Client.class)},
		columns={@ColumnResult(name="TEMP.last_analysis")}
)*/
//SELECT clients.*, TEMP.last_analysis FROM clients LEFT OUTER JOIN (SELECT MAX(analysis.date) AS last_analysis, analysis.clientid FROM analysis, clients WHERE clients.id = analysis.clientid GROUP BY clientid) AS TEMP ON TEMP.clientid = clients.id
//CREATE OR REPLACE VIEW `testdb`.`all_analysis_dates` AS SELECT clients.id, analysis_dates.last_analysis_date FROM clients LEFT OUTER JOIN analysis_dates ON analysis_dates.clientid = clients.id 
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
	private List<Analysis> analyses;
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
/*	@Column(name="last_analysis_date", table="all_analysis_dates", insertable=false, updatable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastAnalysisDate;*/
	@Transient
	private Date lastAnalysisDate;
//	@OneToOne(cascade={})
//	@JoinColumn(name="id",referencedColumnName="id",insertable=false, updatable=false)
//	@JoinColumn(name = "id",referencedColumnName="clientid", updatable=false, insertable=false)  
//	private AnalysisInfo analysisInfo;

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

	public List<Analysis> getAnalyses() {
		List<Analysis> emptyList = Collections.emptyList();
		return (analyses == null) ? emptyList : Collections.unmodifiableList(analyses);
	}

	public void removeAnalysis(Analysis analysis) {
		if (analysis != null && analyses != null && !analyses.isEmpty()) {
			analyses.remove(analysis);
			setLastAnalysisDate(analyses.isEmpty() ? null : analyses.get(analyses.size() - 1).getCreationDate());
		}
	}

	public void addAnalysis(Analysis analysis) {
		if (analyses == null) {
			analyses = new ArrayList<Analysis>();
		}
		analyses.add(analysis);
		setLastAnalysisDate(analysis.getCreationDate());
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
		/*if (lastAnalysisDate == null && analysisInfo != null) {
			setLastAnalysisDate(analysisInfo.getLastAnalysisDate());
		}
		return lastAnalysisDate;*/
		if (lastAnalysisDate == null && analyses != null && !analyses.isEmpty()) {
			Collections.sort(analyses);
			lastAnalysisDate = analyses.get(analyses.size()-1).getCreationDate();
		}
		return lastAnalysisDate;
	}
	
	private void setLastAnalysisDate(Date lastAnalysisDate) {
		/*if (analysisInfo != null) {
			analysisInfo.setLastAnalysisDate(lastAnalysisDate);
		}*/
		firePropertyChange(LAST_ANALYSIS_DATE, this.lastAnalysisDate, this.lastAnalysisDate = lastAnalysisDate);
	}

	@Override
	public int hashCode() {
		//TODO this implementation isn error prone.. hashcode will not be constant during object's lifecycle
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (getClass() == obj.getClass()) {
			Client other = (Client) obj;
			if (getId() == null && other.getId() == null) {
				result = this == other;
			} else {
				result = getId().equals(other.getId());
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return getFirstname() + " " + getName();
	}

	public int compareTo(Client o) {
		int result;
		if (this.equals(o)) {
			result = 0;
		} else {
			result = getId().compareTo(o.getId());
		}
		return result;
	}

}
