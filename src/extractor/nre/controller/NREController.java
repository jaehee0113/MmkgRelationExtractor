package extractor.nre.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.simple.Sentence;
import extractor.models.Article;
import extractor.models.MMKGRelationTriple;
import extractor.nre.config.NREConfig;

public class NREController {
	
    /**
     * Create a file (text file), which will be used as an output template.
     * @param name the name of a file.
     */
	public static void createFile(String name){
		try {
			//Just creating a file
			PrintWriter writer = new PrintWriter(name, "UTF-8");
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /**
     * Write lines corresponding to each article
     * @param article the article extracted from ElasticSearch DB.
     */
	public static void writeLine(Article article, String fileName){
		File file = new File(fileName);
		FileWriter writer;
		try {
			writer = new FileWriter(file, true);
			PrintWriter printer = new PrintWriter(writer);
			
			Set<String> uniquelines = new HashSet<String>();
			List<MMKGRelationTriple> triples = article.getTriples();
			for(MMKGRelationTriple triple: triples){
				Sentence sent = triple.getSentence();	
				String subject = triple.getTriple().subjectGloss();
				String object = triple.getTriple().objectGloss();
				uniquelines.add("m.01l443l" + "\t" + "m.04t_bj" + "\t" + subject + "\t" + object + "\t" + "NA" + "\t" + sent.toString().replace("\n", "").replace("\r", "") + "\t" + "###END###");
			}
			
			for(String line: uniquelines){
				printer.append(line + "\n");
			}
			
			printer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
