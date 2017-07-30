package com.nlp.extraction;

public class SVO {
	public String sub = "";
	public String predicate = "";
	public String prep = "";
	public String obj = "";

	public void printSVO(SVO svo, String currentEntity) {
		System.out.println("---------------SVO----------------");
		System.out.println("SUBJECT::" + svo.sub);
		System.out.println("PREDICATE::" + svo.predicate);
		System.out.println("PRONOUN::" + svo.prep);
		System.out.println("OBJECT::" + svo.obj);
	}

}
