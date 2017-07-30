package com.nlp.entityresolution;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
import com.nlp.stringsimilarity.SoftTfIdf;

public class EntityResolver {
	private IndexSearcher searcher;
	private QueryParser titleQueryParser;
	private QueryParser contentQueryParser;

	private static final String INDEX_DIR = "resources\\";
	private static final int DEFAULT_RESULT_SIZE = 100;

	NGramBasedDistance ngramDistance = new NGramBasedDistance();
	Levenshtein leven = new Levenshtein();
	Cosine cosine = new Cosine();
	SoftTfIdf softidf = new SoftTfIdf();
	Ngram ngram;
	

	public EntityResolver()  { 
		// create the index
		ngram=new Ngram();
		}

	
	static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e2.getValue().compareTo(e1.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
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
		Query query = titleQueryParser.parse(entityQuery);
		Query ngramQuery = contentQueryParser.parse(ngrams);
		// execute the query and get the results
		ScoreDoc[] queryResults = searcher.search(query, DEFAULT_RESULT_SIZE).scoreDocs;
		ScoreDoc[] queryResultsnGrams = searcher.search(ngramQuery, DEFAULT_RESULT_SIZE).scoreDocs;
		
		// process the results
		for (ScoreDoc scoreDoc : queryResults) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category"));
		}
		// process the results
		for (ScoreDoc scoreDoc : queryResultsnGrams) {
			Document doc = searcher.doc(scoreDoc.doc);
			level1Results.put(doc.get("Entity"), doc.get("ID")+"__"+doc.get("Category"));
		}
		return level1Results;
	}

	/**
	 * @param entityQuery Query for which match needs to be find out
	 * @param l1results Map of lucene results found from L1 search in Lucene
	 * @return Returns a Map of matched entities and their corresponding scores
	 */
	public TreeMap<String, Double> getLevel2Results(String entityQuery,
			Map<String, String> l1results) {
		TreeMap<String, Double> l2Results = new TreeMap<String, Double>();
		for (String ontologyEntity : l1results.keySet()) {
			String originalOntologyEntity = ontologyEntity;
			String id = l1results.get(ontologyEntity);
			ontologyEntity = ontologyEntity.toLowerCase().trim();
			ontologyEntity=ontologyEntity.replaceAll(".com", "");
			ontologyEntity=ontologyEntity.replaceAll(", ", " ");
			if (ontologyEntity.endsWith("."))
				ontologyEntity=ontologyEntity.substring(0,ontologyEntity.length()-1);
			ontologyEntity = ontologyEntity.toLowerCase().trim();
			entityQuery = entityQuery.toLowerCase().trim();
			entityQuery = Normalizer.normalize(entityQuery,
					Normalizer.Form.NFKD);
			Double ngramDistanceScore = ngramDistance.distance(entityQuery,
					ontologyEntity);
			Double levenScore = leven.distance(entityQuery, ontologyEntity);
			Double cosineScore = cosine.distance(entityQuery, ontologyEntity);
			Double softidfScore = softidf.distance(entityQuery, ontologyEntity);
			System.out.println(ngramDistanceScore);
			System.out.println(levenScore);
			System.out.println(cosineScore);
			System.out.println(softidfScore);
			System.out.println(entityQuery+" :: "+ ontologyEntity);
			if (Double.isNaN(cosineScore))
				cosineScore = 0.0;

			l2Results.put(id + "__" + originalOntologyEntity, Math.max(Math.max(
					Math.max(ngramDistanceScore, levenScore), cosineScore),
					softidfScore));
		}
		return l2Results;
	}


	public List<Object> resolveEntity(String entityName, String indexName) {

		List<Object> matchedEntities = new ArrayList<Object> ();
		try {
			Map<String, String> l1results = getLevel1Results(entityName,
					indexName);
			TreeMap<String, Double> l2results = getLevel2Results(entityName,
					l1results);
			SortedSet<Entry<String, Double>> results = entriesSortedByValues(l2results);
			Iterator<Entry<String, Double>> it = results.iterator();
			while (it.hasNext()) {
				TreeMap<String , String> temp=new TreeMap<String , String> ();
				Entry<String, Double> temprecord= it.next();
				String key=temprecord.getKey();
				String tokens[]=key.split("__");
				temp.put("id", tokens[0]);
				temp.put("entity_class", tokens[1]);
				temp.put("name", tokens[2]);
				temp.put("score", temprecord.getValue()+"");
				matchedEntities.add(temp);
				}
		} catch (Exception e) {
		}
		return matchedEntities;
	}

	public static void main(String args[]) throws IOException, ParseException {
		EntityResolver lss = new EntityResolver();
		String entityQuery = "task";
		// String cleaned_name=Normalizer.normalize(entityQuery,
		// Normalizer.Form.NFKD);
		System.out.println(lss.resolveEntity(entityQuery.toString()
				.toLowerCase(), "entitesIndex"));
		

	}
}
