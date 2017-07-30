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
public class SoftTfIdf {

	Map<String, Double> idfDictionary = new TreeMap<String, Double>();
	Double defaultScore=0.0;
	
	public SoftTfIdf() {
		BufferedReader br = null;

		try {

			String sCurrentLine;
			Double totalScore=0.0;
			br = new BufferedReader(new FileReader(
					"resources/tblCompanyNames_dedup_idf.tsv"));
			while ((sCurrentLine = br.readLine()) != null) {
				String tokens[] = sCurrentLine.split("\t");
				Double idfScore=Double.parseDouble(tokens[1]);
				totalScore=idfScore+totalScore;
				idfDictionary.put(tokens[0], idfScore);
			}
			
			defaultScore=totalScore/idfDictionary.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double distance(String s1, String s2) {
		Double commonScore=0.0;
		Double totalScore=0.0;
		List<String> common=common(s1, s2);
		for (String element:unique(s1, s2))
		{
			Double idfScore=idfDictionary.containsKey(element) ? idfDictionary.get(element) : defaultScore;
			totalScore=totalScore+idfScore;
			if (common.contains(element))
				commonScore=commonScore+idfScore;
		}
		return commonScore/totalScore;
	}

    private List<String> common(String s1, String s2)
    {
		String s1Array[]=s1.split("[ ,\\.]");
		String s2Array[]=s2.split("[ ,\\.]");
		List<String> s1List = Arrays.asList(s1Array);
		List<String> s2List = Arrays.asList(s2Array);
		List<String> l3 = new ArrayList<String>(s1List);
		l3.retainAll(s2List);
        return l3;
    }

    private Set<String> unique(String s1, String s2)
    {
		String s1Array[]=s1.split("[ ,\\.]");
		String s2Array[]=s2.split("[ ,\\.]");
		List<String> s1List = Arrays.asList(s1Array);
		List<String> s2List = Arrays.asList(s2Array);
		Set<String> uniqueEntries = new HashSet<String>();
		uniqueEntries.addAll(s2List);
		uniqueEntries.addAll(s1List);
        return uniqueEntries;
    }

    public static void main(String args[]) {
    	SoftTfIdf idfDistance= new SoftTfIdf();
		String s1 = "is the generic name";//"cleveland�cavaliers";
		String s2 = "generic name";//"cleveland cavaliers";
		Double matchingScore = idfDistance.distance(s1.toLowerCase(), s2.toLowerCase());
		System.out.println(matchingScore);
	}

}