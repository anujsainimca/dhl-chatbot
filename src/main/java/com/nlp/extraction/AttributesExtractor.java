package com.nlp.extraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class AttributesExtractor {

	public TreeMap<String,Object> getAttributes(String question,AttributesExtractor pe ) {
		TreeMap<String,Object> attributesMap=new TreeMap<String,Object> ();
		String tokens[]=question.split("( in | for | to | at | on | across | with )");
		attributesMap.put("Entity", tokens[0]);
		List<String> negations= new ArrayList<String>();
		List<String> attributes= new ArrayList<String>();
		TreeMap<String,Object> negationsTempMap=pe.getNegations(tokens[0],"Entity");
		if (negationsTempMap.containsKey("EntityNegations")){
			ArrayList<String> al = (ArrayList<String>) negationsTempMap.get("EntityNegations");
			negations.addAll(al);
			attributesMap.put("Entity", negationsTempMap.get("Entity"));
		}
		for (int i=1;i<tokens.length;i++) {
			attributes.add(tokens[i].trim());
			negationsTempMap.clear();
			negationsTempMap=pe.getNegations(tokens[i],"EntityAttributes");
			if (negationsTempMap.containsKey("EntityNegations")){
				ArrayList<String> al = (ArrayList<String>) negationsTempMap.get("EntityNegations");
				negations.addAll(al);
				attributes.add( negationsTempMap.get("EntityAttributes").toString().trim());
			}
		}
		Set<String> newSet = new HashSet<String>(attributes);
		List<String> newList = new ArrayList<String>(newSet);
		attributesMap.put("EntityAtributes", newList);

		Set<String> newSet1 = new HashSet<String>(negations);
		List<String> newList1 = new ArrayList<String>(newSet1);
		attributesMap.put("EntityNegations", newList1 );
		return attributesMap;

	}

	public TreeMap<String,Object> getNegations(String question,String entity_type) {
		TreeMap<String,Object> attributesMap=new TreeMap<String,Object> ();
		String tokens[]=question.split("(dont show |dont like |don't show |don't like |except |dont show me |don't show me |do not show |do not show me )");
		List<String> attributes= new ArrayList<String>();
		for (int i=1;i<tokens.length;i++) {
			String negations[]=tokens[i].split("( in | for | to | at | on | across )")[0].split("(,| and )");
			
			for (String negation:negations)
				attributes.add(negation.trim());
		}
		
		attributesMap.put(entity_type, tokens[0].trim());
		attributesMap.put("EntityNegations", attributes );
		return attributesMap;

	}

	public static void main(String args[]) {
		AttributesExtractor pe = new AttributesExtractor();
		String question = "update all projectts across all repositories but dont";
		System.out.println(pe.getAttributes(question,pe));

	}
}
