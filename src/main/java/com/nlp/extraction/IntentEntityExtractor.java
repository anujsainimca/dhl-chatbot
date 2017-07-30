package com.nlp.extraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nlp.entityresolution.LuceneSearcher;
import com.nlp.stringsimilarity.JunkRemoval;
import com.nlp.stringsimilarity.Synonyms;

public class IntentEntityExtractor {
	public AttributesExtractor ae;
	public NegationExtractor ne;
	public LuceneSearcher ls;
	public PreProcessing pp;
	public PhraseDetector pd;
	public FallBackListBased ee;
	public IntentEntityMatching im;
	public JunkRemoval jr;

	public IntentEntityExtractor() {
		ae = new AttributesExtractor();
		ne = new NegationExtractor();
		ls = new LuceneSearcher();
		pp = new PreProcessing();
		pd = new PhraseDetector();
		ee = new FallBackListBased();
		im = new IntentEntityMatching();
		jr = new JunkRemoval();
	}

	public Map<String, Object> RegExExtraction(String value, String type) {
		System.out.println(type);
		System.out.println(value);
		TreeMap<String, Object> tm = new TreeMap<String, Object>();
		String valuetokens[] = value.split("> <");
		String typetokens[] = type.split("> <");
		String entity_type = "";
		String entity_value = "";
		Map<String, Object> ieMap = new TreeMap<String, Object>();
		for (int i = 0; i < typetokens.length; i++) {
			String valuesubtokens[] = valuetokens[i].split(" : ");
			String typesubtokens[] = typetokens[i].split(" : ");
			if (typesubtokens.length == 2) {
				String entity_value_temp = valuesubtokens[1];
				String entity_type_temp = typesubtokens[1];
				if (!entity_type_temp.startsWith("+")
						&& !entity_type.equals("")
						&& !entity_type.equals(entity_type_temp)) {
					if (entity_value.endsWith(">"))
						entity_value = entity_value.substring(0,
								entity_value.length() - 1);
					if (entity_type.endsWith(">"))
						entity_type = entity_type.substring(0,
								entity_type.length() - 1);
					ieMap.put(entity_type, entity_value);
				}
				if (entity_type_temp.startsWith("+")) {
					entity_value = entity_value + " " + entity_value_temp;
				} else {
					entity_type = entity_type_temp;
					entity_value = entity_value_temp;
				}
			} else {
				if (entity_value.endsWith(">"))
					entity_value = entity_value.substring(0,
							entity_value.length() - 1);
				if (entity_type.endsWith(">"))
					entity_type = entity_type.substring(0,
							entity_type.length() - 1);
				ieMap.put(entity_type, entity_value);
				entity_type = "";
				entity_value = "";
			}
		}
		if (entity_value.endsWith(">"))
			entity_value = entity_value.substring(0, entity_value.length() - 1);
		if (entity_type.endsWith(">"))
			entity_type = entity_type.substring(0, entity_type.length() - 1);
		ieMap.put(entity_type, entity_value);
		entity_type = "";
		entity_value = "";
		return ieMap;
	}

	public String cleanSentence(String sentence) {
		sentence = sentence.replace(", and ", " and ");
		int index = sentence.lastIndexOf(".");
		if (index != -1)
			sentence = sentence.substring(0, index) + " .";
		return sentence;
	}

	public List<Map<String, Object>> getIntentEntity(String question,
			IntentEntityExtractor iee) {
		List<Map<String, Object>> intentEntities = new ArrayList<Map<String, Object>>();
		if (!(question.endsWith("?") || question.endsWith(".")))
			question = question + "?";
		TreeMap<String, String> regexDict = new TreeMap<String, String>();
		regexDict.put("<ADVP> <ADJP> <PP> <NP>",
				"<ADVP : Intent> <ADJP> <PP> <NP : Entity>");
		regexDict.put("<VP> <PP> <NP>", "<VP : Intent> <PP> <NP : Entity>");
		regexDict.put("<VP> <NP>", "<VP : Intent> <NP : Entity>");
		regexDict.put("<ADVP> <VP> <VP>", "<ADVP> <VP : Entity> <VP : Intent>");
		regexDict.put("<ADVP> <O> <NP> <VP>", "<ADVP> <O> <NP : Entity> <VP : Intent>");

		// String sentences[] = pd.sentenceDetector.sentDetect(input);
		// for (String sentence : sentences)
		{
			int index = question.lastIndexOf(".");
			if (index != -1)
				question = question.substring(0, index) + " .";

			Map<String, Object> triples = new TreeMap<String, Object>();
			System.out.println(question);
			triples.put("question", question);
			String questionExpr = "";
			String questionExprTest = "";
			ArrayList<PhraseTemplate> phrases = pp.preprocessPhrases(question,
					pp, pd);
			for (int i = 0; i < phrases.size(); i++) {
				PhraseTemplate phraseObj = phrases.get(i);
				String phrase = phraseObj.phrase;
				if (jr.IsJunkWord(phrase))
					continue;
				String phraseType = phraseObj.phraseType;
				String phrasePos = phraseObj.pos;
				questionExpr = questionExpr + " <" + phraseType + ">";
				questionExprTest = questionExprTest + " <" + phraseType + " : "
						+ phrase + ">";
			}
			questionExpr = questionExpr.trim();
			String salary = "";
			for (String regex : regexDict.keySet()) {
				String value = regexDict.get(regex);
				regex = regex.replaceAll(">", " [^<]+>");
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(questionExprTest);
				while (m.find()) {
					if (m.group().trim().length() > 1) {
						salary = m.group();
						Map<String, Object> ieMap = iee.RegExExtraction(salary,
								value);
						Map<String, Object> entityAttributes = new TreeMap<String, Object>();
						for (String key : ieMap.keySet()) {
							if (key.contains("Entity")) {
								entityAttributes = ae.getAttributes(
										ieMap.get(key).toString(), ae);
							}
						}
						for (String attributesKey : entityAttributes.keySet())
							ieMap.put(attributesKey,
									entityAttributes.get(attributesKey));

						Map<String, Object> tempieMap = new TreeMap<String, Object>(
								ieMap);
						for (String key : tempieMap.keySet()) {
							System.out.println("--------"+key);
							if (key.trim().equals("Entity")) {
								String mappedEntity = "";
								mappedEntity = ls.resolveEntity(ieMap.get(key)
										.toString(), "entitesIndex");
								if (mappedEntity.length() > 0
										&& mappedEntity.contains("__")) {
									ieMap.put("MappedEntity",
											mappedEntity.split("__")[4]);
									ieMap.put("MappedEntityType", mappedEntity.split("__")[2]);
									ieMap.put("EntityConfidenceScore", mappedEntity.split("__")[5]);
								}
							}
							if (key.trim().equals("Intent")) {
								String mappedIntent = "";
								mappedIntent = ls.resolveEntity(ieMap.get(key)
										.toString(), "entitesIndex");
								ieMap.put("MappedIntent", mappedIntent.split("__")[4]);
								ieMap.put("IntentConfidenceScore", mappedIntent.split("__")[5]);
								ieMap.put("Requirements", mappedIntent.split("__")[2]);
							}
						}
						System.out.println(ieMap.entrySet());
						intentEntities.add(ieMap);
					}
				}
			}
		}
		if (intentEntities.size() == 0) {
			intentEntities.add(ee.getIE(question, ee));

		}
		return intentEntities;
	}

	public static void main(String args[]) {
		IntentEntityExtractor iee = new IntentEntityExtractor();
		String question = "What is the position of my parcel?";
		iee.getIntentEntity(question, iee);
	}
}