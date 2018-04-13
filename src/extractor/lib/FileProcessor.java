package extractor.lib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import extractor.models.Article;
import extractor.models.MMKGRelationTriple;
import extractor.semafor.config.SemaforConfig;


public class FileProcessor {
	
    protected static String readFileAsString(File file) throws IOException {
        byte[] buffer = new byte[(int) file.length()];
        @SuppressWarnings("resource")
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(file));
        f.read(buffer);
        return new String(buffer);
    }
    
    public static List<String> getSentencesFromArticle(Article article){
    	
    	List<String> sentences = new ArrayList<String>();
    	List<MMKGRelationTriple> triples = article.getTriples();
    	
    	for(MMKGRelationTriple triple: triples){
    		sentences.add(triple.getSentenceToString());
    	}
    	
    	return sentences;
    	
    }
    
    //used for generating text file of sentences of an article
    public static void writeFile(List<String> sentences, String out_file_name){
    	    	
    	try {
			PrintWriter writer = new PrintWriter(SemaforConfig.INPUT_FILE_DIR + out_file_name + ".txt", "UTF-8");
			
			for(String sent: sentences){
				writer.println(sent);
			}
			
			writer.close();
			System.out.println("File has successfully been generated.");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
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
