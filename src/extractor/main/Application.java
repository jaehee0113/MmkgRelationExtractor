package extractor.main;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import extractor.models.Article;

public class Application extends AppWorker{
	
	static Set<String> subjectEntities;
	static Set<String> dbpediaEntities;
	static HashMap<String, ArrayList<String>> subjectSimilarDBPEntities;
	
	public static void main(String[] args) throws UnknownHostException, InterruptedException, JSONException {
		
		Map<String, Article> bb_articles = getArticlesFromTopic("beef_ban");
		
		//Gets specific article from the topic using its id
		Article article = bb_articles.get("mmkg-article-2eda01a81adeb6531351d0002b6766dda34b9366");
		process(article);

	}
}
