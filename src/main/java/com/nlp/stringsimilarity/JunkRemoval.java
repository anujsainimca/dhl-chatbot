package com.nlp.stringsimilarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The similarity between the two strings using weight of words. Weights would
 * be determined using IDF scores of strings
 * 
 */
public class JunkRemoval {

	Map<String, String> junkWordsMap = new HashMap<String, String>();

	public JunkRemoval() {
		BufferedReader br = null;

		try {

			String sCurrentLine;
			br = new BufferedReader(new FileReader("resources/junkwords.txt"));
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.trim().length()>0) {
					junkWordsMap.put(sCurrentLine.toLowerCase().trim(), "");
					}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean IsJunkWord(String s1) {
		if (junkWordsMap.containsKey(s1.toLowerCase()))
			return true;
		return false;
	}


	public static void main(String args[]) {
		JunkRemoval syn = new JunkRemoval();
		System.out.println(syn.IsJunkWord("please tafell"));
	}

}