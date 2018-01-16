package extractor.elastic.controller;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONException;
import org.json.JSONObject;

import extractor.elastic.client.ElasticClient;
import extractor.elastic.config.ElasticConfig;
import extractor.elastic.lib.TopicDict;

import org.elasticsearch.common.unit.TimeValue;

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
		.setScroll(new TimeValue(60000))
		.setSize(ElasticConfig.PAGE_SIZE)
		.setQuery(QueryBuilders.matchQuery("type", "tweet")).get();
		
	}	
	/**
	 * Returns hits from the specific topic
	 * 
	 * @param topic Elasticsearch topic
	 * @param type article or tweet
	 */
	public static SearchResponse getJSONHitsFromIndex(String topic, String type){
		Map<String,String> topicDict = TopicDict.getTopicDict();
		String index = String.format("mmkg-doc-%s", topicDict.get(topic));	
		
		return ElasticClient.getInstance().prepareSearch(index)
		.setScroll(new TimeValue(60000))
		.setSize(ElasticConfig.PAGE_SIZE)
		.setQuery(QueryBuilders.matchQuery("type", type)).get();
	}
	
	/**
	 * Returns hits from the specific topic
	 * 
	 * @param topic Elasticsearch topic
	 * @param type article or tweet
	 * @param start_date start date
	 * @param end_date end date
	 */
	public static SearchResponse getJSONHitsFromIndex(String topic, String type, String start_date, String end_date){
		Map<String,String> topicDict = TopicDict.getTopicDict();
		String index = String.format("mmkg-doc-%s", topicDict.get(topic));	
		
		BoolQueryBuilder query = QueryBuilders.boolQuery()
			.filter(QueryBuilders.matchQuery("type", type))
			.filter(QueryBuilders.rangeQuery("timestamp").from(start_date).to(end_date));
		
		return ElasticClient.getInstance().prepareSearch(index)
		.setScroll(new TimeValue(60000))
		.setSize(ElasticConfig.PAGE_SIZE)
		.setQuery(query)
		.get();
	}
	
}
