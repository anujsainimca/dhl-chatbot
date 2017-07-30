package com.nlp.extraction;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

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

public class PhraseDetector {
	public SentenceDetector sentenceDetector;
	public Tokenizer tokenizer;
	public POSTaggerME tagger;
	public ChunkerME chunker;

	public PhraseDetector() {
		try {
			InputStream SentenceModelIn = this.getClass().getClassLoader()
					.getResourceAsStream("en-sent.bin");
			InputStream TokenModelIn = this.getClass().getClassLoader()
					.getResourceAsStream("en-token.bin");
			InputStream POSModelIn = this.getClass().getClassLoader()
					.getResourceAsStream("en-pos-maxent.bin");
			InputStream ChunkerModelIn = this.getClass().getClassLoader()
					.getResourceAsStream("en-chunker.bin");
			// ------------Loading Models--------------//

			SentenceModel SentenceModel = new SentenceModel(SentenceModelIn);
			TokenizerModel TokenModel = new TokenizerModel(TokenModelIn);
			POSModel POSModel = new POSModel(POSModelIn);
			ChunkerModel ChunkerModel = new ChunkerModel(ChunkerModelIn);

			// ------------Instantiating Models--------------//
			sentenceDetector = new SentenceDetectorME(SentenceModel);
			tokenizer = new TokenizerME(TokenModel);
			tagger = new POSTaggerME(POSModel);
			chunker = new ChunkerME(ChunkerModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<PhraseTemplate> GetPhrases(String sentence) {
		ArrayList<PhraseTemplate> al = new ArrayList<PhraseTemplate>();
		String tokens[] = tokenizer.tokenize(sentence);
		String tags[] = tagger.tag(tokens);
		String chunks[] = chunker.chunk(tokens, tags);
		String phrase = "";
		String pos = "";
		String phraseType = "";
		for (int i = 0; i < chunks.length; i++) {
			if (chunks[i].startsWith("B-") || chunks[i].startsWith("O")) {
				//System.out.println(tokens[i]+"#####"+chunks[i]);
				PhraseTemplate svo = new PhraseTemplate();
				svo.phrase = phrase;
				svo.pos = pos;
				svo.phraseType = phraseType;
				// This if condition is temporary to remove all PP and O phrases
				// if
				// (phraseType.trim().equals("SBAR")||phraseType.trim().equals("O")||phraseType.equals("PP"))
				// continue;
				al.add(svo);

				phrase = tokens[i];
				pos = tags[i];
				phraseType = chunks[i].replace("B-", "");
			}
			if (chunks[i].startsWith("I-")) {
				phrase = phrase + " " + tokens[i];
				pos = pos + " " + tags[i];
			}
			if (i == chunks.length - 1 && chunks[i].startsWith("B-")) {
				PhraseTemplate svo = new PhraseTemplate();
				svo.phrase = tokens[i];
				svo.pos = tags[i];
				svo.phraseType = chunks[i].replace("B-", "");
				al.add(svo);
			}
		}
		return al;
	}

	public static void main(String args[]) {
		PhraseDetector pd = new PhraseDetector();
		String input = "";

		try {
			// input=sc.getClaimText(claim_id);
			// input="Dr. Hockaday to grant received by KCTCS. ";
			// input="James reported that the Kentucky Cabinet for Families and Children is responsible for administering the Kentucky Transitional Assistance Program which is the cash assistance welfare program nationally known as Temporary Assistance for Needy Families. ";
			input="show me all projects in jira and github." ;
					
		} catch (Exception e)
		// catch (MalformedURLException | SolrServerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sentences[] = pd.sentenceDetector.sentDetect(input);
		for (String sentence : sentences) {

			ArrayList<PhraseTemplate> phrases = pd.GetPhrases(sentence);
			for (PhraseTemplate svo : phrases) {
				String key = svo.phrase;
				String value = svo.phraseType;
				String pos= svo.pos;
				System.out.println(key + " => " + value+ " => " + pos);
			}
		}
	}
}
