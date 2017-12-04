package extractor.elastic.controller;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.json.JSONException;
import org.json.JSONObject;

import extractor.elastic.client.ElasticClient;
import extractor.elastic.config.ElasticConfig;
import extractor.elastic.lib.TopicDict;

import org.elasticsearch.client.transport.TransportClient;

public class ElasticController {
	
	private ElasticController(){
		
	}
	
	public static SearchResponse getGeneralResponse(){
		return ElasticClient.getInstance().prepareSearch().get();
	}
	
	public static JSONObject getJSONDocumentsFromIndex(String topic){
		
		Map<String,String> topicDict = TopicDict.getTopicDict();
		String index = String.format("mmkg-doc-%s", topicDict.get(topic));	
		try {
			return new JSONObject(ElasticClient.getInstance().prepareSearch(index).get().toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static SearchResponse getJSONTweetsFromIndex(String topic){
		
		Map<String,String> topicDict = TopicDict.getTopicDict();
		String index = String.format("mmkg-doc-%s", topicDict.get(topic));	
		
		return ElasticClient.getInstance().prepareSearch(index)
		.setSize(ElasticConfig.PAGE_SIZE)
		.setQuery(QueryBuilders.matchQuery("type", "tweet")).get();
		
	}	
	
	public static SearchResponse getJSONArticlesFromIndex(String topic){
		
		Map<String,String> topicDict = TopicDict.getTopicDict();
		String index = String.format("mmkg-doc-%s", topicDict.get(topic));	
		
		return ElasticClient.getInstance().prepareSearch(index)
		.setSize(ElasticConfig.PAGE_SIZE)
		.setQuery(QueryBuilders.matchQuery("type", "article")).get();
		
	}
	
}
