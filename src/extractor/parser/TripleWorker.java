package extractor.parser;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class TripleWorker {

	Document document;
	
	public TripleWorker(Document document){
		this.document = document;
	}
	
	public void printTriples(){
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples()) {
	    	System.out.println("Sentence No: " + sent.sentenceIndex() + "\n");
	        System.out.println(triple.confidence + "\t" +
	            triple.subjectLemmaGloss() + "\t" +
	            triple.relationLemmaGloss() + "\t" +
	            triple.objectLemmaGloss());
	      }
	    }
	}
	
	public Map<String, Long> getRelationFrequencies(){
		ArrayList<String> relations = new ArrayList<String>();
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples()) relations.add(triple.relationLemmaGloss());
		}
	    
	    Map<String, Long> counts = relations.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));	    
	    
	    return counts;
	}
	
	public Map<String, Long> getSubjectFrequencies(){
		ArrayList<String> subjects = new ArrayList<String>();
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples()) subjects.add(triple.subjectLemmaGloss());
		}
	    
	    Map<String, Long> counts = subjects.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));	    
	    
	    return counts;
	}
	
	public Map<String, Long> getObjectFrequencies(){
		ArrayList<String> objects = new ArrayList<String>();
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples()) objects.add(triple.objectLemmaGloss());
		}
	    
	    Map<String, Long> counts = objects.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));	    
	    
	    return counts;
	}
	
	public Map<String, Long> sortMapByValue(Map<String,Long> unsortedMap){
		// 1. Convert Map to List of Map
        List<Map.Entry<String, Long>> list =
                new LinkedList<Map.Entry<String, Long>>(unsortedMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
            public int compare(Map.Entry<String, Long> o1,
                               Map.Entry<String, Long> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        for (Map.Entry<String, Long> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());
        

        return sortedMap;
	}
	
	public void printRelationFrequencies(){
		Map<String,Long> unsortedRelations = getRelationFrequencies();
		Map<String,Long> sortedRelations = sortMapByValue(unsortedRelations);
		printMap(sortedRelations);
	}
	
	public void printEntitiesFrequencies(){
		
		Map<String,Long> unsortedSubjects = getSubjectFrequencies();
		Map<String,Long> sortedSubjects = sortMapByValue(unsortedSubjects);

		Map<String,Long> unsortedObjects = getObjectFrequencies();
		Map<String,Long> sortedObjects = sortMapByValue(unsortedObjects);
		
		System.out.println("============= Subject Frequencies ===============");
		printMap(sortedSubjects);
		System.out.println();
		System.out.println("============= Object Frequencies ================");
		printMap(sortedObjects);
		
		
	}
	
	public void printMap(Map<String,Long> sortedMap){
	    for(Map.Entry<String, Long> entry : sortedMap.entrySet())
	    	System.out.println( entry.getKey() + " " + entry.getValue());
	}
	
	public void printSubjects() {
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples())
	      	System.out.println(triple.subjectLemmaGloss());
		}	
	}

	public void printRelations() {
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples())
	      	System.out.println(triple.relationLemmaGloss());
		}	
	}
	
	public void printObjects() {
	    for (Sentence sent : document.sentences()) {
	      for (RelationTriple triple : sent.openieTriples())
	      	System.out.println(triple.objectLemmaGloss());
		}	
	}
	
	public void fileOutSubject(String fileLocation) {
		try {
			PrintWriter writer = new PrintWriter(fileLocation, "UTF-8");
			
		    for (Sentence sent : document.sentences()) {
			      for (RelationTriple triple : sent.openieTriples())
			    	  writer.println(triple.subjectLemmaGloss());
			}
		    
		    writer.close();
		    
		    System.out.println("The list of subjects has been generated in the following address:" + fileLocation);
		    
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}