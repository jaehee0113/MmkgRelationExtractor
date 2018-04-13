package extractor.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.dbpedia.spotlight.exceptions.AnnotationException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Document;
import extractor.diff.DiffWorker;
import extractor.elastic.controller.ElasticController;
import extractor.export.gexf.GexfGraph;
import extractor.lib.FileProcessor;
import extractor.lib.Preprocessor;
import extractor.models.Article;
import extractor.models.MMKGRelationTriple;
import extractor.dbpedia.spotlight.client.DBpediaLookupClient;
import extractor.dbpedia.spotlight.client.DBpediaSpotlightClient;
import extractor.dbpedia.spotlight.config.DBpediaSpotlightConfig;
import extractor.dbpedia.spotlight.controller.DBpediaSpotlightController;
import extractor.parser.CorefWorker;
import extractor.parser.SentenceWorker;
import extractor.parser.TripleWorker;
import extractor.semafor.client.SemaforClient;
import extractor.semafor.config.SemaforConfig;
import extractor.semafor.controller.SemaforController;

public class AppWorker {
	
    /**
     * Extracting multimedia knowledge graph as well as triples in many forms
     * @param article the article from ElasticSearch DB.
     */
	public static void process(Article article){
		
		//First populate the triples with raw data
		article.populateTriples();
		
		//Pruning the sentence generated
		preprocess(article);
		
		//Generates statistics about an article
		try {
			generateTripleArticleStat(article);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//generateTripleArticleStat(article2);
		postprocess(article);
	}
	
	
    /**
     * Condense graph by using the triples selected based on the ranking algorithm from each sentence.
     * @param articles the articles extracted from ElasticSearch DB.
     */
	public static void condense_graph(List<Article> articles, boolean allow_report){
		
		System.out.println("Started condensing graph ... ");
		
		int total_prev_triples = 0;
		int total_triples = 0;
		int total_sent = 0;
		
		for(Article article : articles){
			
			System.out.println("Retrieved an article...");
			
			List<MMKGRelationTriple> triples = article.getCanonicalTriples();
			
			if(triples != null){
				total_prev_triples += triples.size();
				
				Map<String, List<MMKGRelationTriple>> triples_sent = new HashMap<String, List<MMKGRelationTriple>>();
				
				for(MMKGRelationTriple triple : triples){
					
					String sent = triple.getSentenceToString();
					
					if(triples_sent.containsKey(sent)){
						List<MMKGRelationTriple> triple_list = triples_sent.get(sent);
						triple_list.add(triple);
						triples_sent.put(sent, triple_list);
					}else{
						List<MMKGRelationTriple> triple_list = new ArrayList<MMKGRelationTriple>();
						triple_list.add(triple);
						triples_sent.put(sent, triple_list);
					}				
					
				}
				
				//Triples for each article
				List<MMKGRelationTriple> best_triples = new ArrayList<MMKGRelationTriple>();
				
				for(Map.Entry<String, List<MMKGRelationTriple>> e : triples_sent.entrySet()) {				
					List<MMKGRelationTriple> sent_triples = e.getValue();
					//best triple per sent
					MMKGRelationTriple best_triple = get_best_triple(sent_triples, article, articles);
					best_triples.add(best_triple);
					
					if(allow_report){
						int order = 1;
						System.out.println();
						System.out.println();
						System.out.println("======================start=of=1=============================");
						System.out.println("The best triple for the sentence: '" + e.getKey() + "' is ");
						System.out.println("Subject Concept: " + best_triple.getSubjectConcept() + " Original: " + best_triple.getTriple().subjectGloss());
						System.out.println("Relation: " + best_triple.getRelationFrame() + " Original: " + best_triple.getTriple().relationGloss());
						System.out.println("Object: " + best_triple.getObjectConcept() + " Original: " + best_triple.getTriple().objectGloss());
						System.out.println("========================end=of=1=============================");
						System.out.println("======================start=of=2=============================");
						System.out.println("This sentence had the following triples as confidence level of 1.0");
						for(MMKGRelationTriple raw_triple : sent_triples){
							System.out.println("\t Triple " + order + ": " + raw_triple.getTriple().toString());
							order++;
						}
						System.out.println("=======================end=of=2=============================");
						System.out.println();
						System.out.println();
					}
				}
				
				//Replace with best_triples
				article.setCanonicalTriples(best_triples);
				
				total_triples += best_triples.size();
				total_sent += triples_sent.size();
			}
			
		}
		
		System.out.println("The total number of sentences in this document is " + total_sent + "The number of triples is now " + total_triples + ". Previously the graph used to render " + total_prev_triples + " triples.");
		
	}
	
	public static MMKGRelationTriple get_best_triple(List<MMKGRelationTriple> triples_in_sent, Article curr_article, List<Article> document){
		
		
		double curr_max = 0;
		MMKGRelationTriple best_triple = null;
		
		for(MMKGRelationTriple triple : triples_in_sent){
			
			//calculate tf
			double subject_tf = Preprocessor.get_tf(triple.getSubjectConcept(), curr_article);
			double object_tf = Preprocessor.get_tf(triple.getObjectConcept(), curr_article);
			double relation_tf = Preprocessor.get_tf(triple.getRelationFrame(), curr_article);
			
			//calculate idf
			double subject_idf = Preprocessor.get_idf(triple.getSubjectConcept(), document);
			double object_idf = Preprocessor.get_idf(triple.getObjectConcept(), document);
			double relation_idf = Preprocessor.get_idf(triple.getRelationFrame(), document);
			
			double subject_tfidf = Preprocessor.get_tf_idf(subject_tf, subject_idf);
			double object_tfidf = Preprocessor.get_tf_idf(object_tf, object_idf);
			double relation_tfidf = Preprocessor.get_tf_idf(relation_tf, relation_idf);
			
			//calculate harmonic mean and get rank
			double rank = 3 / ( 1/subject_tfidf + 1/object_tfidf + 1/relation_tfidf );
			
			if(rank >= curr_max) {
				curr_max = rank;
				best_triple = triple;
			}
			
		}		
		
		return best_triple;
		
	}
	
    /**
     * Deletes files used for processing and select triple for each sentence to reduce the number of nodes
     * @param article the article from ElasticSearch DB.
     */
	public static void postprocess(Article article){
		
		//We do not need these files anymore (just used for preprocessing)
		File file = new File(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + ".txt");
		File file1 = new File(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-complete.txt");
		File file2 = new File(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-pruned.txt");
		File file3 = new File(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-completed.txt");

		
		file.delete();
		file1.delete();
		file2.delete();
		file3.delete();
		
	}
	
	public static void preprocess(Article article){
		try {
			List<String> sentences = FileProcessor.getSentencesFromArticle(article);
			FileProcessor.writeFile(sentences, article.getDocumentID());
			
			//Remove empty lines
			BufferedReader br = new BufferedReader(new FileReader(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + ".txt"));
			PrintWriter outputFile = new PrintWriter(new FileWriter(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-pruned.txt"));
			
			String line = null;
			while((line = br.readLine()) != null) {
				if("".equals(line.trim())){
					continue;
				}
				outputFile.println(line);
				outputFile.flush();
			}
			
			br.close();
			outputFile.close();
			
			// . if the end of line does not end with it.
			PrintWriter outputFile2 = new PrintWriter(new FileWriter(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-complete.txt"));
			BufferedReader br2 = new BufferedReader(new FileReader(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-pruned.txt"));
			String line2 = null;
			while((line2 = br2.readLine()) != null) {
				
				if(line2.charAt(line2.length() - 1) != '.'){
					outputFile2.println(line2 + ".");
					outputFile2.flush();
				}else{
					outputFile2.println(line2);
					outputFile2.flush();
				}
			}
			br2.close();
			outputFile2.close();
			
			//Remove duplicate lines
			BufferedReader br3 = new BufferedReader(new FileReader(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-complete.txt"));
			Set<String> lines = new HashSet<String>(10000);
			String line5;
			while ((line5 = br3.readLine()) != null) {
				lines.add(line5);
			}
			
			br3.close();
			
			BufferedWriter outputFile3 = new BufferedWriter(new FileWriter(SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-completed.txt"));
			for (String unique : lines) {
			   outputFile3.write(unique);
			   outputFile3.newLine();
			}
			   
			outputFile3.close();
			
			article.populateTriplesFromFile();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static String extractConceptFromDBPLookup(String text){
		DBpediaLookupClient controller;
		try {
			controller = new DBpediaLookupClient(text);
			Map<String, String> result = controller.getResult();
			String the_result = result.get("URI");
			
			if(the_result == null)return null;
			else return the_result;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return null;
	}
	
	public static List<String> extractConceptFromDBP(String entity, String text){
		
		List<String> return_obj = new ArrayList<String>();
		
		DBpediaSpotlightClient client = DBpediaSpotlightClient.getInstance();
		DBpediaSpotlightController controller = new DBpediaSpotlightController(client);
		try {
			
			Map<String, HashMap<String, String>> result = controller.extractFromString(text);
			
			
			Map<String, HashMap<String, String>> ent_result = controller.extractFromString(entity);
			HashMap<String, String> the_ent_result = ent_result.get(entity);
			
			//if cannot be found from string do another matching just by putting in subject or object

			if(the_ent_result == null) {
				for (Map.Entry<String, HashMap<String, String>> e : result.entrySet()) {
					if(entity.contains(e.getKey())) {
						Map<String, String> sub_result = e.getValue();
						return_obj.add(sub_result.get("URI"));
						return_obj.add(sub_result.get("types"));
						return return_obj;
					}	
				}
			}else {
				
				return_obj.add(the_ent_result.get("URI"));
				return_obj.add(the_ent_result.get("types"));
				
				return return_obj;	
			} 
			
		} catch (AnnotationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//If still not getting any result, try DBpedia lookup
		//String final_result = extractConceptFromDBPLookup(entity);
		//if(final_result != null) {
		//	return final_result;
		//}
		
		return null;
	}
	
	public static Map<String, String> getFrames(Article article) throws InterruptedException, JSONException{
		Map<String, String> relation_frame = new HashMap<String,String>();
		Runtime rt = Runtime.getRuntime();
		
		try {
			
			Process pr = rt.exec( SemaforConfig.MALT_PARSER_SHELL_DIR + " " + SemaforConfig.INPUT_FILE_DIR + article.getDocumentID() + "-completed.txt" + " " + SemaforConfig.OUTPUT_DIR);
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			 
            String line=null;

            while((line=input.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            System.out.println("Exited with error code " + exitVal);
            
            if(exitVal == 1) {
            	
	            BufferedReader errinput = new BufferedReader(new InputStreamReader(
	            		pr.getErrorStream()));
	            String line2 =null;
	
	            while((line2 = errinput.readLine()) != null) {
	                System.out.println(line2);
	            }
            
            }
            
            if(exitVal == 0) {
    			String[] cmd2 = {
    					"/bin/sh",
    					"-c",
    					"cat /home/ubuntu/semafor/semafor/output/conll | nc 130.56.248.107 8888"
    			};
    			
    			
    			pr = rt.exec(cmd2);
                BufferedReader input2 = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                String line2 =null;
               
                while((line2 = input2.readLine()) != null) {
                	
                	JSONObject result = new JSONObject(line2);
                	JSONArray frames = result.getJSONArray("frames");
                	
    				for(int j = 0; j < frames.length(); j++){
    					JSONObject frame = frames.getJSONObject(j);
    					JSONArray spans = frame.getJSONObject("target").getJSONArray("spans");
    					JSONObject text_object = spans.getJSONObject(0);
    					String text = text_object.getString("text");
    					String name = frame.getJSONObject("target").getString("name");
    					//System.out.println("Text: " + text + " Frame: " + name);
    					relation_frame.put(text,name);
    				}
 
                }
                
            }
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return relation_frame;
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
	
	public static void extractCanonicalForm(Article article) throws InterruptedException, JSONException{
		
		Map<String,String> relation_frames = getFrames(article);
		
		List<MMKGRelationTriple> triples = article.getTriples();
		for(MMKGRelationTriple triple: triples) {
					
			//Getting parts of a triple
			String subject = triple.getTriple().subjectLemmaGloss();
			String object = triple.getTriple().objectLemmaGloss();
			String relation = triple.getTriple().relationGloss();
			
			List<String> subject_concept_details = extractConceptFromDBP(subject, triple.getSentenceToString());
			List<String> object_concept_details = extractConceptFromDBP(object, triple.getSentenceToString());
						
			if(subject_concept_details != null){
				String subject_concept = subject_concept_details.get(0);
				String subject_concept_types = subject_concept_details.get(1);	
				
				
				List<String> subject_concept_types_list = new ArrayList<String>(Arrays.asList(subject_concept_types.split(",")));
				String subject_concept_type = getConceptType(subject_concept_types_list);
				
				triple.setSubjectConcept(subject_concept);
				triple.setSubjectConceptType(subject_concept_type);
			}
			
			if(object_concept_details != null){
				String object_concept = object_concept_details.get(0);
				String object_concept_types = object_concept_details.get(1);
				
				List<String> object_concept_types_list = new ArrayList<String>(Arrays.asList(object_concept_types.split(",")));
				String object_concept_type = getConceptType(object_concept_types_list);
				
				triple.setObjectConcept(object_concept);
				triple.setObjectConceptType(object_concept_type);
			}
			
			//Finding frame for the relation
			for (Map.Entry<String, String> e : relation_frames.entrySet()) {
				if(relation.contains(e.getKey())){
					//System.out.println("Text: " + e.getKey() + " Frame: " + e.getValue());
					//Store in MMKGRelationTriple object
					triple.setRelationFrame(e.getValue());
				}
			}

		}
	}
	
	public static String getConceptType(List<String> types){
		
		List<String> supported_types = Arrays.asList(DBpediaSpotlightConfig.SUPPORTED_TYPES);
		
		for(String type: types){
			if(supported_types.contains(type)) {
				return Integer.toString(supported_types.indexOf(type));
			}
		}
		
		return "99";
		
	}
	
	
    /**
     * Generate stat about the article and returns the canonical form of a triple
     * @param article the article from ElasticSearch DB.
     */
	public static void generateTripleArticleStat(Article article) throws InterruptedException, JSONException{
		
		//First extract canonical form
		extractCanonicalForm(article);
		
		//Then get triples
		List<MMKGRelationTriple> triples = article.getTriples();
		
		Map<String, List<String>> subject_only = new HashMap<String, List<String>>();
		Map<String, List<String>> object_only = new HashMap<String, List<String>>();
		Map<String, List<String>> rel_frame_only = new HashMap<String, List<String>>();
		Map<String, List<String>> rel_frame_subject_only = new HashMap<String, List<String>>();
		Map<String, List<String>> rel_frame_object_only = new HashMap<String, List<String>>();
		Map<String, List<String>> both = new HashMap<String, List<String>>();
		Map<String, List<String>> none = new HashMap<String, List<String>>();
		Map<String, List<String>> all = new HashMap<String, List<String>>();
		
		//Triples in canonical form
		List<MMKGRelationTriple> canonicalTriples = new ArrayList<MMKGRelationTriple>();
		
		//Generate Report
		
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
			
			//Set the time for each triple
			triple.setTimestamp(article.getTimestamp());
			String subject = triple.getTriple().subjectLemmaGloss();
			String object = triple.getTriple().objectLemmaGloss();
			String relation = triple.getTriple().relationLemmaGloss();
			String relation_frame = triple.getRelationFrame();
			String subj_concept = triple.getSubjectConcept();
			String obj_concept = triple.getObjectConcept();
			
			boolean subjectAvailable = false;
			boolean objectAvailable = false;
			boolean relFrameAvailable = false;

			if(subj_concept != null) subjectAvailable = true;
			if(obj_concept != null) objectAvailable = true;
			
			String result = "(" + subject + "," + relation + "," + object + ")" + " Canonical form: (" + subj_concept + "," + obj_concept + ")";

			if(relation_frame != null) {
				relFrameAvailable = true;
				relFrameMatch++;
				result = "(" + subject + "," + relation + "," + object + ")" + " Canonical form: (" + subj_concept + "," + relation_frame + "," + obj_concept + ")";	
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
				canonicalTriples.add(triple);
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
		
		//Store canonical triples
		article.setCanonicalTriples(canonicalTriples);
		
		System.out.println("Article Title: " + article.getTitle());
		System.out.println();
		
		System.out.println("Article Content: " + article.getDescription());
		System.out.println();
		
		System.out.println("Total # of triples: " + triples.size());
		
		System.out.println("Total # of triples whose entities and relations have their canonical form: " + allPair);
		System.out.println("Total # of triples whose entities have concepts and relations have no frames: " + matchingPair);
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
		getCorrespondingTriples(all);
		
		System.out.println();
		System.out.println("Subject and Object Sentences and Triples");
		System.out.println();
		getCorrespondingTriples(both);
		
		System.out.println();
		System.out.println("Subject Only Sentences and Triples");
		System.out.println();
		getCorrespondingTriples(subject_only);
		
		System.out.println();
		System.out.println("Subject Only Sentences and Triples with Relation Frame");
		System.out.println();
		getCorrespondingTriples(rel_frame_subject_only);
		
		System.out.println();
		System.out.println("Object Only Sentences and Triples");
		System.out.println();
		getCorrespondingTriples(object_only);
		
		System.out.println();
		System.out.println("Object Only Sentences and Triples with Relation Frame");
		System.out.println();
		getCorrespondingTriples(rel_frame_object_only);
		
		System.out.println();
		System.out.println("No Entities Sentences and Triples with Relation Frame");
		System.out.println();
		getCorrespondingTriples(rel_frame_only);
		
		System.out.println();
		System.out.println("No Entities Sentences and Triples");
		System.out.println();
		getCorrespondingTriples(none);
		
		
		// Generate gexf file for graph rendering
		//GexfGraph graph = new GexfGraph();
		//graph.createGraphFromTriples(triples);
		//graph.exportGexfGraph("uncanonized");
		
		//GexfGraph graph1 = new GexfGraph();
		//graph1.createGraphFromTriples(canonicalTriples);
		//graph1.exportGexfGraph("canonized");
		
	}
	
	  private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }

	  public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
	  
	  public static String getLastBitFromUrl(final String url){
		    // return url.replaceFirst("[^?]*/(.*?)(?:\\?.*)","$1);" <-- incorrect
		    return url.replaceFirst(".*/([^/?]+).*", "$1");
		}
	
    /**
     * Find entities (via a relationship) that do not exist in DBpedia.
     *  
     * @param canonical_triples canonical triples it found from articles fetched.
     */
	public static void findUnknownTriplesFromDBPedia(List<MMKGRelationTriple> canonical_triples){
		
		//Go through the triple and generate a HashMap containing key as the entity name and value as another hashmap that
		//has key as a relation name (from DBpedia) and value as the an entity
		//We will only get the entity from subjects set
		
		ArrayList<String> subject_concepts = new ArrayList<String>();
		
		for(MMKGRelationTriple triple : canonical_triples){
			
			String entity_name = getLastBitFromUrl(triple.getSubjectConcept());
			
			if(!subject_concepts.contains(entity_name))
				subject_concepts.add(entity_name);
		}
		
		//< Subject, <Object, <Relation>>
		Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String, List<String>>>();
		
		for(String concept: subject_concepts){
			try {
				JSONObject json = readJsonFromUrl("http://dbpedia.org/data/" + concept + ".json");
				Iterator<?> keys = json.keys();
				
				Map<String, List<String>> sub_result = new HashMap<String, List<String>>();
				while(keys.hasNext()){
					String key = (String) keys.next();
					if(key.contains("http://dbpedia.org/resource/")){
						Iterator<?> relations = json.getJSONObject(key).keys();
						List<String> relation_list = new ArrayList<String>();
						while(relations.hasNext()){
							String relation = (String) relations.next();
							relation_list.add(getLastBitFromUrl(relation));
							
						}
						sub_result.put(getLastBitFromUrl(key), relation_list);
					}
				}
				
				result.put(concept, sub_result);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Checking all the triples from KB (i.e. DBpedia)
		/*
		for (Map.Entry<String, Map<String, List<String>>> e : result.entrySet()) {
			System.out.println("============================");
			System.out.println("Subject: " + e.getKey());
			
			Map<String, List<String>> obj_rels = e.getValue();
			
			for (Map.Entry<String, List<String>> f : obj_rels.entrySet()) {
				
				System.out.println("Object: " + f.getKey());
				
				int count = 0;
				
				for(String relation : f.getValue()) {
					++count;
					System.out.println("Relation #" + count + ": " + relation);
				}
				
			}
			System.out.println("============================");
			
		}
		*/
		//Now get the current triples and compare
		for(MMKGRelationTriple triple : canonical_triples){
			
			String subj_name = getLastBitFromUrl(triple.getSubjectConcept());
			String obj_name = getLastBitFromUrl(triple.getObjectConcept());
			
			//Get object and relations pair of the subject
			Map<String, List<String>> obj_rels = result.get(subj_name);
			
			//Get list of relations if obj found from DB
			if(obj_rels.get(obj_name) != null){
				//FOUND
				System.out.println("Object found: " + obj_name);
				//List<String> relations = obj_rels.get(obj_name);
				//
				//for(String rel: relations){
				//	System.out.println(rel);
				//}
			}else{
				//NOT FOUND
				System.out.println("Object not found: " + obj_name);
			}
			
		}
		
	}
	
	public static List<MMKGRelationTriple> generateGraphFromArticles(List<Article> articles){
		
		List<MMKGRelationTriple> merged_triples = new ArrayList<MMKGRelationTriple>();
		List<MMKGRelationTriple> merged_canonical_triples = new ArrayList<MMKGRelationTriple>();
		
		for(Article article : articles) {
			if(article.getTriples() != null) merged_triples.addAll(article.getTriples());
			if(article.getCanonicalTriples() != null) merged_canonical_triples.addAll(article.getCanonicalTriples());
		}
		
		GexfGraph graph = new GexfGraph();
		graph.createGraphFromTriples(merged_triples);
		graph.exportGexfGraph("uncanonized");
		
		GexfGraph graph2 = new GexfGraph();
		graph2.createGraphFromTriples(merged_canonical_triples);
		graph2.exportGexfGraph("canonized");
		
		
		return merged_canonical_triples;
		
	}
	
	public static void getCorrespondingTriples(Map<String, List<String>> sentTriples){
		for (Map.Entry<String, List<String>> e : sentTriples.entrySet()) {
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
	
	/**
	 * @param topic Elasticsearch topic
	 */
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
				
				Article article = new Article( article_id, (String) sourceJSON.get("title"), description, true, false);
				
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
	
	/**
	 * @param topic Elasticsearch topic
	 * @param start_date searches articles from this start date
	 * @param end_date searches articles up to this end date
	 */
	public static Map<String, Article> getArticlesFromTopic(String topic, String start_date, String end_date){
		
		Map<String, Article> articles = new HashMap<String, Article>();
		
		SearchResponse response = ElasticController.getJSONArticlesFromIndex(topic, start_date, end_date);

		SearchHit[] results = response.getHits().getHits();
		
		for(SearchHit hit : results) {
			String article_id = hit.getId();
			String source = hit.getSourceAsString();
			try {
				JSONObject sourceJSON = new JSONObject(source);
				
				String description = (String) sourceJSON.get("description");
				
				Article article = new Article( article_id, (String) sourceJSON.get("title"), description, true, false);
				
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
