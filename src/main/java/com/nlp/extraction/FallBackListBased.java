package com.nlp.extraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import com.nlp.entityresolution.LuceneSearcher;
import com.nlp.stringsimilarity.WordNGram;

public class FallBackListBased {
	public SentenceDetector sentenceDetector;
	public Tokenizer tokenizer;
	public POSTaggerME tagger;
	public ChunkerME chunker;
	public LuceneSearcher lss;

	public FallBackListBased() {
		try {
			lss = new LuceneSearcher();
			InputStream sentenceModelIn = this.getClass().getClassLoader()
					.getResourceAsStream("en-sent.bin");
			// ------------Loading Models--------------//

			SentenceModel SentenceModel = new SentenceModel(sentenceModelIn);

			// ------------Instantiating Models--------------//
			sentenceDetector = new SentenceDetectorME(SentenceModel);
		} catch (Exception e) {
		}
	}

	public ArrayList<PhraseTemplate> GetPhrases(String sentence, int sentenceId) {
		ArrayList<PhraseTemplate> al = new ArrayList<PhraseTemplate>();
		WordNGram wng = new WordNGram();
		List<String> ngrams = wng.getWordNGrams(sentence, 5);
		HashSet<String> hs = new HashSet<String>();
		hs.addAll(ngrams);
		ngrams.clear();
		ngrams.addAll(hs);
		for (String ngram : ngrams) {
			PhraseTemplate svo = new PhraseTemplate();
			Pattern p = Pattern.compile(ngram);
			Matcher m = p.matcher(sentence);
			while (m.find()) {
				svo.phraseStart = m.start();
				svo.phraseEnd = m.end();
			}

			svo.phrase = ngram;
			svo.pos = "XXX";
			svo.sentenceId = sentenceId;
			svo.phraseType = "XXX";
			al.add(svo);
		}

		return al;
	}


	public Map<String, Object> getIE(String text, FallBackListBased pd) {
		// Preprocessing. Replace all " - " combinations with a comma.
		text = text.replaceAll(" � ", ", ");
		text = text.replaceAll("'s ", " ");
		text = text.replaceAll("'s", "");
		text = text.replaceAll("�s ", " ");
		text = text.replaceAll("�s ", " ");
		text = text.replaceAll("\\?", "");
		text = text.replaceAll(":", "");
		Map<String, Object> entities_map = new TreeMap<String, Object>();
		Map<String, Object> extractedEntities = new TreeMap<String, Object>();
		List<TreeMap<String, String>> entities = new ArrayList<TreeMap<String, String>>();
		List<TreeMap<String, String>> intents= new ArrayList<TreeMap<String, String>>();
		Set<String> intentsList=new HashSet<String>();
		Set<String> entitiesList=new HashSet<String>();
		
		String input = text;
		// text=text.replaceAll("  ", " ");
		Span spanSentences[] = pd.sentenceDetector.sentPosDetect(input);
		int sentenceId = 1;
		TreeMap<String, String> intents_map = new TreeMap<String, String>();
		Double entites_highest_cofidence = 0.0;
		Double intents_highest_cofidence = 0.0;
		for (Span sentence : spanSentences) {
			ArrayList<PhraseTemplate> phrases = pd.GetPhrases(sentence
					.getCoveredText(input).toString(), sentenceId++);
			
			for (PhraseTemplate svo : phrases) {
				String key = svo.phrase;
				key = key.replaceAll(" is ", "");
				key  = key.replaceAll("What ", "");
				key  = key.replaceAll("what ", "");
				key  = key.replaceAll(" the ", "");

				if (key.length() > 1) {
					try {
						String resolvedEntity = lss.resolveEntity(key
								.toLowerCase().trim().toString(),
								"entitesIndex");
						String tokens[] = resolvedEntity.split("__");
//						System.out.println(resolvedEntity+" :: "+key);
						if (tokens.length < 2)
							continue;
						if (tokens[1].trim().startsWith("entity")
								&& Double.parseDouble(tokens[5].trim())>0.9)
							entitiesList.add(tokens[5].trim());
						if (tokens[1].trim().startsWith("entity")
								&& Double.parseDouble(tokens[5].trim()) > entites_highest_cofidence) {
							entities_map.clear();
							entities_map.put("Entity", key.trim());
							entities_map.put("MappedEntity", tokens[4].trim());
							entities_map.put("EntityConfidenceScore", tokens[5].trim());
							entities_map.put("MappedEntityType", tokens[2].trim());
							entites_highest_cofidence = Double
									.parseDouble(tokens[5].trim());
						}
						if (tokens[1].trim().startsWith("intent")
								&& Double.parseDouble(tokens[5].trim())>0.9)
							intentsList.add(tokens[5].trim());
						if (tokens[1].trim().startsWith("intent")
								&& Double.parseDouble(tokens[5].trim()) > intents_highest_cofidence) {
							intents_map.clear();
							intents_map.put("Id", tokens[0].trim());
							intents_map.put("Extracted Entity", key.trim());
							intents_map.put("Mapped Entity", tokens[4].trim());
							intents_map.put("Confidence", tokens[5].trim());
							intents_map.put("Class", tokens[1].trim());
							intents_highest_cofidence = Double
									.parseDouble(tokens[5].trim());
						}
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		entities_map.put("Intent", intents_map.get("Extracted Entity"));
		entities_map.put("MappedIntent", intents_map.get("Mapped Entity"));
		entities_map.put("IntentConfidenceScore", intents_map.get("Confidence"));

		return entities_map;
	}

	public static void main(String args[]) throws IOException {
		String input = "";
		try {

			input = "Can you please tell  generic name for Omesce?";
			System.out.println(input);
			FallBackListBased ee = new FallBackListBased();
			System.out.println(ee.getIE(input, ee).entrySet());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
