package extractor.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import extractor.semafor.config.SemaforConfig;

/*
 * 
 * A model for an article (one of the types of documents from the ElasticSearch)
 * 
 * 
 */

public class Article extends Docu implements IModel{
	
	private String id;
	private String title;
	private String description;
	private boolean strict;
	private boolean bulk;
	private List<Sentence> sentences;
	private List<MMKGRelationTriple> triples;
	private List<MMKGRelationTriple> canonical_triples;
	private List<String> subjects;
	private List<String> objects;
	private List<String> relations;
	private List<String> known_entities;
	private Date timestamp;
	
	public Article(String id, String title, String description, boolean strict, boolean bulk){
		this.id = id;
		this.title = title;
		this.description = description;
		this.strict = strict;
		//If enabled, the program will generate triples for every article in the index
		//This slows down the mmkg extraction process. %so be careful%
		if(this.bulk) populateTriples();
	}
	
	public void populateTriplesFromFile() throws IOException{
		
		//Clear triples as pruned version of these will be used
		this.triples.clear();
		
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(SemaforConfig.INPUT_FILE_DIR + this.getDocumentID() + "-completed.txt"));
			String line = null;
			while((line = br.readLine()) != null) {
				Sentence sent = new Sentence(line);
				for (RelationTriple triple : sent.openieTriples()){
				   if(strict && triple.confidence == 1.0) {
					   List<CoreLabel> canonicalSubjects = triple.canonicalSubject;
					   List<CoreLabel> canonicalObjects = triple.canonicalObject;
					   List<CoreLabel> relations = triple.relation;
					   RelationTriple enhancedTriple = new RelationTriple(canonicalSubjects, relations, canonicalObjects, 1.0);
					   MMKGRelationTriple mmkg_triple = new MMKGRelationTriple(enhancedTriple, sent);
					   this.triples.add(mmkg_triple);				   
				   }else if(!strict) {
					   MMKGRelationTriple mmkg_triple = new MMKGRelationTriple(triple, sent);
					   this.triples.add(mmkg_triple);				   
				   }
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void populateTriples(){
		Document document =  new Document(this.getDescription());
		this.sentences = document.sentences();
		triples = new ArrayList<MMKGRelationTriple>();
		
	    for (Sentence sent : sentences) {
		   for (RelationTriple triple : sent.openieTriples()) {			
			   if(strict && triple.confidence == 1.0) {
				   List<CoreLabel> canonicalSubjects = triple.canonicalSubject;
				   List<CoreLabel> canonicalObjects = triple.canonicalObject;
				   List<CoreLabel> relations = triple.relation;
				   RelationTriple enhancedTriple = new RelationTriple(canonicalSubjects, relations, canonicalObjects, 1.0);
				   MMKGRelationTriple mmkg_triple = new MMKGRelationTriple(enhancedTriple, sent);
				   triples.add(mmkg_triple);				   
			   }else if(!strict) {
				   MMKGRelationTriple mmkg_triple = new MMKGRelationTriple(triple, sent);
				   triples.add(mmkg_triple);				   
			   }
		   }
	    }		
	}
	
	/*
	 * Getters 
	 */
	
	public String getDocumentID(){
		return this.id;
	}
	
	public List<MMKGRelationTriple> getTriples(){
		return this.triples;
	}
	
	public List<Sentence> getSentences(){
		return this.sentences;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public Date getTimestamp(){
		return this.timestamp;
	}
	
	public List<MMKGRelationTriple> getCanonicalTriples(){
		return this.canonical_triples;
	}
	
	@Override
	public List<String> getSubjects() {
		// TODO Auto-generated method stub
		return subjects;
	}

	@Override
	public List<String> getObjects() {
		// TODO Auto-generated method stub
		return objects;
	}

	@Override
	public List<String> getRelations() {
		// TODO Auto-generated method stub
		return relations;
	}

	@Override
	public List<String> getKnownEntities() {
		// TODO Auto-generated method stub
		return this.known_entities;
	}
	
	/*
	 * Setters
	 * 
	 */
	
	public void setCanonicalTriples(List<MMKGRelationTriple> canonical_triples){
		this.canonical_triples = canonical_triples;
	}
	
	public void setKnownEntities(List<String> known_entities){
		this.known_entities = known_entities;
	}
	
	public void setTimestamp(Date timestamp){
		this.timestamp = timestamp;
	}
	
	public void printsentences(){
		for(Sentence sent: sentences)
			System.out.println(sent.toString());
	}
	
	public void printTriples(){
	    for (MMKGRelationTriple triple : triples) {
		    System.out.println(triple.toString());
	    }
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString(){
		return "Title: " + title + "\n" + "Description: " + description + "\n" + "Time: " + timestamp;
	}

}
