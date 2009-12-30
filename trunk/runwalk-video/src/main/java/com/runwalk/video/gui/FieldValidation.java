package com.runwalk.video.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidation {

	public static boolean isEmailValid(String email){
		boolean isValid = false;
		/*
		Email format: A valid email address will have following format:

		 * [\\w\\.-]+ : Begins with word characters, (may include periods and hypens).
		 * @ : It must have a Ô@Õ symbol after initial characters.
		 * ([\\w\\-]+\\.)+ : Ô@Õ must follow by more alphanumeric characters (may include hypens.).
		      This part must also have a Ò.Ó to separate domain and subdomain names.
		 * [A-Z]{2,4}$ : Must end with two to four alaphabets.
		      (This will allow domain names with 2, 3 and 4 characters e.g pa, com, net, wxyz)

		Examples: Following email addresses will pass validation
		abc@xyz.net; ab.c@tx.gov
		 */
		//Initialize reg ex for email.
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;

		//Make the comparison case-insensitive.

		Pattern pattern = Pattern.compile(expression,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if(matcher.matches()){
			isValid = true;
		}
		return isValid;
	}

	public static boolean isNumeric(String number){
		boolean isValid = false;
		/*Number: A numeric value will have following format:

		 * ^[-+]? : Starts with an optional Ò+Ó or Ò-Ó sign.
		 * [0-9]* : May have one or more digits.
		 * \\.? : May contain an optional Ò.Ó (decimal point) character.
		 * [0-9]+$ : ends with numeric digit.

		 */
		//Initialize reg ex for numeric data.
		String expression = "[0-9]*";
		CharSequence inputStr = number;
		Pattern pattern = Pattern.compile(expression);
		Matcher matcher = pattern.matcher(inputStr);
		if(matcher.matches()){
			isValid = true;
		}
		return isValid;
	}
}
