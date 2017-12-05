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
import extractor.models.MMKGRelationTriple;
import extractor.dbpedia.spotlight.client.DBpediaSpotlightClient;
import extractor.dbpedia.spotlight.controller.DBpediaSpotlightController;
import extractor.parser.CorefWorker;
import extractor.parser.SentenceWorker;
import extractor.parser.TripleWorker;
import extractor.semafor.client.SemaforClient;
import extractor.semafor.controller.SemaforController;

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
	
	public static String extractRelationFrameFromSemafor(String relation, String text){
		SemaforClient client = SemaforClient.getInstance();
		SemaforController controller = new SemaforController(client);
		
		try {
			String frame = controller.extractFrames(relation, text);			
			return frame;
		} catch (AnnotationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static void generateTripleArticleStat(Article article){
		
		List<MMKGRelationTriple> triples = article.getTriples();
		
		Map<String, List<String>> subject_only = new HashMap<String, List<String>>();
		Map<String, List<String>> object_only = new HashMap<String, List<String>>();
		Map<String, List<String>> rel_frame_only = new HashMap<String, List<String>>();
		Map<String, List<String>> rel_frame_subject_only = new HashMap<String, List<String>>();
		Map<String, List<String>> rel_frame_object_only = new HashMap<String, List<String>>();
		Map<String, List<String>> both = new HashMap<String, List<String>>();
		Map<String, List<String>> none = new HashMap<String, List<String>>();
		Map<String, List<String>> all = new HashMap<String, List<String>>();
		
		
		//Converting to subjects and entities to concepts
		
		int matchingPair = 0;
		int subonlyPair = 0;
		int objonlyPair = 0;
		int objonlyWithFramePair = 0;
		int subonlyWithFramePair = 0;
		int nonmatchingPair = 0;
		int relFrameMatch = 0;
		int allPair = 0;
		int onlyRelFrame = 0;
		
		for(MMKGRelationTriple triple: triples) {
			String subject = triple.getTriple().subjectLemmaGloss();
			String object = triple.getTriple().objectLemmaGloss();
			String relation = triple.getTriple().relationLemmaGloss();
			String relation_ori = triple.getTriple().relationGloss();	
			String relation_frame = extractRelationFrameFromSemafor(relation_ori, triple.getSentenceToString());
			
			
			boolean subjectAvailable = false;
			boolean objectAvailable = false;
			boolean relFrameAvailable = false;
			
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
			
			String result = "(" + subject + "," + relation + "," + object + ")" + " Concepts: (" + subj_concept + "," + obj_concept + ")";

			if(relation_frame != null) {
				relFrameAvailable = true;
				result = "(" + subject + "," + relation_frame + "," + object + ")" + " Concepts: (" + subj_concept + "," + obj_concept + ")";	
			}
			
			if(relFrameAvailable) {
				relFrameMatch++;
			}
			
			if(!relFrameAvailable && subjectAvailable && objectAvailable){
				++matchingPair;
				
				if(both.containsKey(triple.getSentenceToString())){
					List<String> curr_list = both.get(triple.getSentenceToString());
					curr_list.add(result);
					both.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					both.put(triple.getSentenceToString(), new_list);
				}
				
			}else if(!relFrameAvailable && subjectAvailable && !objectAvailable){
				
				++subonlyPair;
				if(subject_only.containsKey(triple.getSentenceToString())){
					List<String> curr_list = subject_only.get(triple.getSentenceToString());
					curr_list.add(result);
					subject_only.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					subject_only.put(triple.getSentenceToString(), new_list);
				}
				
			}else if(!relFrameAvailable && !subjectAvailable && objectAvailable){
				++objonlyPair;
				if(object_only.containsKey(triple.getSentenceToString())){
					List<String> curr_list = object_only.get(triple.getSentenceToString());
					curr_list.add(result);
					object_only.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					object_only.put(triple.getSentenceToString(), new_list);
				}
			}else if(!relFrameAvailable && !subjectAvailable && !objectAvailable) {
				++nonmatchingPair;
				if(none.containsKey(triple.getSentenceToString())){
					List<String> curr_list = none.get(triple.getSentenceToString());
					curr_list.add(result);
					none.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					none.put(triple.getSentenceToString(), new_list);
				}
				
			}else if(relFrameAvailable && !subjectAvailable && objectAvailable){
				++subonlyWithFramePair;
				if(rel_frame_subject_only.containsKey(triple.getSentenceToString())){
					List<String> curr_list = rel_frame_subject_only.get(triple.getSentenceToString());
					curr_list.add(result);
					rel_frame_subject_only.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					rel_frame_subject_only.put(triple.getSentenceToString(), new_list);
				}
			}else if(relFrameAvailable && subjectAvailable && !objectAvailable) {
				++objonlyWithFramePair;
				if(rel_frame_object_only.containsKey(triple.getSentenceToString())){
					List<String> curr_list = rel_frame_object_only.get(triple.getSentenceToString());
					curr_list.add(result);
					rel_frame_object_only.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					rel_frame_object_only.put(triple.getSentenceToString(), new_list);
				}
			}else if(subjectAvailable && objectAvailable && relFrameAvailable){
				allPair++;
				if(all.containsKey(triple.getSentenceToString())){
					List<String> curr_list = all.get(triple.getSentenceToString());
					curr_list.add(result);
					all.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					all.put(triple.getSentenceToString(), new_list);
				}
			}else if(!subjectAvailable && !objectAvailable && relFrameAvailable) {
				onlyRelFrame++;
				if(rel_frame_only.containsKey(triple.getSentenceToString())){
					List<String> curr_list = rel_frame_only.get(triple.getSentenceToString());
					curr_list.add(result);
					rel_frame_only.put(triple.getSentenceToString(), curr_list);
				}else {
					List<String> new_list = new ArrayList<String>();
					new_list.add(result);
					rel_frame_only.put(triple.getSentenceToString(), new_list);
				}
			}
			

		}
		
		System.out.println("Article Title: " + article.getTitle());
		System.out.println();
		
		System.out.println("Article Content: " + article.getDescription());
		System.out.println();
		
		System.out.println("Total # of triples: " + triples.size());
		
		System.out.println("Total # of triples whose entities and relations have their canonical form: " + allPair);
		System.out.println("Total # of triples whose entities have concepts in DBpedia: " + matchingPair);
		System.out.println("Total # of triples whose subjects have concepts in DBpedia: " + subonlyPair);
		System.out.println("Total # of triples whose subjects and relations have their canonical form: " + subonlyWithFramePair);
		System.out.println("Total # of triples whose objects have concepts in DBpedia: " + objonlyPair);
		System.out.println("Total # of triples whose objects and relations have their canonical form: " + objonlyWithFramePair);
		System.out.println("Total # of triples whose entities have no concepts in DBpedia: " + nonmatchingPair);
		System.out.println("Total # of triples whose relations have corresponding frame in FrameNet: " + relFrameMatch);
		System.out.println("Total # of triples whose relations have frame and entities have no concepts: " + onlyRelFrame);
		
		System.out.println();
		System.out.println("Entities and Relation Frame in Sentences and Triples");
		System.out.println();
		for (Map.Entry<String, List<String>> e : all.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		
		System.out.println();
		System.out.println("Subject and Object Sentences and Triples");
		System.out.println();
		for (Map.Entry<String, List<String>> e : both.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		
		System.out.println();
		System.out.println("Subject Only Sentences and Triples");
		System.out.println();
		for (Map.Entry<String, List<String>> e : subject_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Subject Only Sentences and Triples with Relation Frame");
		System.out.println();
		for (Map.Entry<String, List<String>> e : rel_frame_subject_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Object Only Sentences and Triples");
		System.out.println();
		for (Map.Entry<String, List<String>> e : object_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("Object Only Sentences and Triples with Relation Frame");
		System.out.println();
		for (Map.Entry<String, List<String>> e : rel_frame_object_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("No Entities Sentences and Triples with Relation Frame");
		System.out.println();
		for (Map.Entry<String, List<String>> e : rel_frame_only.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		System.out.println();
		System.out.println("No Entities Sentences and Triples");
		System.out.println();
		for (Map.Entry<String, List<String>> e : none.entrySet()) {
			System.out.println("Sentence: " + e.getKey());
			List<String> tripleList = e.getValue();
			int idx = 1;
			for(String result: tripleList) {
				System.out.println("\t Corresponding triple " + idx + ": " + result);
				idx++;
			}
			System.out.println();
		}
		
		
		
	}
	
	public static String getSentence(List<CoreLabel> sentTokens){
		List<String> words = new ArrayList<String>();
		for(CoreLabel each : sentTokens) words.add(each.word());
		return String.join(" ", words);
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
				
				String description = (String) sourceJSON.get("description");
				
				Article article = new Article( article_id, (String) sourceJSON.get("title"), description, true);
				
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
