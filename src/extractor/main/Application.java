package extractor.main;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import extractor.lib.FileProcessor;
import extractor.models.Article;

public class Application extends AppWorker{
	
	static Set<String> subjectEntities;
	static Set<String> dbpediaEntities;
	static HashMap<String, ArrayList<String>> subjectSimilarDBPEntities;
	
	public static void main(String[] args) throws UnknownHostException, InterruptedException, JSONException {
		
		Map<String, Article> bb_articles = getArticlesFromTopic("gay_marriage");
		
		//Gets specific article from the topic using its id
		Article article = bb_articles.get("article-uuid-865f80ac48b35e540cb770bcba74cf47c4715d8b");
		
		//Some preprocessing (this will soon be generalized)
		String description = article.getDescription();
		if(description.contains("[email protected]")) {
			String replaced_description = description.replace("[email protected]", "[email protected].");
			article.setDescription(replaced_description);
		}
		article.populateTriples();
		
		//Generates the file of sentence list
		List<String> sentences = FileProcessor.getSentencesFromArticle(article);
		FileProcessor.writeFile(sentences, article.getDocumentID());
				
		//Generates statistics about an article
		generateTripleArticleStat(article);

	}
}
