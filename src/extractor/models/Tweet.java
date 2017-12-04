package extractor.models;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;

public class Tweet implements IModel{

	private List<MMKGRelationTriple> triples;
	private List<String> subjects;
	private List<String> objects;
	private List<String> relations;
	
	private List<String> known_entities;


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

}
