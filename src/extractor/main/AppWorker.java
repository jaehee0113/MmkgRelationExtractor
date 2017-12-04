package extractor.main;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.dbpedia.spotlight.model.DBpediaResource;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Document;
import extractor.diff.DiffWorker;
import extractor.elastic.controller.ElasticController;
import extractor.models.Article;
import extractor.models.Docu;
import extractor.dbpedia.spotlight.client.DBpediaSpotlightClient;
import extractor.dbpedia.spotlight.controller.DBpediaSpotlightController;
import extractor.parser.CorefWorker;
import extractor.parser.SentenceWorker;
import extractor.parser.TripleWorker;

public class AppWorker {
	
	public static String extractConceptFromDBP(String text){
		DBpediaSpotlightClient client = DBpediaSpotlightClient.getInstance();
		DBpediaSpotlightController controller = new DBpediaSpotlightController(client);
		try {
			Map<String, HashMap<String, String>> result = controller.extractFromString(text);
			HashMap<String, String> the_result = result.get(text);
			
			if(the_result == null)return null;
			else return the_result.get("URI");
			
		} catch (AnnotationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void generateTripleArticleStat(Article article){
		
		List<RelationTriple> triples = article.getTriples();
		
		
		Map<String, String> subject_only = new HashMap<String, String>();
		Map<String, String> object_only = new HashMap<String, String>();
		Map<String, String> both = new HashMap<String, String>();
		
		
		//Converting to subjects and entities to concepts
		
		int matchingPair = 0;
		int subonlyPair = 0;
		int objonlyPair = 0;
		int nonmatchingPair = 0;
		
		for(RelationTriple triple: triples) {
			String subject = triple.subjectLemmaGloss();
			String object = triple.objectLemmaGloss();
			String relation = triple.relationLemmaGloss();
			
			boolean subjectAvailable = false;
			boolean objectAvailable = false;
			
			//Subject parsing			
			String subj_concept = extractConceptFromDBP(subject);
			String obj_concept = extractConceptFromDBP(object);
			
			if(subj_concept != null){
				//Matching with the known entities means that this entity is legit.
				//if(sp_art.getKnownEntities().contains(subj_concept)) {
					subjectAvailable = true;
				//}
			}
			
			if(obj_concept != null){
				//Matching with the known entities means that this entity is legit.
				//if(sp_art.getKnownEntities().contains(obj_concept)) {
					objectAvailable = true;
				//}
			}

			
			if(subjectAvailable && objectAvailable){
				++matchingPair;
				both.put(triple.asSentence().toString(), "(" + subject + "," + relation + "," + object + ")" + " Concepts: (" + subj_concept + "," + obj_concept + " )." );

			}else if(subjectAvailable && !objectAvailable){
				subject_only.put(triple.asSentence().toString(), "(" + subject + "," + relation + "," + object + ")" + " Concept: " + subj_concept);
				++subonlyPair;
			}else if(!subjectAvailable && objectAvailable){
				++objonlyPair;
				object_only.put(triple.asSentence().toString(), "(" + subject + "," + relation + "," + object + ")" + " Concept: " + obj_concept);
			}else {
				++nonmatchingPair;
			}

		}
		
		System.out.println("Article Title: " + article.getTitle());
		System.out.println("Total # of triples: " + triples.size());
		System.out.println("Total # of triples whose entities have concepts in DBpedia: " + matchingPair);
		System.out.println("Total # of triples whose subjects have concepts in DBpedia: " + subonlyPair);
		System.out.println("Total # of triples whose objects have concepts in DBpedia: " + objonlyPair);
		System.out.println("Total # of triples whose entities have no concepts in DBpedia: " + nonmatchingPair);
		System.out.println("Subject Only Sentences and Triples");
		for (Map.Entry<String, String> e : subject_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey() + " Triple: " + e.getValue());
		}
		
		System.out.println("Object Only Sentences and Triples");
		for (Map.Entry<String, String> e : object_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey() + " Triple: " + e.getValue());
		}
		
		System.out.println("Subject and Object Sentences and Triples");
		for (Map.Entry<String, String> e : both.entrySet()) {
			System.out.println("Sentence: " + e.getKey() + " Triple: " + e.getValue());
		}
		
		
	}
	
	public static Map<String, Article> getArticlesFromTopic(String topic){
		
		Map<String, Article> articles = new HashMap<String, Article>();
		
		SearchResponse response = ElasticController.getJSONArticlesFromIndex(topic);

		SearchHit[] results = response.getHits().getHits();
		
		for(SearchHit hit : results) {
			String article_id = hit.getId();
			String source = hit.getSourceAsString();
			try {
				JSONObject sourceJSON = new JSONObject(source);

				//When creating an article, it will automatically populate the triple based on the description
				Article article = new Article( article_id, (String) sourceJSON.get("title"), (String) sourceJSON.get("description"));
				
				
				
				//Populating known entities properties of an article
				ArrayList<String> known_entities = new ArrayList<String>();
				
				
				JSONArray entities = null;
				try {
					entities = sourceJSON.getJSONArray("entities");
				}catch(Exception e){
					
				}	
				
				if(entities != null) {
					for(int i = 0; i < entities.length(); i++) {
						JSONObject obj = entities.getJSONObject(i);
						String uri = obj.getString("uri");
						known_entities.add(uri);
					}		
				}
				
				//Populating a timestamp of an article
				String timestamp = (String) sourceJSON.get("timestamp");
				Date parsedDate = DatatypeConverter.parseDateTime(timestamp).getTime();
				article.setTimestamp(parsedDate);
				article.setKnownEntities(known_entities);

				//Finally add the article to the list
				articles.put(article_id, article);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return articles;
		
	}
	
	
	/*
	 * 
	 * Python-crawler based functions
	 * (when stored results from txt files in extractor.lib.files)
	 * 
	 */

	public static void printSentences(Document doc) {
		SentenceWorker sen1 = new SentenceWorker(doc);
		sen1.printSentences();
	}
	
	public static void generateCorefClusters(String content){
		String annotators = "tokenize, ssplit, pos, lemma, ner, parse, dcoref";
		CorefWorker corefWorker = new CorefWorker(content);
		Properties props = corefWorker.setProperties(annotators);
		corefWorker.printCorefClusters(props);
	}
	
	public static void printTriples(Document doc){
		TripleWorker sen1 = new TripleWorker(doc);
		sen1.printTriples();
	}
	
	public static void printRelationFrequencies(Document doc){
		TripleWorker sen1 = new TripleWorker(doc);
		sen1.printRelationFrequencies();
	}
	
	public static void printEntitiesFrequencies(Document doc){
		TripleWorker sen1 = new TripleWorker(doc);
		sen1.printEntitiesFrequencies();
	}
	
	public static void printLemmas(Document doc, URL fileUrl){
		TripleWorker sen1 = new TripleWorker(doc);
		sen1.fileOutSubject(fileUrl.toString().replace("file:","").replace(".txt", "-subjects.txt"));
	}
	
	public static void generateSimilarEntitiesReport(Set<String> entities1, Set<String> entities2){
		Set<String> entities = DiffWorker.getEntitiesWithSimilarEntities(DiffWorker.getSimilarEntitiesList(entities1, entities2));
		System.out.println("Total " + entities.size() + " entities have similar or same entities from DBpedia");
		System.out.println("That is about " + (float) entities.size() / 899 * 100 + "%" + " of total entities from the supplied dataset.");
		System.out.println(entities.toString());
	}
}
