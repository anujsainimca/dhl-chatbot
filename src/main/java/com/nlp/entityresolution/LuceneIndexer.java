package com.nlp.entityresolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneIndexer {
	private IndexWriter writer;
	Ngram ngram;
	

	public LuceneIndexer(String indexDir) throws IOException { 
		// create the index
		ngram=new Ngram();
		if(writer==null){
			writer= new IndexWriter(FSDirectory.open(new File(indexDir)), new IndexWriterConfig(Version.LUCENE_36,
					new StandardAnalyzer(Version.LUCENE_36)));
			} 
		}
	
	/**
	* This	method will add	the	items into index
	*/
	
	public void index(String id, String entityName) throws IOException
	{
		String ngrams=ngram.getNgrams(entityName, 3, ngram);
		Document doc = new Document();
		doc.add(new Field("ID", id.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("Entity", entityName, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field("EntityNgrams", ngrams, Field.Store.YES, Field.Index.ANALYZED));
		// add the document to the index
		writer.addDocument(doc);
	}
	
	/**
	* Closing the index
	*/
	public void close() throws IOException
	{
			writer.close();
	} 

	public static void main(String[] args) {

		
		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("D:/Personal/Scrapping/bloomberg/synapse.csv"));
			LuceneIndexer lsi=new LuceneIndexer("resources/SynapseLuceneIndex");
			while ((sCurrentLine = br.readLine()) != null) {
				String tokens[]=sCurrentLine.split(",");
				lsi.index(tokens[0], tokens[3]);
			}
			lsi.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}
}

