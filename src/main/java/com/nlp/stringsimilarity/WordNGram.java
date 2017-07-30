package com.nlp.stringsimilarity;

import java.util.*;

public class WordNGram {

	static WordNGram wng = new WordNGram();

	public List<String> ngrams(int n, String str) {
		List<String> ngrams = new ArrayList<String>();
		String[] words = str.split("[ ,\\.]");
		for (int i = 0; i < words.length - n + 1; i++)
			ngrams.add(concat(words, i, i + n));
		return ngrams;
	}

	public String concat(String[] words, int start, int end) {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < end; i++)
			sb.append((i > start ? " " : "") + words[i]);
		return sb.toString();
	}

	public List<String> getWordNGrams(String text, int grams) {
		List<String> ngrams = new ArrayList<String>();
		for (int n = 1; n <= grams; n++) {
			for (String ngram : wng.ngrams(n, text))
				ngrams.add(ngram);
		}
		return ngrams;
	}

	public static void main(String[] args) {

		WordNGram wng = new WordNGram();
		String text = "What is the 52 week range of Canadian Natural ";
		for (String ngram : wng.getWordNGrams(text, 5))
			System.out.println(ngram);

	}
}