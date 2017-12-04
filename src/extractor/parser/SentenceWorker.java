package extractor.parser;

import java.awt.List;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class SentenceWorker {
	
	Document paragraphs;
	
	public SentenceWorker(Document paragraphs) {
		this.paragraphs = paragraphs;
	}
	
	public void printSentences() {
	    for (Sentence sent : paragraphs.sentences()) {
	    	System.out.println("Sentence No: " + ( sent.sentenceIndex() + 1 ) + "\t");
	    	System.out.println(sent.toString() + "\n");
	    }		
	}
	
	public List getSentencesInString(){	
		List senList = new List();
	    for (Sentence sent : paragraphs.sentences()) senList.add(sent.toString());   
	    return senList;
	}
	
}
