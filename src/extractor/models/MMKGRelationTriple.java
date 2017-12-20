package extractor.models;

import java.util.Date;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Sentence;

public class MMKGRelationTriple {
	
	RelationTriple triple;
	String subject_concept_type;
	String subject_concept;
	String object_concept_type;
	String object_concept;
	Date timestamp;
	String relation_frame;
	Sentence sentence;
	
	public MMKGRelationTriple(RelationTriple triple, Sentence sentence){
		this.triple = triple;
		this.sentence = sentence;
	}
	
	public RelationTriple getTriple() {
		return this.triple;
	}
	
	public Sentence getSentence(){
		return this.sentence;
	}
	
	public String getSentenceToString(){
		return this.sentence.toString();
	}
	
	public String getSubjectConcept(){
		return this.subject_concept;
	}
	
	public String getObjectConcept(){
		return this.object_concept;
	}
	
	public String getSubjectConceptType(){
		return this.subject_concept_type;
	}

	public String getObjectConceptType(){
		return this.object_concept_type;
	}
	
	public String getRelationFrame(){
		return this.relation_frame;
	}
	
	public Date getTimestamp(){
		return this.timestamp;
	}
	
	public void setTriple(RelationTriple triple){
		this.triple = triple;
	}
	
	public void setRelationFrame(String relation_frame){
		this.relation_frame = relation_frame;
	}
	
	public void setSubjectConcept(String subject_concept){
		this.subject_concept = subject_concept;
	}
	
	public void setObjectConcept(String object_concept){
		this.object_concept = object_concept;
	}
	
	public void setSubjectConceptType(String subject_concept_type){
		this.subject_concept_type = subject_concept_type;
	}

	public void setObjectConceptType(String object_concept_type){
		this.object_concept_type = object_concept_type;
	}
	
	public void setTimestamp(Date timestamp){
		this.timestamp = timestamp;
	}

}
