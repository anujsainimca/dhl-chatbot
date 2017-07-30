package com.nlp.springboot;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.nlp.extraction.AnswerFinder;
import com.nlp.extraction.AnswerGenerator;
import com.nlp.extraction.DummySearch;
import com.nlp.extraction.IntentEntityExtractor;

@RestController
@EnableAutoConfiguration
public class IntentEntityExtractionService {

	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	IntentEntityExtractor iee=new IntentEntityExtractor();
	@RequestMapping(value = "/getIntentEntities", method = RequestMethod.POST, produces = {
			"application/json", "application/xml" })
	List<Map<String, Object>> getIntentEntities(@RequestBody Map<String, Object> payload) {
		return iee.getIntentEntity(payload.get("question").toString(),iee);
	}

	AnswerGenerator ag=new AnswerGenerator();
	List<String> queries= new ArrayList<String>();
	String prevQuestion="";
	String prevResponse="";
	@RequestMapping(value = "/getResponse", method = RequestMethod.POST, produces = {
			"application/json", "application/xml" })
	Map<Object,Object> getAnswer(@RequestBody Map<String, Object> payload) throws SolrServerException {
		queries.add(payload.get("question").toString());
		Map<Object,Object> response=new TreeMap<Object,Object> ();
		if (prevResponse.startsWith("Please"))
			response=ag.getAnswer(prevQuestion+" "+payload.get("question").toString());
		else
			response=ag.getAnswer(payload.get("question").toString());
		prevResponse=response.get("response").toString();
		prevQuestion=payload.get("question").toString();
		return response;
	}

	AnswerFinder af=new AnswerFinder();
	@RequestMapping(value = "/getSolrResponse", method = RequestMethod.POST, produces = {
			"application/json", "application/xml" })
	ArrayList<Map<String, Object>> geSolrtAnswer(@RequestBody Map<String, Object> payload) throws SolrServerException {
		return af.getSolrAnswer(payload.get("query").toString(),payload.get("response").toString(),payload.get("requirement").toString());
	}

	DummySearch ts=new DummySearch();
	@RequestMapping(value = "/getDummyResponse", method = RequestMethod.POST, produces = {
			"application/json", "application/xml" })
	Map<Object,Object> getDummyResponse(@RequestBody Map<String, Object> payload) throws SolrServerException {
		return ts.getTriples(payload.get("question").toString());
	}

	
	
	public static void main(String[] args) throws Exception {
		System.getProperties().put("server.port", 8083);
		SpringApplication.run(IntentEntityExtractionService.class, args);
	}

}	