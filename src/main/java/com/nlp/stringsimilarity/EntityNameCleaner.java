package com.nlp.stringsimilarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

/**
 * This class clean entity names by stripping legal names from the entity name
 * 
 */
public class EntityNameCleaner {

	List<String> legalNamesList = new ArrayList<String>();

	public EntityNameCleaner() {
		BufferedReader br = null;

		try {

			String sCurrentLine;
			br = new BufferedReader(new FileReader("resources\\legal-names.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				legalNamesList.add(sCurrentLine.toLowerCase().trim());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String cleanEntity(String s1) {
		for (String legalName:legalNamesList)
		{
			s1=s1.toLowerCase().replaceAll(" "+legalName.toLowerCase(), "").trim();
		}
		s1=WordUtils.capitalize(s1);
		if (s1.endsWith("."))
			s1 = s1.substring(0,s1.length()-1);
		s1=s1.replaceAll(".com", "").trim();
		return s1;
	}

	public static void main(String args[]) {
		EntityNameCleaner syn = new EntityNameCleaner();
		System.out.println(syn.cleanEntity("American Express ltd."));
	}

}