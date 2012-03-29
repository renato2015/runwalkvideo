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
	
	private String feedUrl;
	
	public AuthenticationSettings(String userName, String password,
			String feedUrl) {
		this.userName = userName;
		this.password = password;
		this.feedUrl = feedUrl;
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
		return feedUrl;
	}

	public void setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
	}
	
}
