package extractor.semafor.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import extractor.semafor.client.SemaforClient;
import extractor.semafor.config.SemaforConfig;
import extractor.semafor.lib.AnnotationClient;

public class SemaforController extends AnnotationClient{
	
	SemaforClient client;
	
	public SemaforController(SemaforClient client){
		this.client = client;
	}
	
	public String getResponse(String sentence) throws AnnotationException{	
		LOG.info("Querying API.");
		String semaforResponse;
		
		try {
			GetMethod getMethod = new GetMethod(SemaforConfig.API_URL + "/parse/api/v1/parse?" +
					"sentence=" + URLEncoder.encode(sentence, "utf-8"));
			
			getMethod.addRequestHeader(new Header("Accept", "application/json"));

			semaforResponse = request(getMethod);
			
			return semaforResponse;
			
		} catch (UnsupportedEncodingException e) {
			throw new AnnotationException("Could not encode text.", e);
		}
	}
	
	public String extractFrames(String relation, String sentence) throws AnnotationException{
		
		String response = getResponse(sentence);
		assert response != null;
		JSONArray jsonResponse = jsonifyString(response);
		
		String relation_frame = getResult(relation, jsonResponse);
		
		
		return relation_frame;
		
	}
	
	
	/*
	 * Helper methods
	 * 
	 */
	
	public String getResult(String relation, JSONArray sentences){
		
		Map<String, String> relation_frame = new HashMap<String,String>();
		
		for(int i = 0; i < sentences.length(); i++){
			
			try {
				JSONObject sentence = sentences.getJSONObject(i);
				JSONArray frames = sentence.getJSONArray(("frames"));
				
				for(int j = 0; j < frames.length(); j++){
					JSONObject frame = frames.getJSONObject(j);
					String text = frame.getJSONObject("target").getString("text");
					if(relation.contains(text)) relation_frame.put(relation, frame.getJSONObject("target").getString("name"));
				}
				
			}catch(JSONException e){
				LOG.error("JSON exception " + e);
			}
			
		}
		
		return relation_frame.get(relation);
		
	}
	
	public JSONArray jsonifyString(String response) throws AnnotationException{
		JSONObject resultJSON = null;
		JSONArray sentences = null;

		try {
			resultJSON = new JSONObject(response);
			sentences = resultJSON.getJSONArray("sentences");
			return sentences;
		} catch (JSONException e) {
			throw new AnnotationException("Received invalid response from Semafor API.");
		}
	}

}
