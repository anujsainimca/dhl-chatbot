package com.nlp.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.nlp.entityresolution.Ngram;
import com.nlp.stringsimilarity.WordNetSynonyms;

public class LuceneIndexing {
	private IndexWriter writer;
	Ngram ngram;
	WordNetSynonyms wns;
	TreeMap<String, String> synonymsDict= new TreeMap<String, String> ();
	public LuceneIndexing(String indexDir) throws IOException {
		// create the index
		ngram = new Ngram();
		wns = new WordNetSynonyms();
		if (writer == null) {
			writer = new IndexWriter(FSDirectory.open(new File(indexDir)),
					new IndexWriterConfig(Version.LUCENE_36,
							new StandardAnalyzer(Version.LUCENE_36)));
		}
	}

	/**
	 * This method will add the items into index
	 */

	public static void main(String args[]) throws IOException {
		File fin = new File("D:/Personal/Competitions/Hackererath/DHL/intentEntities.txt");
		BufferedReader br = new BufferedReader(new FileReader(fin));
		Ngram ngram = new Ngram();
		int id = 0;
		LuceneIndexing lsi = new LuceneIndexing("resources/entitesIndex");
		String line = null;
		while ((line = br.readLine()) != null) {
			Map<String, String> entities = new TreeMap<String, String>();
			String tokens[] = line.split(":");
			System.out.println(tokens.length);
			String entity = tokens[0];
			String category = tokens[1];
			String requirements = tokens[2];
			lsi.index(id++, entity, category, requirements, lsi);

		}
		lsi.writer.close();
		
		try
		{
		    String filename= "resources/synonyms.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    for (String key:lsi.synonymsDict.keySet())
		    {
			    fw.write("\n");//appends the string to the file
			    fw.write(key+":"+lsi.synonymsDict.get(key));//appends the string to the file
		    }
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}
	public void index(int id, String entity,
			String category, String requirements, LuceneIndexing lsi)
			throws IOException {
		String ngrams = ngram.getNgrams(entity, 3, ngram);
		List<String> syns = wns.searchWord(entity);
		String synonyms="";
		for (String syn : syns){
			synonyms=synonyms+", "+syn;
		}
		if (synonyms.startsWith(", "))
			synonyms=synonyms.replaceFirst(", ",  "");
		synonymsDict.put(entity, synonyms);
		Document doc = new Document();
		doc.add(new Field("ID", id++ + "", Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field("Entity", entity, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("Category", category, Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("requirements", requirements, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("EntityNgrams", ngrams, Field.Store.YES,
				Field.Index.ANALYZED));
		// add the document to the index
		lsi.writer.addDocument(doc);
	}

	/**
	 * Closing the index
	 */
	public void close() throws IOException {
		writer.close();
	}

}
