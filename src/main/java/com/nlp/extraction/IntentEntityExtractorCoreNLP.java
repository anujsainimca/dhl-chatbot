package com.nlp.extraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.queryParser.ParseException;

import com.nlp.entityresolution.LuceneSearcher;
import com.nlp.stringsimilarity.Synonyms;

public class IntentEntityExtractorCoreNLP {
	public AttributesExtractor ae;
	public NegationExtractor ne;
	public LuceneSearcher ls;
	public PreProcessing pp; 
	public PhraseDetector pd; 
	public FallBackListBased ee;
	public IntentEntityMatching im;
	public IntentEntityExtractorCoreNLP() {
		ae = new AttributesExtractor();
		ne = new NegationExtractor ();
		ls=new LuceneSearcher ();
		pp= new PreProcessing();
		pd=new PhraseDetector();
		 ee= new FallBackListBased();
		 im=new IntentEntityMatching();	 
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

	public List<Object> getIntentEntity(String question,
			IntentEntityExtractorCoreNLP iee) {
		List<Object> results = new ArrayList<Object>();
		List<Object> intentEntities = new ArrayList<Object>();
		if (!(question.endsWith("?") || question.endsWith(".")))
			question = question + "?";
		TreeMap<String, String> regexDict = new TreeMap<String, String>();
		regexDict.put("<ADVP> <ADJP> <PP> <NP>",
				"<ADVP : Intent> <ADJP> <PP> <NP : Entity>");
		regexDict.put("<VP> <PP> <NP>", "<VP : Intent> <PP> <NP : Entity>");
		regexDict.put("<VP> <NP>", "<VP : Intent> <NP : Entity>");

		// String sentences[] = pd.sentenceDetector.sentDetect(input);
		// for (String sentence : sentences)
		{
			int index = question.lastIndexOf(".");
			if (index != -1)
				question = question.substring(0, index) + " .";
			System.out.println(question);

			Map<String, Object> triples = new TreeMap<String, Object>();
			System.out.println(question);
			triples.put("question", question);
			String questionExpr = "";
			String questionExprTest = "";
			ArrayList<PhraseTemplate> phrases = pp.preprocessPhrases(question, pp, pd);
			for (int i = 0; i < phrases.size(); i++) {
				PhraseTemplate phraseObj = phrases.get(i);
				String phrase = phraseObj.phrase;
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
								entityAttributes = ae.getAttributes(ieMap.get(
										key).toString(),ae);
							}
						}
						for (String attributesKey : entityAttributes.keySet())
							ieMap.put(attributesKey,entityAttributes.get(attributesKey));
						
						System.out.println(ieMap.entrySet());
						for (String key : ieMap.keySet()) {
							if (key.trim().equals("Entity")) {
								String mappedEntity="";
								try {
									mappedEntity = im.getLevel1Results(ieMap.get(key).toString(),"entitesIndex");
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ParseException e) {
									e.printStackTrace();
								}
								Map<String,Object> tempieMap= new TreeMap<String, Object>();
								tempieMap.put("entity", mappedEntity);
								results.add(tempieMap);
							}
							if (key.trim().equals("EntityAtributes")) {
								String mappedEntity="";
								ArrayList<String> al1 = (ArrayList<String>) ieMap.get(key);
								for (String keyvalue:al1)
								{try {
									mappedEntity = im.getLevel1Results(keyvalue,"entitesIndex");
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ParseException e) {
									e.printStackTrace();
								}}
								Map<String,Object> tempieMap= new TreeMap<String, Object>();
								tempieMap.put("entity", mappedEntity);
								results.add(tempieMap);
							}
							if (key.trim().equals("Intent")) {
								String mappedIntent="";
								try {
									mappedIntent= im.getLevel1Results(ieMap.get(key).toString(),"entitesIndex");
								} catch (IOException e) {
									e.printStackTrace();
								} catch (ParseException e) {
									e.printStackTrace();
								}
								Map<String,Object> tempieMap= new TreeMap<String, Object>();
								tempieMap.put("intent", mappedIntent);
								results.add(tempieMap);
							}
							}
					}
				}
			}
		}
		System.out.println(results.get(0));
		return results;
	}

	public static void main(String args[]) {
		IntentEntityExtractorCoreNLP iee = new IntentEntityExtractorCoreNLP();
		String question = "hey, can you create a project in jira?";
		iee.getIntentEntity(question, iee);
		System.out.println("------------------------");
		//
		// question ="Can you Please create Project in GITHUB ?";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Help me in creating project with name XYZ ?";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Use XYZ project to start building ?";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "How many Tasks open in X Project current sprint?";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question =
		// "please Assign NLP POC Task to Akil and do create sprint as well";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Request you to create my tasks in project XYZ";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Start build for my project";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Help me in pushing branch abc to github";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "create. Branch  XYZ";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Create New Project";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Use X project";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Start Build";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Create New Branch in Github and CI/CD pipeline";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Create New Task";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Assign NLP POC Task to Akil";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "How many Tasks open in X Project current sprint";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "push branch to github";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "pull branch from github";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Open a push request";
		// iee.getIntentEntity(question);
		// System.out.println("------------------------");
		//
		// question = "Open a pull request";
		// iee.getIntentEntity(question);
	}

}
