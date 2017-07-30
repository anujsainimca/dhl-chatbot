package com.nlp.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExBasedExtraction {
	
	public void getIE(String value, String type)
	{
		System.out.println(type);
		System.out.println(value);
		TreeMap<String,Object> tm=new TreeMap<String,Object> ();
		String valuetokens[]=value.split("> <");
		String typetokens[]=type.split("> <");
		String entity_type="";
		String entity_value="";
		for (int i=0;i<typetokens.length;i++)
		{
			String valuesubtokens[]=valuetokens[i].split(" : ");
			String typesubtokens[]=typetokens[i].split(" : ");
			if (typesubtokens.length==2)
			{
				String entity_value_temp=valuesubtokens[1];
				String entity_type_temp=typesubtokens[1];
				if (!entity_type_temp.startsWith("+") && !entity_type.equals("") && !entity_type.equals(entity_type_temp))
				{
					System.out.println(entity_type+" :: "+entity_value);
				}
				if (entity_type_temp.startsWith("+"))
				{
					entity_value=entity_value+" "+entity_value_temp;
				}
				else
				{
					entity_type=entity_type_temp;
					entity_value=entity_value_temp;
				}
			}
			else
			{
				System.out.println(entity_type+" :: "+entity_value);
				entity_type="";
				entity_value="";
			}
		}
		System.out.println(entity_type+" :: "+entity_value);
		entity_type="";
		entity_value="";

	}
	public String cleanSentence(String sentence) {
		sentence = sentence.replace(", and ", " and ");
		int index = sentence.lastIndexOf(".");
		if (index != -1)
			sentence = sentence.substring(0, index) + " .";
		return sentence;
	}

	public static void main(String args[])
	{
		String input="How many Tasks open in X Project current sprint?";
		TreeMap<String,String> regexDict= new TreeMap<String,String> ();
		regexDict.put("<ADVP> <NP> <ADJP> <PP> <NP>", "<ADVP : Intent> <NP : +Intent> <ADJP> <PP> <NP : Entity>");
		RegExBasedExtraction rbe= new RegExBasedExtraction();
		
		PhraseDetector pd = new PhraseDetector();
		List<Object> triplesList = new ArrayList<Object>();
		String sentences[] = pd.sentenceDetector.sentDetect(input);
		for (String sentence : sentences) {
			int index = sentence.lastIndexOf(".");
			if (index != -1)
				sentence = sentence.substring(0, index) + " .";
			System.out.println(sentence);

			Map<String, Object> triples = new HashMap<String, Object>();
			System.out.println(sentence);
			triples.put("sentence", sentence);
			ArrayList<PhraseTemplate> phrases = pd
					.GetPhrases(rbe.cleanSentence(sentence));
			String questionExpr = "";
			String questionExprTest = "";
			for (int i = 0; i < phrases.size(); i++) {
				PhraseTemplate phraseObj = phrases.get(i);
				String phrase = phraseObj.phrase;
				String phraseType = phraseObj.phraseType;
				String phrasePos = phraseObj.pos;
//				System.out.println(phrase + " :: " + phraseType + " :: "+ phrasePos);
				questionExpr = questionExpr + " <" + phraseType + ">";
				questionExprTest = questionExprTest + " <" + phraseType + " : "
						+ phrase + ">";
			}
			questionExpr = questionExpr.trim();
			String salary="";
//			String questionExprTest = " < : > <ADVP : How> <NP : many Tasks> <ADJP : open> <PP : in> <NP : X Project current sprint>";
//			String regexp = "(<ADVP> <NP> <ADJP> <PP> <NP>";
//			String value = "<ADVP : Intent> <NP : +Intent> <ADJP> <PP> <NP : Entity>";
//			regexp = "(<ADVP .*> <NP .*> <ADJP .*> <PP .*> <NP .*>)";
			for (String regex:regexDict.keySet())
			{
				String value=regexDict.get(regex);
				regex=regex.replaceAll(">", " .*>");
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(questionExprTest);
			while (m.find()) {
				if (m.group().trim().length() > 1) {
					salary = m.group();
					//System.out.println(salary);
					rbe.getIE(salary, value);
				}
			}
		}

		}

	}

}
