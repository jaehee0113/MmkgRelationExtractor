package extractor.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.text.similarity.LevenshteinDistance;
		
public class DiffWorker {
	//sim threshold and whether contains at least one word (which can be separated by _ (underscore))
	static final int SIM_THRESHOLD = 0;
	//If there is a substring then apply penalty (as 0 or below is considered good so the lowest score.
	static final int PENALTY_POINT = 10;
	
	
	// Returns the list of entities that have similar entities
	public static Set<String> getEntitiesWithSimilarEntities(HashMap<String, ArrayList<String>> result){
		
		Set<String> entities = new HashSet<>();
		Set<String> keys = result.keySet();
		
		Iterator<String> keyIterator = keys.iterator();
		
		while(keyIterator.hasNext()) {
			String currKey = keyIterator.next();
			if(!result.get(currKey).isEmpty())
				entities.add(currKey);
		}
		
		return entities;
		
	}
	
	// Prints out keys (entities extracted from Elasticsearch) and similar entities from DBPedia
	public static void printSimilarEntitiesList(HashMap<String, ArrayList<String>> result) {		
		
		Set<String> keys = result.keySet();
		
		Iterator<String> keyIterator = keys.iterator();
		
		while(keyIterator.hasNext()) {
			String currKey = keyIterator.next();
			System.out.println("Similar entities from DBpedia of: " + currKey);
			System.out.println(result.get(currKey));
			System.out.println();
		}
		
	}
	
	//Uses the function below to find out the most similar entries from DBpedia entity!
	public static HashMap<String, ArrayList<String>> getSimilarEntitiesList(Set<String> input1, Set<String> input2){
		ArrayList<String> ip1 = new ArrayList<String>(input1);
		ArrayList<String> ip2 = new ArrayList<String>(input2);

		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();

		for(String elem1 : ip1) {
			ArrayList<String> elem2List = new ArrayList<String>();
			
			for(String elem2 : ip2) {
				if(computeWordSimilarity(elem1, elem2) <= SIM_THRESHOLD) elem2List.add(elem2);
			}	
			
			result.put(elem1, elem2List);
		}
		
		return result;
	}
	
	//Uses Levenshtein Distance
	public static int computeWordSimilarity(String input1, String input2) {
		LevenshteinDistance distanceChecker = new LevenshteinDistance();
		int metric = distanceChecker.apply(input1, input2);
	
		String[] ip1_parts = input1.split("_");
		if(ip1_parts.length > 1) {
			for(String elem: ip1_parts) {
				if(input2.indexOf(elem) >= 0)
					metric -= PENALTY_POINT;
			}
		}
		
		return metric;
	}

}
