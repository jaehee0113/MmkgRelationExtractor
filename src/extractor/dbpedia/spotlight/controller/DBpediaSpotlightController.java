package extractor.dbpedia.spotlight.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.dbpedia.spotlight.model.Text;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import extractor.dbpedia.spotlight.client.DBpediaSpotlightClient;
import extractor.dbpedia.spotlight.config.DBpediaSpotlightConfig;
import extractor.dbpedia.spotlight.lib.AnnotationClient;

public class DBpediaSpotlightController extends AnnotationClient{
	
	DBpediaSpotlightClient client;
	
	public DBpediaSpotlightController(DBpediaSpotlightClient client){
		this.client = client;
	}

	public Map<String, HashMap<String, String>> extractFromString(String text) throws AnnotationException {
	  	String response = getResponse(text);
		assert response != null;
		
		JSONArray entities = jsonifyString(response);
		
		Map<String, HashMap<String, String>> resources = getResult(entities);
		
		return resources;
	}

	@Override
	public List<DBpediaResource> extract(Text text) throws AnnotationException {

 		String response = getResponse(text.text());
		assert response != null;
		JSONArray entities = jsonifyString(response);
		LinkedList<DBpediaResource> resources = getDBpediaResources(entities);
		
		return resources;
	}
	
	
	public void printEntitiesToFile(URL fileUrl){
		
		File input = new File(fileUrl.toString().replace("file:", ""));
		File output = new File(fileUrl.toString().replace("file:","").replace(".txt", "-dbpedia.txt"));
		
		try {
			evaluate(input, output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	/*
	 *  Helper methods
	 */
	
	public Map<String, HashMap<String, String>> getResult(JSONArray entities){
		
		Map<String, HashMap<String, String>> resources = new HashMap<String, HashMap<String, String>>();
		
		for(int i = 0; i < entities.length(); i++) {
			try {
				JSONObject entity = entities.getJSONObject(i);
				HashMap<String,String> details = new HashMap<String, String>();
				details.put("URI", entity.getString("@URI"));
				details.put("support", entity.getString("@support"));
				details.put("similarity", entity.getString("@similarityScore"));
				details.put("surface", entity.getString("@surfaceForm"));
				details.put("types", entity.getString("@types"));
				resources.put(entity.getString("@surfaceForm"), details);
				
			} catch (JSONException e) {
              LOG.error("JSON exception "+e);
          }
		}
		
		return resources;
	}
	
	public LinkedList<DBpediaResource> getDBpediaResources(JSONArray entities){
		
		LinkedList<DBpediaResource> resources = new LinkedList<DBpediaResource>();
		for(int i = 0; i < entities.length(); i++) {
			try {
				JSONObject entity = entities.getJSONObject(i);
				resources.add(
						new DBpediaResource(entity.getString("@URI"),
								Integer.parseInt(entity.getString("@support"))));
			} catch (JSONException e) {
              LOG.error("JSON exception "+e);
          }
		}

		return resources;
		
	}
	
	
	public JSONArray jsonifyString(String response) throws AnnotationException{
		JSONObject resultJSON = null;
		JSONArray entities = null;

		try {
			resultJSON = new JSONObject(response);
			entities = resultJSON.getJSONArray("Resources");
			return entities;
		} catch (JSONException e) {
			throw new AnnotationException("Received invalid response from DBpedia Spotlight API.");
		}
	}
	
	
	public String getResponse(String text) throws AnnotationException{	
		LOG.info("Querying API.");
		String spotlightResponse;
		
		try {
			GetMethod getMethod = new GetMethod(DBpediaSpotlightConfig.API_URL + "/annotate/?" +
					"confidence=" + DBpediaSpotlightConfig.CONFIDENCE
					+ "&support=" + DBpediaSpotlightConfig.SUPPORT
					+ "&text=" + URLEncoder.encode(text, "utf-8"));
			
			getMethod.addRequestHeader(new Header("Accept", "application/json"));

			spotlightResponse = request(getMethod);
			
			return spotlightResponse;
			
		} catch (UnsupportedEncodingException e) {
			throw new AnnotationException("Could not encode text.", e);
		}
	}
	
}
