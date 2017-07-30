package com.nlp.extraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ForTest {

	public static void main(String args[])
	{
		String s="I love this phone, its i super fast and there's so" +
	            " much new and cool things with jelly bean....but of recently I've seen some bugs.";
	    String[] words = s.split(" ");
	    ArrayList<String> wordsList = new ArrayList<String>();
	    Set<String> stopWordsSet = new HashSet<String>();
	    stopWordsSet.add("I");
	    stopWordsSet.add("THIS");
	    stopWordsSet.add("AND");
	    stopWordsSet.add("THERE'S");

	    for(String word : words)
	    {
	        String wordCompare = word.toUpperCase();
	        if(!stopWordsSet.contains(wordCompare))
	        {
	            wordsList.add(word);
	        }
	    }

	    for (String str : wordsList){
	        System.out.print(str+" ");
	    }
	}
}
