package com.nlp.indexing;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WeightIndexer {
	public static void main(String[] args) throws IOException,
			SolrServerException {
		File fin = new File("D:/Personal/Competitions/Hackererath/DHL/weights.txt");
		HttpSolrServer server = new HttpSolrServer(
				"http://localhost:8983/solr/weights");
		int i = 0;
		BufferedReader br = new BufferedReader(new FileReader(fin));

		String line = null;
		while ((line = br.readLine()) != null) {
			String tokens[]=line.split("\t");
					int id = 1;
					String parcel_type= "";
					String location= "";
					String weight = "";
					String example= "";
					String price= "";
					String duration= "";
					parcel_type = tokens[0].trim();
					location= tokens[1].trim();
					price = tokens[2].trim();
					duration= tokens[3].trim();
					example= tokens[4].trim();
					weight= tokens[5].trim();
					SolrInputDocument doc = new SolrInputDocument();
					doc.addField("parcel_type", parcel_type);
					doc.addField("location", location);
					doc.addField("weight", weight);
					doc.addField("price", price);
					doc.addField("example", example);
					doc.addField("duration", duration);
					doc.addField("id", i +"");
					//System.out.println(coordinates);
					server.add(doc);
					i++;
					System.out.println(i);
				
			if (i % 10000 == 0)
				server.commit(); // periodically flush
		}

		br.close();
		server.commit();
	}
}
