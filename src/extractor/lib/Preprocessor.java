package extractor.lib;

import java.util.LinkedList;
import java.util.Set;

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

}
