package extractor.models;

import java.util.Date;
import java.util.List;

import edu.stanford.nlp.simple.Sentence;

public interface IModel {
	
	public List<MMKGRelationTriple> getTriples();
	
	public List<String> getKnownEntities();
	
	public List<String> getSubjects();
	
	public List<String> getObjects();
	
	public List<String> getRelations();
	
	public String getTitle();
	
	public String getDescription();
	
	public Date getTimestamp();
	
	public List<Sentence> getSentences();
	
	public String getDocumentID();
}
