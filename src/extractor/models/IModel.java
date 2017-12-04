package extractor.models;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ie.util.RelationTriple;

public interface IModel {
	
	public List<MMKGRelationTriple> getTriples();
	
	public List<String> getKnownEntities();
	
	public List<String> getSubjects();
	
	public List<String> getObjects();
	
	public List<String> getRelations();

}
