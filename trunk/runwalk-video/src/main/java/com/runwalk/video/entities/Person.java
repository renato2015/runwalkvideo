package com.runwalk.video.entities;

import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@SuppressWarnings("serial")
@Table(schema = "testdb", name = "phppos_people")
@DiscriminatorColumn(name=Person.TYPE)
@AttributeOverride(name=Person.ID, column=@Column(name=Person.PERSON_ID))
public abstract class Person extends SerializableEntity<Person> {
	
	public static final String FIRSTNAME = "first_name";
	public static final String ADDRESS = "address";
	public static final String NAME = "last_name";
	public static final String BIRTH_DATE = "birthDate";
	public static final String GENDER = "gender";
	public static final String PERSON_ID = "person_id";
	public static final String TYPE = "type";
	
	public static enum PersonType {
		CUSTOMER, EMPLOYEE, SUPPLIER;
	}
	
	@Column(name = NAME)
	private String name;
	@Column(name = FIRSTNAME)
	private String firstname;
	@Embedded
	private Address address = new Address();
	@Column(name = "email")
	private String emailAdress;
	@Column(name = "phone_number")
	private String phoneNumber;
	@Column(name = "birthdate")
	@Temporal(value = TemporalType.DATE)
	private Date birthDate;
	@Column(name = "gender")
	@Enumerated(EnumType.ORDINAL)
	private Gender gender;
	@Column(name = "in_mailing_list")
	private boolean inMailingList = true;
	@Version
	private int version;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		firePropertyChange(NAME, this.name, this.name = name);
	}

	public void setGender(Gender gender) {
		firePropertyChange(GENDER, this.gender, this.gender = gender);
	}
	
	public Gender getGender() {
		return gender;
	}
	
	public boolean isInMailingList() {
		return inMailingList;
	}

	public void setInMailingList(boolean inMailingList) {
		this.inMailingList = inMailingList;
	}

	public String getEmailAddress() {
		return this.emailAdress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAdress = emailAddress;
	}
	
	public int getVersion() {
		return version;
	}
	
	public void incrementVersion() {
		version++;
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
	
	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		firePropertyChange(FIRSTNAME, this.firstname, this.firstname = firstname);
	}

	public Address getAddress() {
		return address;
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

	public int compareTo(Person o) {
		return this.equals(o) ? 0 : getId() != null ? getId().compareTo(o.getId()) : 1;
	}
	
	/**
	 * Enum for denoting the gender of the client
	 * 
	 * WARNING: this field is mapped to the database by ordinal.
	 * Changing the order of declaration of the constants will change the parsed values in the application!
	 */
	public enum Gender {
		FEMALE, MALE;
	}

}
