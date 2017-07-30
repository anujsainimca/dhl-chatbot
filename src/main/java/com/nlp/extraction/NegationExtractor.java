package com.nlp.extraction;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class NegationExtractor {

	public List<String> getNegations(String question) {
		String tokens[]=question.split("(dont show |dont like |don't show |don't like |except |dont show me |don't show me |do not show |do not show me )");
		List<String> attributes= new ArrayList<String>();
		for (int i=1;i<tokens.length;i++) {
			String negations[]=tokens[i].split("( in | for | to | at | on | across )")[0].split("(,| and )");
			
			for (String negation:negations)
				attributes.add(negation);
		}
		return attributes;

	}
	public static void main(String args[]) {
		NegationExtractor ne = new NegationExtractor();
		String question = "all projects except jira and github across all repositories ";
		System.out.println(ne.getNegations(question));

	}
}
