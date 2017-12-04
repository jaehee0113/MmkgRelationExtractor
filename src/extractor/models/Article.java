package extractor.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

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
	private List<Sentence> sentences;
	private List<RelationTriple> triples;
	private List<String> subjects;
	private List<String> objects;
	private List<String> relations;
	private List<String> known_entities;
	private Date timestamp;
	
	public Article(String id, String title, String description){
		this.id = id;
		this.title = title;
		this.description = description;
		populateTriples();
	}
	
	public void populateTriples(){
		Document document =  new Document(this.getDescription());
		this.sentences = document.sentences();
		triples = new ArrayList<RelationTriple>();
		
	    for (Sentence sent : sentences) {
		   for (RelationTriple triple : sent.openieTriples()) 
			   triples.add(triple);
	    }		
	}
	
	/*
	 * Getters 
	 */
	
	public String getDocumentID(){
		return this.id;
	}
	
	public List<RelationTriple> getTriples(){
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
	    for (RelationTriple triple : triples) {
		    System.out.println(triple.toString());
	    }
	}
	
	public String toString(){
		return "Title: " + title + "\n" + "Description: " + description + "\n" + "Time: " + timestamp;
	}

}
