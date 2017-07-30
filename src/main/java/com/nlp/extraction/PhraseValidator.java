package com.nlp.extraction;

public class PhraseValidator
{
	public String getCorrectPhrase(String phrase)
	{
		if (phrase.startsWith("the ")||phrase.startsWith("his ")||phrase.startsWith("her ")||phrase.startsWith("she ")||phrase.startsWith("this ")
				||phrase.startsWith("he ")||phrase.startsWith("there ")||phrase.startsWith("i ")||phrase.startsWith("it "))
		{
			if(phrase.split(" ").length==2)
				return phrase.substring(phrase.indexOf(" "),phrase.length()).trim();
		}
		
		if(phrase.startsWith("'")||phrase.startsWith(") ")||phrase.startsWith("&amp; "))
			return phrase.substring(phrase.indexOf(" "),phrase.length()).trim();
		
		return phrase;
	}
}
