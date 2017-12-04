package extractor.parser;

import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CorefWorker {
	
	String content;
	Annotation document;
	
	public CorefWorker(String content) {
		this.content = content;
		document = new Annotation(content);
	}
	
	public Properties setProperties(String annotators){
		Properties props = new Properties();
	    props.setProperty("annotators", annotators);
	    
	    return props;
	}
	
	public void printCorefClusters(Properties props) {
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    pipeline.annotate(document);
	    System.out.println("---");
	    System.out.println("coref chains");
	    
	    Map<Integer, CorefChain> graph = document.get(CorefChainAnnotation.class);
	    
	    for(Map.Entry<Integer, CorefChain> entry : graph.entrySet()) {
	    	
	    	System.out.println("ClusterId: " + entry.getKey());
	    	System.out.println("CHAIN : " + entry.getValue());
	    	CorefChain c = entry.getValue();
	    	CorefMention representativeMention = c.getRepresentativeMention();
	    	System.out.println("\t" + "Representing entity: " + representativeMention);
	    	System.out.println("\n");
	    }
		
	}
	
}
