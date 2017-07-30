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

import com.nlp.attributes.IDExtractor;
import com.nlp.entityresolution.LuceneSearcher;
import com.nlp.stringsimilarity.WordNGram;

/**
 * @author asai13
 * This class search for parcels/status or any another query given by question understanding unit
 */
public class AnswerGenerator {
	IDExtractor ide;
	IntentEntityExtractor iee;
	AnswerFinder af;
	public AnswerGenerator() {
		ide=new IDExtractor();
		iee = new IntentEntityExtractor();
		af=new AnswerFinder();
	}
	public Map<Object,Object>  getAnswer(String question) throws SolrServerException 
	{
		ArrayList<Map<String, Object>> al = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> parsedQuestion=iee.getIntentEntity(question, iee);
		String order_id=ide.getID(question);
		System.out.println(parsedQuestion);
		System.out.println(order_id);
		String query="parcel_id:"+order_id;
		String intent=parsedQuestion.get(0).get("MappedIntent").toString();
		String entity=parsedQuestion.get(0).get("MappedEntity").toString();
		String requirement=parsedQuestion.get(0).get("Requirements").toString();
		al=af.getSolrAnswer(query,intent,requirement);
		System.out.println(al);
		Object parcel_data=al.get(0).get("parcel_data");
		Object response=al.get(0).get("response");
		Map<Object,Object> responseMap = new TreeMap<Object,Object>();
		if (response.toString().startsWith("Please"))
			responseMap.put("response", response);
		else
			responseMap.put("response", "The "+intent+ " of  your "+entity +" is "+response);
		responseMap.put("label", query); 
		responseMap.put("url", parcel_data); 
		List<Object> jsonResponse=new ArrayList<Object> ();
		Map<Object,Object> tempMap= new TreeMap<Object,Object>();
		tempMap.put("parcel_id",order_id);
		tempMap.put("entity",entity);
		tempMap.put("intent",intent);
		jsonResponse.add(tempMap);
		
		responseMap.put("answer_explain", jsonResponse); 
		return responseMap ;

	}

	public static void main(String args[]) throws IOException {
		try {
			String question = "what is the status of my parcel my order id is 3873173242 " ;
			AnswerGenerator ag = new AnswerGenerator();
			System.out.println(ag.getAnswer(question));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
