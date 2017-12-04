package extractor.main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONException;

import extractor.dbpedia.spotlight.client.DBpediaSpotlightClient;
import extractor.diff.DiffWorker;
import extractor.elastic.client.ElasticClient;
import extractor.elastic.controller.ElasticController;
import extractor.lib.FileProcessor;
import extractor.lib.Preprocessor;
import extractor.lib.TextProcessor;
import extractor.models.Article;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.ie.util.RelationTriple;


public class Application extends AppWorker{
	
	static Set<String> subjectEntities;
	static Set<String> dbpediaEntities;
	static HashMap<String, ArrayList<String>> subjectSimilarDBPEntities;
	
	public static void main(String[] args) throws UnknownHostException {
		
		Map<String, Article> bb_articles = getArticlesFromTopic("gay_marriage");
		
		Article article = bb_articles.get("article-uuid-865f80ac48b35e540cb770bcba74cf47c4715d8b");
		
		generateTripleArticleStat(article);
		
		//for(Article article : bb_articles){
		//	generateTripleArticleStat(article);
		//}
		
		System.out.println(extractConceptFromDBP("aggravated"));
		
		//System.out.println(ElasticController.getJSONTweetsFromIndex("beef_ban"));
		//System.out.println(ElasticController.getJSONArticlesFromIndex("beef_ban"));
		/*
		try {
			//System.out.println(ElasticController.getJSONDocumentsFromIndex("beef_ban").get("hits"));
			System.out.println(ElasticController.getJSONTweetsFromIndex("beef_ban"));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//ElasticClient.getInstance().close();
		
		//TextProcessor proc1 = new TextProcessor();
		//URL fileUrl = proc1.getURL();
		//String bb_art_str = proc1.toString(fileUrl);	
		
		//TextProcessor proc2 = new TextProcessor("beef_ban", "tweet");
		//URL bb_tw_url = proc2.getURL();
		//String bb_tw_str = proc2.toString(bb_tw_url);	
		
		//Documents
		//Document bb_art = new Document(bb_art_str);
		//Document bb_tw = new Document(bb_tw_str);
		
		//System.out.println(DiffWorker.computeWordSimilarity("indian_express_app_ie_online_media_services_pvt_ltd", "the_indian_express"));
		
		//AppWorker.printRelationFrequencies(bb_tw);
		//AppWorker.printEntitiesFrequencies(bb_tw);
		
		
		/*
		try {
			//subjectEntities = FileProcessor.storeSetFromFile(fileUrl.toString().replace("file:","").replace(".txt", "-subjects.txt"));
			//dbpediaEntities = FileProcessor.storeSetFromFile(fileUrl.toString().replace("file:","").replace(".txt", "-dbpedia.txt"));
			//subjectEntities = Preprocessor.setToLowercase(subjectEntities, true);
			//dbpediaEntities = Preprocessor.setToLowercase(dbpediaEntities, true);
			
			//DiffWorker.printSimilarEntitiesList(DiffWorker.getSimilarEntitiesList(subjectEntities, dbpediaEntities));
			
			//generateSimilarEntitiesReport(subjectEntities, dbpediaEntities);
			
			//DiffWorker.computeWordSimilarity("gay", "gaay");
			
			//FileProcessor.printUniqueEntities(subjectEntities);
			//System.out.println();
			//System.out.println();
			//FileProcessor.printUniqueEntities(dbpediaEntities);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//Application.printSentences(doc);
		//Application.generateCorefClusters(content);
		//Application.printTriples(doc);
		//Application.initDBSpotlight(fileUrl);
		//Application.printLemmas(bb_art, fileUrl);

	}
}
