package com.nlp.entityresolution;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

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

import com.nlp.stringsimilarity.Cosine;
import com.nlp.stringsimilarity.Levenshtein;
import com.nlp.stringsimilarity.NGramBasedDistance;
import com.nlp.stringsimilarity.PreProcess;
import com.nlp.stringsimilarity.SoftTfIdf;
import com.nlp.stringsimilarity.Synonyms;

public class LuceneSearcher {
	private IndexSearcher searcher;
	private QueryParser titleQueryParser;
	private QueryParser contentQueryParser;

	private static final String INDEX_DIR = "resources/";
	private static final int DEFAULT_RESULT_SIZE = 100;

	NGramBasedDistance ngramDistance = new NGramBasedDistance();
	Levenshtein leven = new Levenshtein();
	Cosine cosine = new Cosine();
	SoftTfIdf softidf = new SoftTfIdf();
	Ngram ngram;
	Synonyms syn;
	PreProcess pp;
	public LuceneSearcher()  { 
		// create the index
		ngram=new Ngram();
		syn=new Synonyms();
		pp= new PreProcess();

		}

	/**
	 * @param entityQuery
	 *            Query for which match needs to be find out
	 * @param IndexName
	 *            Lucene Index name, where query needs to be executed
	 * @return Returns a Map of candidate matches found in Lucene
	 * @throws IOException
	 *             Index not found exception
	 * @throws ParseException
	 *             Exception if not able to parse Lucene Query
	 */
	public Map<String, String> getLevel1Results(String entityQuery,
			String IndexName) throws IOException, ParseException {
		Map<String, String> level1Results = new HashMap<String, String>();
		String ngrams=ngram.getNgrams(entityQuery, 3, ngram);
		searcher = new IndexSearcher(IndexReader.open(FSDirectory
				.open(new File(INDEX_DIR + IndexName))));
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		// defining the query parser to search entity tokens.
		titleQueryParser = new QueryParser(Version.LUCENE_36, "Entity",
				analyzer);
		// defining the query parser to search entity by n-grams.
		contentQueryParser = new QueryParser(Version.LUCENE_36, "EntityNgrams",
				analyzer);
		System.out.println("::"+entityQuery);
		Query query = titleQueryParser.parse(entityQuery);
		Query ngramQuery = contentQueryParser.parse(ngrams);
		// execute the query and get the results
		ScoreDoc[] queryResults = searcher.search(query, DEFAULT_RESULT_SIZE).scoreDocs;
		ScoreDoc[] queryResultsnGrams = searcher.search(ngramQuery, DEFAULT_RESULT_SIZE).scoreDocs;
		
		// process the results
		for (ScoreDoc scoreDoc : queryResults) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category")+"__"+doc.get("requirements")+"__"+doc.get("name"));
		}
		// process the results
		for (ScoreDoc scoreDoc : queryResultsnGrams) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category")+"__"+doc.get("requirements")+"__"+doc.get("name"));
		}
		return level1Results;
	}

	/**
	 * @param entityQuery Query for which match needs to be find out
	 * @param l1results Map of lucene results found from L1 search in Lucene
	 * @return Returns a Map of matched entities and their corresponding scores
	 */
	public TreeMap<Double, String> getLevel2Results(String entityQuery,
			Map<String, String> l1results) {
		TreeMap<Double, String> l2Results = new TreeMap<Double, String>();
		for (String ontologyEntity : l1results.keySet()) {
			String originalOntologyEntity = ontologyEntity;
			String id = l1results.get(ontologyEntity);
			ontologyEntity = ontologyEntity.toLowerCase().trim();
			ontologyEntity=ontologyEntity.replaceAll("_", " ");
			entityQuery=entityQuery.replaceAll(", ", " ");
			if (entityQuery.endsWith("."))
				entityQuery=entityQuery.substring(0,entityQuery.length()-1);
			ontologyEntity=ontologyEntity.replaceAll(", ", " ");
			if (ontologyEntity.endsWith("."))
				ontologyEntity=ontologyEntity.substring(0,ontologyEntity.length()-1);
			entityQuery = entityQuery.toLowerCase().trim();
			entityQuery = Normalizer.normalize(entityQuery,
					Normalizer.Form.NFKD);
			Double ngramDistanceScore = ngramDistance.distance(entityQuery,
					ontologyEntity);
			Double levenScore = leven.distance(entityQuery, ontologyEntity);
			Double cosineScore = cosine.distance(entityQuery, ontologyEntity);
			Double softidfScore = softidf.distance(entityQuery, ontologyEntity);
//			System.out.println(cosineScore);
//			System.out.println(ngramDistanceScore);
//			System.out.println(softidfScore);
//			System.out.println(levenScore);
//			System.out.println(entityQuery+" :: "+ ontologyEntity);
			
			if (Double.isNaN(cosineScore))
				cosineScore = 0.0;
			l2Results.put(Math.max(Math.max(
					Math.max(ngramDistanceScore, levenScore), cosineScore),
					softidfScore), id + "__" + originalOntologyEntity);
		}
		return l2Results;
	}

	public String resolveEntity(String entityName, String indexName) {
		entityName=syn.getProcessedSynonym(entityName);
		String matchedEntity = "";
		try {
			Map<String, String> l1results = getLevel1Results(entityName,
					indexName);
			TreeMap<Double, String> l2results = getLevel2Results(entityName,
					l1results);
			if (l2results.size()>0)
			matchedEntity = l2results.lastEntry().getValue() + "__"
					+ l2results.lastEntry().getKey().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return matchedEntity;
	}

	public static void main(String args[]) throws IOException, ParseException {
		LuceneSearcher lss = new LuceneSearcher();
		String entityQuery = "is the position";
		// String cleaned_name=Normalizer.normalize(entityQuery,
		// Normalizer.Form.NFKD);
		System.out.println(lss.resolveEntity(entityQuery.toString()
				.toLowerCase(), "entitesIndex"));

	}
}
