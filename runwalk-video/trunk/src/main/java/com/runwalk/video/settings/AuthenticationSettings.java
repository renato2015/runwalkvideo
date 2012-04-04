package com.runwalk.video.settings;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class manages three common properties that are commonly used in authentication against
 * backends.
 *
 * @author Jeroen Peelaerts
 */
@XmlRootElement
public class AuthenticationSettings {

	private String userName;
	
	private String password;
	
	private String url;
	
	public AuthenticationSettings() { }
	
	public AuthenticationSettings(String userName, String password,
			String url) {
		this.userName = userName;
		this.password = password;
		this.url = url;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
