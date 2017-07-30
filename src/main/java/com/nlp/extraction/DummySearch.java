package com.nlp.extraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DummySearch {
	public static String currentEntity = "";
	public static ArrayList<String> days = new ArrayList<String>();

	public static void main(String args[]) {
		PhraseDetector pd = new PhraseDetector();
		String input = "";
		DummySearch te = new DummySearch();
		try {
			input = "create Branch XYZ?";
			// input = "What is target price for msft?";
			te.getTriples(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public Map<Object,Object> getTriples(String input) {
		Map<Object,Object> responseMap = new TreeMap<Object,Object>();
		responseMap.put("response", "The instruction data for this shipment have been provided by the sender to DHL electronically"); 
		responseMap.put("label", "courier with weight 500 gm to delhi"); 
		responseMap.put("url", "http://parcel.dhl.co.uk/dhl-service-point/size-and-price-guide"); 
		List<Object> jsonResponse=new ArrayList<Object> ();
		Map<Object,Object> tempMap= new TreeMap<Object,Object>();
		tempMap.put("id",2343);
		tempMap.put("weight",23.43);
		tempMap.put("city","delhi");
		tempMap.put("enitty","courier");
		tempMap.put("intent","find");
		jsonResponse.add(tempMap);
		
		responseMap.put("answer_explain", jsonResponse); 
		return responseMap ;
	}
}
