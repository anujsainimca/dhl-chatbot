package com.nlp.indexing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JsonIndexer {

	public static void main(String[] args) {

		JSONParser parser = new JSONParser();

		try {
			HttpSolrServer server = new HttpSolrServer(
					"http://localhost:8983/solr/parcelDetails");

			SolrInputDocument doc = new SolrInputDocument();
			Object obj = parser.parse(new FileReader(
					"D:/Personal/Competitions/Hackererath/DHL/dhl-dsl.json"));

			JSONObject jsonObject = (JSONObject) obj;
			System.out.println(jsonObject);

			JSONObject parcelObject = (JSONObject) jsonObject.get("parcel");
			JSONObject addressToObject = (JSONObject) ((JSONObject) parcelObject
					.get("address")).get("to");
			JSONObject addressFromObject = (JSONObject) ((JSONObject) parcelObject
					.get("address")).get("from");
			JSONObject trackingObject = (JSONObject) parcelObject
					.get("tracking");
			JSONObject notificationObject = (JSONObject) trackingObject
					.get("notification");
			long parcelid = (Long) parcelObject.get("id");
			System.out.println();
			doc.addField("parcel_id", parcelid);

			String description = (String) parcelObject.get("description");
			System.out.println(description);
			doc.addField("description", description);

			String type = (String) parcelObject.get("type");
			System.out.println(type);
			doc.addField("parcel_type", type);

			System.out.println(parcelObject.get("weight (lbs)"));
			String weight = (String)parcelObject.get("weight (lbs)");
			Float pweight=Float.parseFloat(weight);
			System.out.println(pweight);
			doc.addField("weight", pweight);

			String addressToName = (String) addressToObject.get("name");
			System.out.println(addressToName);
			doc.addField("address_to_name", addressToName);
			String addressToAddress = (String) addressToObject.get("address");
			System.out.println(addressToAddress);
			doc.addField("address_to_address", addressToAddress);
			String addressToZip = (String) addressToObject.get("zip");
			System.out.println(addressToZip);
			doc.addField("address_to_zip", addressToZip);

			String addressFromName = (String) addressFromObject.get("name");
			System.out.println(addressFromName);
			doc.addField("address_from_name", addressFromName);
			String addressFromAddress = (String) addressFromObject
					.get("address");
			System.out.println(addressFromAddress);
			doc.addField("address_from_address", addressFromAddress);
			String addressFromZip = (String) addressFromObject.get("zip");
			System.out.println(addressFromZip);
			doc.addField("address_from_zip", addressFromZip);

			String email = (String) notificationObject.get("email");
			System.out.println(email);
			doc.addField("email", email);
			String phone = (String) notificationObject.get("text");
			System.out.println(phone);
			doc.addField("phone", phone);

			String current_status = (String) trackingObject
					.get("current_status");
			doc.addField("current_status", current_status);
			System.out.println(current_status);
			// loop array
			JSONArray msg = (JSONArray) parcelObject.get("service");
			List<String> services=new ArrayList<String>();
			Iterator<String> iterator = msg.iterator();
			while (iterator.hasNext()) {
				//System.out.println(iterator.next());
				services.add(iterator.next());
			}
			doc.addField("service", services);
			server.add(doc);
			server.commit();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}