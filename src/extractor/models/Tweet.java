package extractor.models;

import java.util.Date;
import java.util.List;

import edu.stanford.nlp.simple.Sentence;

public class Tweet implements IModel{
	
	private String id;
	private List<MMKGRelationTriple> triples;	
	private List<Sentence> sentences;
	private List<String> subjects;
	private List<String> objects;
	private List<String> relations;
	private List<String> known_entities;
	private Date timestamp;

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

	@Override
	public List<MMKGRelationTriple> getTriples() {
		// TODO Auto-generated method stub
		return this.triples;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getTimestamp() {
		// TODO Auto-generated method stub
		return this.timestamp;
	}

	@Override
	public List<Sentence> getSentences() {
		// TODO Auto-generated method stub
		return this.sentences;
	}

	@Override
	public String getDocumentID() {
		// TODO Auto-generated method stub
		return this.id;
	}

}
