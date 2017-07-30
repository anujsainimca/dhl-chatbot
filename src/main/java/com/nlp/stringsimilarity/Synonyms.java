package com.nlp.stringsimilarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The similarity between the two strings using weight of words. Weights would
 * be determined using IDF scores of strings
 * 
 */
public class Synonyms {

	Map<String, String> synonymsMap = new TreeMap<String, String>();
	PreProcess pp = new PreProcess();

	public Synonyms() {
		BufferedReader br = null;

		try {

			String sCurrentLine;
			br = new BufferedReader(new FileReader("resources/synonyms.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains(":")) {
					String tokens[] = sCurrentLine.split(":");
					String synonyms = tokens[1];
					String synonyms_tokens[] = synonyms.split(",");
					for (String synonym : synonyms_tokens) {
						if (synonym.toLowerCase().trim().length() > 1)
							synonymsMap.put(synonym.toLowerCase().trim(),
									tokens[0].toLowerCase().trim());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean containsSynonym(String s1) {
		if (synonymsMap.containsKey(s1.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * This method get synonym after cleaning the input word. It removes stop
	 * words from the query word
	 * 
	 * @param s1
	 *            input word whose synonym needs to be found
	 * @return synonym
	 */
	public String getProcessedSynonym(String s1) {
		s1=pp.getCleanedText(s1);
		if (synonymsMap.containsKey(s1.toLowerCase())) {
			return synonymsMap.get(s1.toLowerCase());
		}
		return s1;
	}

	public String getSynonym(String s1) {
		if (synonymsMap.containsKey(s1.toLowerCase()))
			return synonymsMap.get(s1.toLowerCase());
		return s1;
	}

	public static void main(String args[]) {
		Synonyms syn = new Synonyms();
		System.out.println(syn.getProcessedSynonym("is the position"));
	}

}