package extractor.lib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class FileProcessor {
	
    protected static String readFileAsString(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
        f.read(buffer);
        return new String(buffer);
    }
    
    public static void printUniqueEntities(Set<String> entities) {
		Iterator<String> entitiesIterator = entities.iterator();
		
		while(entitiesIterator.hasNext()){
			System.out.println(entitiesIterator.next());
		}
    }
   
	
	public static Set<String> storeSetFromFile(String fileUrl) throws IOException{
		Set<String> entities = new HashSet<>();
		
		File inputFile = new File(fileUrl);
		String text = readFileAsString(inputFile);
		for (String snippet: text.split("\n")){
			entities.add(snippet);
		}
		
		return entities;
	}

}
