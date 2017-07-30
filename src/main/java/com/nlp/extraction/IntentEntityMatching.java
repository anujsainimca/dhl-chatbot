package com.nlp.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

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
import com.nlp.entityresolution.Ngram;
import com.nlp.stringsimilarity.Cosine;
import com.nlp.stringsimilarity.Levenshtein;
import com.nlp.stringsimilarity.NGramBasedDistance;
import com.nlp.stringsimilarity.SoftTfIdf;
import com.nlp.stringsimilarity.Synonyms;
import com.nlp.stringsimilarity.WordNGram;

public class IntentEntityMatching {
	private IndexSearcher searcher;
	private QueryParser titleQueryParser;
	private QueryParser contentQueryParser;
	public LuceneSearcher lss;
	private static final String INDEX_DIR = "resources\\entitesIndex";
	private static final int DEFAULT_RESULT_SIZE = 1;
	public Synonyms syn;

	Ngram ngram;

	public IntentEntityMatching() {
		try {
			ngram=new Ngram();
			 syn = new Synonyms();
			searcher = new IndexSearcher(IndexReader.open(FSDirectory
					.open(new File(INDEX_DIR))));
		} catch (Exception e) {
		}
	}

	public String getLevel1Results(String entityQuery,
			String entity_type) throws IOException, ParseException {
		entityQuery=syn.getSynonym(entityQuery); 
		Map<String, String> level1Results = new HashMap<String, String>();
		String ngrams=ngram.getNgrams(entityQuery, 3, ngram);
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		// defining the query parser to search entity tokens.
		titleQueryParser = new QueryParser(Version.LUCENE_36, "Entity",
				analyzer);
		// defining the query parser to search entity by n-grams.
		contentQueryParser = new QueryParser(Version.LUCENE_36, "EntityNgrams",
				analyzer);
		Query query = titleQueryParser.parse(entityQuery);
		Query ngramQuery = contentQueryParser.parse(ngrams);
		// execute the query and get the results
		ScoreDoc[] queryResults = searcher.search(query, DEFAULT_RESULT_SIZE).scoreDocs;
		ScoreDoc[] queryResultsnGrams = searcher.search(ngramQuery, DEFAULT_RESULT_SIZE).scoreDocs;
		
		// process the results
		for (ScoreDoc scoreDoc : queryResults) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category")+"__"+doc.get("ticker")+"__"+doc.get("name")+"__"+doc.get("ric_code"));
		}
		// process the results
		for (ScoreDoc scoreDoc : queryResultsnGrams) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category")+"__"+doc.get("ticker")+"__"+doc.get("name")+"__"+doc.get("ric_code"));
		}
		if (level1Results.size()>0)
			return (String) level1Results.keySet().toArray()[0]; 
		else
			return "";
	}


	public static void main(String args[]) throws IOException {
		String input = "";
		try {

			input = "jira";
			String entity_type="entity";
			System.out.println(input);
			IntentEntityMatching ee = new IntentEntityMatching();
			System.out.println(ee.getLevel1Results(input, entity_type));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
