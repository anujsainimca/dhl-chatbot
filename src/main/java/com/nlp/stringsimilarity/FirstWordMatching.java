package com.nlp.stringsimilarity;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

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


/**
 * The similarity between the two strings by comparing first word. If first
 * word matches it returns 1.0 as matching score. else no match. 
 * e.g. Abbott is a match for Abbott Laboratories.
 * 
 */
public class FirstWordMatching {

	private IndexSearcher searcher;
	private QueryParser titleQueryParser;
	private QueryParser contentQueryParser;

	private static final String INDEX_DIR = "resources\\";
	private static final int DEFAULT_RESULT_SIZE = 100;

	public Map<String, String> getLevel1Results(String entityQuery,
			String IndexName) throws IOException, ParseException {
		Map<String, String> level1Results = new HashMap<String, String>();
		searcher = new IndexSearcher(IndexReader.open(FSDirectory
				.open(new File(INDEX_DIR + IndexName))));
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		// defining the query parser to search entity tokens.
		titleQueryParser = new QueryParser(Version.LUCENE_36, "Entity",
				analyzer);
		// defining the query parser to search entity by n-grams.
		contentQueryParser = new QueryParser(Version.LUCENE_36, "EntityNgrams",
				analyzer);
		Query query = titleQueryParser.parse(entityQuery);
		// execute the query and get the results
		ScoreDoc[] queryResults = searcher.search(query, DEFAULT_RESULT_SIZE).scoreDocs;
		
		// process the results
		for (ScoreDoc scoreDoc : queryResults) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category")+"__"+doc.get("ticker")+"__"+doc.get("name")+"__"+doc.get("ric_code"));
		}
		// process the results
		return level1Results;
	}

	public String getFirstWordMatchinScore(String entityQuery) throws IOException, ParseException {
		Map<String, String> l1results = getLevel1Results(entityQuery,
				"entitesIndex");
		
		for (String ontologyEntity : l1results.keySet()) {
			String originalOntologyEntity = ontologyEntity;
			String id = l1results.get(ontologyEntity);
			ontologyEntity = ontologyEntity.toLowerCase().trim();
			ontologyEntity=ontologyEntity.replaceAll(".com", "");
			ontologyEntity=ontologyEntity.replaceAll(", ", " ");
			if (ontologyEntity.endsWith("."))
				ontologyEntity=ontologyEntity.substring(0,ontologyEntity.length()-1);
			entityQuery = entityQuery.toLowerCase().trim();
			entityQuery = Normalizer.normalize(entityQuery,
					Normalizer.Form.NFKD);
			Double firstWordMatchScore = similarityScore(entityQuery, ontologyEntity);
			
			if (firstWordMatchScore ==1.0)
				return id + "__" + originalOntologyEntity;
		}
		return "";
	}
	public double similarityScore(String s1, String s2) {
		if (s1.length()>2 && s2.length()>2)
		{
			if (s1.toLowerCase().startsWith(s2.toLowerCase()+" ") || s2.toLowerCase().startsWith(s1.toLowerCase()+" "))
				return 1.0;
		}
		return 0;
	}
	
    public static void main(String args[]) throws IOException, ParseException {
    	FirstWordMatching fwm= new FirstWordMatching();
		String s1 = "barrick";//"cleveland cavaliers";
		String matchingScore = fwm.getFirstWordMatchinScore(s1);
		System.out.println(matchingScore);
	}

}