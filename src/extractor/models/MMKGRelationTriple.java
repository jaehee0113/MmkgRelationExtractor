package extractor.models;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Sentence;

public class MMKGRelationTriple {
	
	RelationTriple triple;
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
	

}
