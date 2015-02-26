package com.runwalk.video.entities;

import java.util.Date;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@SuppressWarnings("serial")
@Table(name = "ospos_people")
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
	public static final String EMAIL = "email";
	
	@Column(name = NAME)
	private String name;
	@Column(name = FIRSTNAME)
	private String firstname;
	@Embedded
	private Address address = new Address();
	@Column(name = "email")
	private String emailAddress;
	@Column(name = "phone_number")
	private String phoneNumber;
	//@Column(name = "birthdate")
	//@Temporal(value = TemporalType.DATE)
	@Transient
	// TODO map to real column
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
		this.name = name;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
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
		return this.emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public int getVersion() {
		return version;
	}
	
	protected void setVersion(int version) {
		this.version = version;
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
		this.birthDate = birthdate;
	}
	
	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Address getAddress() {
		if (address == null) {
			this.address = new Address();
		}
		return address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirstname(), getName(), getId());
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj != null && getClass() == obj.getClass()) {
			Customer other = (Customer) obj;
			return Objects.equals(getFirstname(), other.getFirstname()) &&
				Objects.equals(getName(), other.getName()) &&
				Objects.equals(getBirthdate(), other.getBirthdate()) &&
				Objects.equals(getEmailAddress(), other.getEmailAddress()) &&
				Objects.equals(getAddress(), other.getAddress()) &&
				Objects.equals(getGender(), other.getGender()) &&
				Objects.equals(getId(), other.getId());
		}
		return result;
	}

	
	@Override
	public String toString() {
		return getFirstname() + " " + getName();
	}
	
	/**
	 * Enum for denoting the gender of the customer
	 * 
	 * WARNING: this field is mapped to the database by ordinal.
	 * Changing the order of declaration of the constants will change the parsed values in the application!
	 */
	public enum Gender {
		FEMALE, MALE;
	}
	
	public enum PersonType {
		CUSTOMER, EMPLOYEE, SUPPLIER;
	}

}
