package com.runwalk.video.test;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Test;

import com.runwalk.video.util.ClientHttpRequest;


public class Poster extends TestCase {

	private static final String PHOTO_ID = "photoid";
	private static final String USER_ID = "userid";
	private static final String VISITOR_ID = "visitorguid";
	private static final String PHOTO_ID_VALUE = "fbe08e3a-a9d9-4b72-8f4b-e99df7214ff6";

	@Test
	public void testCategories() throws Exception {
	/*	System.out.println("correct");
	} else {
		System.out.println("incorrect, should be between "  + array[expectedInterval] + " and " + array[expectedInterval + 1]);
	}*/
		assertTrue(checkCategories(2, 0));
		assertTrue(checkCategories(6, 0));
		assertTrue(checkCategories(7, 1));
		assertTrue(checkCategories(8, 1));
		assertTrue(checkCategories(9, 2));
		assertTrue(checkCategories(10, 2));
		assertTrue(checkCategories(11, 2));
//		assertTrue(checkCategories(12, 2));
		
		assertFalse(checkCategories(13, 1));
		assertFalse(checkCategories(13, 2));
		assertFalse(checkCategories(13, 4));
		assertFalse(checkCategories(13, 0));
		assertFalse(checkCategories(10, 1));
		assertFalse(checkCategories(7, 0));
		assertFalse(checkCategories(9, 1));
	}
	
	private boolean checkCategories(int age, int category) {
		int[] array = new int[] {7, 9, 12};
		int foundInterval = -1;
		for (int i = 0; i < array.length && foundInterval == -1; i++) {
			if (age < array[0]) {
				foundInterval = 0;
			} else if (age >= array[i] && (i+1 < array.length && age < array[i+1])) {
				foundInterval = i + 1;
			}
		}
		boolean found = foundInterval == category;
		if (!found) {
			if (category == 0) {
				System.out.println("Must be younger than " + array[0]);
			} else if (category < array.length) {
				System.out.println("Must be between " +  array[category-1] + " and " + array[category]);
			}
		}
		
		return found;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new Poster().makePost();
	}

	private void makePost() throws IOException {
		URL url = new URL("http://www.standaard.be/ugc/photospecial/detail.aspx?id=fbe08e3a-a9d9-4b72-8f4b-e99df7214ff6");
		InputStream stream = ClientHttpRequest.post(url, Collections.emptyMap(), Collections.emptyMap());
		BufferedReader bufferedReader = createBufferedReader(stream);
		String line = null;
		//String patternStr = ".*?'[0-9|a-f]{5}.*";
		LinkedList<String> visitorGuids = new LinkedList<String>();
		String patternStr = "var " + VISITOR_ID + " = '.*?'";
		String callbackParam = null;
		while ((line = bufferedReader.readLine()) != null) {
			if (line.contains("visitor") || line.contains("visitorId")) {
				System.err.println(line);
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(line);
				while(matcher.find()) {
					String declaration = line.substring(matcher.start(), matcher.end());
					int index = declaration.indexOf("'");
					visitorGuids.add(declaration.substring(index+1, declaration.length() - 1));
				}
			} 
			if (line.contains("VoteForPhoto?callback")) {
				String callbackPattern = "VoteForPhoto\\?callback=.*?\\w"; 
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(line);
				while(matcher.find()) {
					String foundStr = line.substring(matcher.start(), matcher.end());
					int index = foundStr.indexOf("=");
					callbackParam = foundStr.substring(index+1);

				}
			} else {
				System.out.println(line);
			}
		}
		bufferedReader.close();
		//		reader.close();
		stream.close();

//		URL callbackUrl = new URL("http://api.standaard.be/UGC/PhotoSpecial/PhotoSpecialService.svc/VoteForPhoto?callback=jsonp1268511475965&");
		Map<String, String> parms = new LinkedHashMap<String, String>();
		Map<String, String> cookies = new LinkedHashMap<String, String>();
		parms.put("callback", "jsonp1277808009414");
//		parms.put("_", "1277808052932");
		parms.put(PHOTO_ID, PHOTO_ID_VALUE);
		parms.put(VISITOR_ID, visitorGuids.getLast());
		parms.put(USER_ID, "0");
		parms.put("papercode", "2");
		
		StringBuilder parmsStringBuilder = new StringBuilder();
		for (Entry<String, String> parm : parms.entrySet()) {
			parmsStringBuilder.append(parm.getKey()).append("=").append(parm.getValue()).append("&");
		}
		String parmsString = parmsStringBuilder.substring(0, parmsStringBuilder.length()-1);
		String result = sendGetRequest("http://api.standaard.be/UGC/PhotoSpecial/PhotoSpecialService.svc/VoteForPhoto", parmsString);
//		InputStream response = ClientHttpRequest.post(callbackUrl, new String[] {}, parms);
//		bufferedReader = createBufferedReader(response);
		/*	while ((line = bufferedReader.readLine()) != null) {
			System.out.println(line);
		}*/
	}

	public static String sendGetRequest(String endpoint, String requestParameters)
	{
		String result = null;
		if (endpoint.startsWith("http://"))
		{
			// Send a GET request to the servlet
			try
			{
				// Construct data
				StringBuffer data = new StringBuffer();

				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length () > 0)
				{
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection ();
				conn.setRequestProperty("Referer", 	"http://www.standaard.be/ugc/photospecial/detail.aspx?id=fbe08e3a-a9d9-4b72-8f4b-e99df7214ff6");
				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null)
				{
					sb.append(line);
				}
				rd.close();
				result = sb.toString();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		String str = "1268514989883";
		return result;
	}

	private BufferedReader createBufferedReader(InputStream input) {
		InputStreamReader reader = new InputStreamReader(input);
		return new BufferedReader(reader);
	}

}
