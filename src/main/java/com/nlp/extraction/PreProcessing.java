package com.nlp.extraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreProcessing {

	public void getIE(String value, String type) {
		System.out.println(type);
		System.out.println(value);
		TreeMap<String, Object> tm = new TreeMap<String, Object>();
		String valuetokens[] = value.split("> <");
		String typetokens[] = type.split("> <");
		String entity_type = "";
		String entity_value = "";
		for (int i = 0; i < typetokens.length; i++) {
			String valuesubtokens[] = valuetokens[i].split(" : ");
			String typesubtokens[] = typetokens[i].split(" : ");
			if (typesubtokens.length == 2) {
				String entity_value_temp = valuesubtokens[1];
				String entity_type_temp = typesubtokens[1];
				if (!entity_type_temp.startsWith("+")
						&& !entity_type.equals("")
						&& !entity_type.equals(entity_type_temp)) {
					System.out.println(entity_type + " :: " + entity_value);
				}
				if (entity_type_temp.startsWith("+")) {
					entity_value = entity_value + " " + entity_value_temp;
				} else {
					entity_type = entity_type_temp;
					entity_value = entity_value_temp;
				}
			} else {
				System.out.println(entity_type + " :: " + entity_value);
				entity_type = "";
				entity_value = "";
			}
		}
		System.out.println(entity_type + " :: " + entity_value);
		entity_type = "";
		entity_value = "";

	}

	public String cleanSentence(String sentence) {
		sentence = sentence.replace(", and ", " and ");
		int index = sentence.lastIndexOf(".");
		if (index != -1)
			sentence = sentence.substring(0, index) + " .";
		return sentence;
	}

	public static ArrayList<PhraseTemplate> preprocessPhrases(String question, PreProcessing pp, PhraseDetector  pd) {
		String input = question;
		String sentence=question;
		ArrayList<PhraseTemplate> processedPhrases = new ArrayList<PhraseTemplate>();
		{
			int index = sentence.lastIndexOf(".");
			if (index != -1)
				sentence = sentence.substring(0, index) + " .";
			Map<String, Object> triples = new HashMap<String, Object>();
			System.out.println(sentence);
			triples.put("sentence", sentence);
			ArrayList<PhraseTemplate> phrases = pd.GetPhrases(pp
					.cleanSentence(sentence));
			boolean hasVerb=false;
			for (PhraseTemplate phrase:phrases)
			{
				//System.out.println(phrase.phrase + " :: " + phrase.phraseType + " :: "+ phrase.pos);
				if (phrase.phraseType.equals("VP"))
				{
					hasVerb=true;
					break;
				}
			}
			if (!hasVerb)
			{
				sentence=sentence.substring(0, 1).toLowerCase() + sentence.substring(1);
				phrases = pd.GetPhrases(pp.cleanSentence(sentence));
			}
			String questionExpr = "";
			String questionExprTest = "";
			String processedPhrase = "";
			String processedPhraseType = "";
			String processedPhrasePos = "";
			for (int i = 0; i < phrases.size(); i++) {
				PhraseTemplate phraseObj = phrases.get(i);
				String phrase = phraseObj.phrase;
				String phraseType = phraseObj.phraseType;
				String phrasePos = phraseObj.pos;
				processedPhrase = phrase;
				processedPhraseType = phraseType;
				processedPhrasePos = phrasePos;
				System.out.println(phrase + " :: " + phraseType + " :: "
				 + phrasePos);
				if (phraseType.equals("ADVP") || (phraseType.equals("VP")&&(phrasePos.equals("VBZ")||phrasePos.equals("VBP")))) {
					processedPhrase = phraseObj.phrase;
					processedPhraseType = phraseObj.phraseType;
					processedPhrasePos = phraseObj.pos;
					for (int j = i + 1; j < phrases.size(); j++) {
						PhraseTemplate tempPhraseObj = phrases.get(j);
						if (tempPhraseObj.phraseType.equals("NP")) {
							i++;
							processedPhrase = processedPhrase + " "
									+ tempPhraseObj.phrase;
							processedPhrasePos = processedPhrasePos + " "
									+ tempPhraseObj.pos;
						} else
							break;
					}
				}
				if (phraseType.equals("NP")) {
					processedPhrase = phraseObj.phrase;
					processedPhraseType = phraseObj.phraseType;
					processedPhrasePos = phraseObj.pos;
					for (int j = i + 1; j < phrases.size(); j++) {
						PhraseTemplate tempPhraseObj = phrases.get(j);
						if (tempPhraseObj.phraseType.equals("NP")
								|| tempPhraseObj.pos.equals("IN")) {
							i++;
							processedPhrase = processedPhrase + " "
									+ tempPhraseObj.phrase;
							processedPhrasePos = processedPhrasePos + " "
									+ tempPhraseObj.pos;
						} else
							break;
					}
				}
				PhraseTemplate processedPhraseObj = new PhraseTemplate();
				processedPhraseObj.phrase = processedPhrase;
				processedPhraseObj.phraseType = processedPhraseType;
				processedPhraseObj.pos = processedPhrasePos;
				processedPhrases.add(processedPhraseObj);

			}
			for (int i = 0; i < processedPhrases.size(); i++) {
				PhraseTemplate phraseObj = processedPhrases.get(i);
				String phrase = phraseObj.phrase;
				String phraseType = phraseObj.phraseType;
				String phrasePos = phraseObj.pos;
				System.out.println(phrase + " :: " + phraseType + " :: "
						+ phrasePos);
			}
		}
		return processedPhrases;
	}

	public static void main(String args[]) {
		PreProcessing pp = new PreProcessing();
		PhraseDetector pd=new PhraseDetector ();
		String question = "What is the recommended dose for allegra";
		pp.preprocessPhrases(question,pp,pd);
	}

}
