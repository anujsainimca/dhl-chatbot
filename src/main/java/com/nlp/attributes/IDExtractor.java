package com.nlp.attributes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author asai13
 * This class extracts order id from the user question
 */
public class IDExtractor {

	public String getID(String question) {
		String regexp = "([0-9]{10})";
		Pattern p = Pattern.compile(regexp);
		String id="";
		Matcher m = p.matcher(question);
		while (m.find()) {
			if (m.group().trim().length() > 1) {
				id=(m.group());
			}
		}
		id=id.trim();
		return id;
	}
	public static void main(String args[]) {
		IDExtractor pe=new IDExtractor();
		String question = "what is the status of my parcel my order id is 3873173242 " ;
		System.out.println(pe.getID(question));
	
	}
}
