package extractor.main;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;

import extractor.models.Article;
import extractor.nre.config.NREConfig;
import extractor.nre.controller.NREController;

public class Application extends AppWorker{
	
	static Set<String> subjectEntities;
	static Set<String> dbpediaEntities;
	static HashMap<String, ArrayList<String>> subjectSimilarDBPEntities;
	
	public static void main(String[] args) throws UnknownHostException, InterruptedException, JSONException {
		
		//Parameters that users pass in 
		String topic = "beef_ban";
		String start_date = "2017-07-01";
		String end_date = "2017-07-01";
		String purpose = "nre";
		
		long startTime = System.currentTimeMillis();
		
		List<Article> graphized_articles = new ArrayList<Article>();
		Map<String, Article> articles = new HashMap<String, Article>();
		
		//Map<String, Article> articles = getArticlesFromTopic("beef_ban");
		if(start_date != null)
			articles = getArticlesFromTopic(topic, start_date, end_date);
		else
			articles = getArticlesFromTopic(topic);
			
		
		// Only use this if you want specific articles
		
		/*
		String[] chosen_articles = {
			"mmkg-article-2eda01a81adeb6531351d0002b6766dda34b9366",
			"mmkg-article-0cc0255113819386cc8d22a3f58b21b7d0c7d84e",
			"article-uuid-ed9a0052f55f59fb264a07ef7e6389aaa0e84eec",
			"article-uuid-04126c5d28866bfef9d46481a33b9ca6f8d7616f"
		};
		
		for(String article_id : chosen_articles){
			Article article = articles.get(article_id);
			process(article);
			graphized_articles.add(article);
		}
		*/
		
		/*
		 *  File output for neural relation extraction (NRE) - data preparation
		 */
		if(purpose == "nre"){
			
			NREController.createFile(topic + "_" + start_date + "_" + end_date);
			
			//Populate triples (subjects and objects per sentence)
			for (Map.Entry<String, Article> e : articles.entrySet()) {
				Article curr_article = e.getValue();
				curr_article.populateTriples();
				NREController.writeLine(curr_article,topic + "_" + start_date + "_" + end_date);
			}
			
		}
		
		/*
		 *  OpenIE extraction part
		 */
		if(purpose == "openie") {
			for (Map.Entry<String, Article> e : articles.entrySet()) {
				Article curr_article = e.getValue();
				process(curr_article);
				graphized_articles.add(curr_article);
			}
			
			generateGraphFromArticles(graphized_articles);
			
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("Time taken to generate both canonical and non-canonical graph from " + articles.size() + " articles:" + estimatedTime);
		}
	}
}
