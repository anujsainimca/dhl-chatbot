package com.nlp.extraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import com.nlp.entityresolution.LuceneSearcher;
import com.nlp.stringsimilarity.WordNGram;

/**
 * @author asai13
 * This class search for parcels/status or any another query given by question understanding unit
 */
public class AnswerFinder {
	HttpSolrServer solr = new HttpSolrServer("http://localhost:8983/solr/parcelDetails");
	HttpSolrServer weightsolr = new HttpSolrServer("http://localhost:8983/solr/weights");


	/**
	 * @param query Query to be searched
	 * @param output fields to be returned
	 * @return json object of results
	 * @throws SolrServerException
	 */
	public ArrayList<Map<String, Object>> getSolrAnswer(String query, String output, String requirements) throws SolrServerException
	{
		ArrayList<Map<String, Object>> al = new ArrayList<Map<String, Object>>();
		
		System.out.println(query);
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery(query);
		System.out.println(query);
		
		try{
		QueryResponse response = null;
		if (query.contains("example:"))
			response = weightsolr.query(solrQuery);
		else
			response = solr.query(solrQuery);

		SolrDocumentList results = response.getResults();
		System.out.println(results.size());
		for (int i = 0; i < results.size(); ++i) {
			Map<String, Object> tempMap = new TreeMap<String, Object>();
			tempMap.put("response", results.get(i).get(output));
			tempMap.put("parcel_data", results.get(i));
			al.add(tempMap);
		}
		}
		catch(Exception e)
		{
			Map<String, Object> tempMap = new TreeMap<String, Object>();
			tempMap.put("response", "Please provide "+requirements);
			tempMap.put("parcel_data", "");
			al.add(tempMap);
			return al;
		}
		return al;
	}

	public static void main(String args[]) throws IOException {
		try {
			String question="what is the price for box 7";
			String query = "(parcel_type:("+question +") OR location:("+question +") OR example:("+question+"))";
			String output="price";
			String req="order_id";
			AnswerFinder ee = new AnswerFinder();
			System.out.println(ee.getSolrAnswer(query,output,req));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
