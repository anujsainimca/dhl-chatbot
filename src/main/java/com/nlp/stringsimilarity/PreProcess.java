package com.nlp.stringsimilarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The similarity between the two strings using weight of words. Weights would
 * be determined using IDF scores of strings
 * 
 */
public class PreProcess {

	Map<String, String> stopWordsMap= new HashMap<String, String>();

	public PreProcess() {
		BufferedReader br = null;

		try {

			String sCurrentLine;
			br = new BufferedReader(new FileReader("resources/stopwords.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.trim().length() > 0) {
					stopWordsMap.put(sCurrentLine.toLowerCase().trim(), "");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getCleanedText(String s) {
		String[] words = s.split(" ");
		ArrayList<String> wordsList = new ArrayList<String>();

		for (String word : words) {
			String wordCompare = word.toLowerCase().trim();
			if (!stopWordsMap.containsKey(wordCompare)) {
				wordsList.add(word);
			}
		}
		String cleanedString = "";
		for (String str : wordsList) {
			cleanedString = cleanedString + " " + str;
		}

		return cleanedString.trim();
	}

	public static void main(String args[]) {
		PreProcess pp = new PreProcess();
		System.out.println(pp.getCleanedText("is the brand name"));
	}

}