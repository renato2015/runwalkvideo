package com.runwalk.video.test.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jdesktop.application.AbstractBean;

import com.runwalk.video.test.entities.Analysis;
import com.runwalk.video.test.entities.Client;

public class Client extends AbstractBean implements Comparable<Client> {
	
		public static final String LAST_ANALYSIS_DATE = "lastAnalysisDate";
		public static final String ORGANIZATION = "organization";
		public static final String FIRSTNAME = "firstname";
		public static final String ADDRESS = "address";
		public static final String NAME = "name";
		public static final String BIRTH_DATE = "birthDate";
		public static final String MALE = "male";
		
		private String firstname;
		private String name;
		private Long id;
		private List<Analysis> analyses = new ArrayList<Analysis>();
		private String emailAdress;
		private Integer taxNumber;
		private String organization;
		private String phoneNumber;
		private Date birthDate;
		private Integer male;
		private Date lastAnalysisDate;

		public Client(String name, String firstName) {
			this.name = name;
			this.firstname = firstName;
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

		public boolean isMale() {
			return male != null && male == 1 ? true : false;
		}

		public void setMale(boolean male) {
			firePropertyChange(MALE, this.male, this.male = male ? 1 : 0);
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

		public int getAnalysesCount() {
			return getAnalyses().size();
		}
		
		public List<Analysis> getAnalyses() {
			return analyses;
		}

		public boolean removeAnalysis(Analysis analysis) {
			boolean result = false;
			if (analysis != null) {
				result = getAnalyses().remove(analysis);
				Date lastAnalysisDate = null;
				if (!getAnalyses().isEmpty()) {
					Analysis lastAnalysis = getAnalyses().get(getAnalysesCount() - 1);
					lastAnalysisDate = lastAnalysis.getCreationDate();
				}
				setLastAnalysisDate(lastAnalysisDate);
			}
			return result;
		}

		public boolean addAnalysis(Analysis analysis) {
			boolean result = getAnalyses().add(analysis);
			setLastAnalysisDate(analysis.getCreationDate());
			return result;
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
			return birthDate;
		}

		public void setBirthdate(Date birthdate) {
			firePropertyChange(BIRTH_DATE, this.birthDate, this.birthDate = birthdate);
		}

		public Date getLastAnalysisDate() {
			if (lastAnalysisDate == null && !getAnalyses().isEmpty()) {
				Collections.sort(getAnalyses());
				lastAnalysisDate = getAnalyses().get(getAnalysesCount()-1).getCreationDate();
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
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if (obj != null && getClass() == obj.getClass()) {
				Client other = (Client) obj;
				result = getFirstname() != null ? getFirstname().equals(other.getFirstname()) : other.getFirstname() == null;
				result &= getName() != null ? getName().equals(other.getName()) : other.getName() == null;
			}
			return result;
		}

		@Override
		public String toString() {
			return getFirstname() + " " + getName();
		}

		public int compareTo(Client o) {
			return this.equals(o) ? 0 : getId() != null ? getId().compareTo(o.getId()) : 1;
		}
}
