package com.nlp.stringsimilarity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class WordNetSynonyms {

	public IDictionary dictionary = null;
	public static void main(String args[]) {
		WordNetSynonyms w = new WordNetSynonyms();
		List<String> syns = w.searchWord("delhi");
		for (String syn : syns)
			System.out.println(syn);
	}


public WordNetSynonyms() {
	/*
	 * IDictionary is the main interface for acessing WordNet dictionary Files.
	 * Dictionary class implements IDictionary interface.
	 */


		try {
			/*
			 * 'path' holds the loaction of the WordNet dictionary files. In
			 * this code it is assumed that the dictionary files are located
			 * under "WordNet/dict3.0/" directory. With the WordNet directory &
			 * this class present in same directory
			 */
			String path = "resources/WordNet" + File.separator + "dict";
			URL url = new URL("file", null, path);

			// construct the dictionary object and open it
			dictionary = new Dictionary(url);
			dictionary.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> searchWord(String key) {

		key = key.replaceAll(" ", "_").toLowerCase().trim();
		List<String> synonyms = new ArrayList<String>();
		/*
		 * A word is having a different WordId in different synsets. Each Word
		 * is having a unique Index.
		 */

		// Get the index associated with the word, 'book' with Parts of Speech
		// NOUN.
		IIndexWord idxWord = dictionary.getIndexWord(key, POS.NOUN);

		int i = 1;

		/*
		 * getWordIDs() returns all the WordID associated with a index
		 */
		try {
			for (IWordID wordID : idxWord.getWordIDs()) {
				// Construct an IWord object representing word associated with
				// wordID
				IWord word = dictionary.getWord(wordID);

				// Get the synset in which word is present.
				ISynset wordSynset = word.getSynset();

				// Returns all the words present in the synset wordSynset
				for (IWord synonym : wordSynset.getWords()) {
					synonyms.add(synonym.getLemma());
				}
				i++;
			}
		} catch (Exception e) {
			synonyms.add(key);
			return synonyms;
		}
		synonyms = new ArrayList<String>(new LinkedHashSet<String>(synonyms));
		return synonyms;
	}
}