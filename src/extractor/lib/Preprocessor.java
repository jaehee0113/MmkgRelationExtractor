package extractor.lib;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

import extractor.models.Article;
import extractor.models.MMKGRelationTriple;

public class Preprocessor {
	
	public static Set<String> setToLowercase(Set<String> elems, boolean removeSpace){
	    //Convert to list
	    LinkedList<String> strsList = new LinkedList<String>();
	    strsList.addAll(elems);
		
	    //Do modification
	    for (int i = 0; i < strsList.size(); i++) {
	        String str = strsList.get(i);
	        if(!removeSpace)
	        	strsList.set(i, str.toLowerCase());
	        else
	        	strsList.set(i, str.replaceAll(" ", "_").toLowerCase());
	    }
	    
	    //Convert back to set
	    elems.clear();
	    elems.addAll(strsList);
	    
	    return elems;
	}
    /**
     * Returns term frequency of a term in an article.
     * @param term (canonical)
     * @param article canonical_form
     * @return
     */
	public static double get_tf(String term, Article article){
		
		List<MMKGRelationTriple> triples =  article.getCanonicalTriples();
		
		int freq = 0;
		
		if(triples != null) {
			for(MMKGRelationTriple triple : triples){
				List<String> possibilities = new ArrayList<String>();
				possibilities.add(triple.getSubjectConcept());
				possibilities.add(triple.getObjectConcept());
				possibilities.add(triple.getRelationFrame());
				
				if(possibilities.indexOf(term) != -1) freq++;	
			}	
		}
		
		return freq;
		
	}
	
    /**
     * Returns term frequency of a term in an article.
     * @param term canonical version of a term
     * @param document list of articles (document)
     * @return
     */
	public static double get_idf(String term, List<Article> document){
		double document_size = document.size();
		
		double num_art_freq = 1;
		
		for(Article art : document){
			double term_freq = get_tf(term, art);
			if(term_freq != 0) num_art_freq++;
		}
		
		double arg = document_size / num_art_freq;
		
		return Math.log(arg);
	}

    /**
     * Returns TF-IDF of a term.
     * @param tf_score TF Score of a term
     * @param idf_score IDF Score of a term
     * @return
     */
	public static double get_tf_idf(double tf_score, double idf_score){
		
		return tf_score * idf_score;
	}
	
    /**
     * Returns the best candidate from possible triples of a sentence using the rank calculated by harmonic mean of TF-IDF score of each component of a triple (i.e. subject).
     * @param tf_score TF Score of a term
     * @param idf_score IDF Score of a term
     * @return
     */	
	public static MMKGRelationTriple get_best_candidate_triple(List<MMKGRelationTriple> candidate_triples){
		
		return null;
	}
	
}
