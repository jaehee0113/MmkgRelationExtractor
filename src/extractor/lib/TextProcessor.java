package extractor.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Scanner;

public class TextProcessor implements Text {
	
	String topic;
	String source;
	
	public TextProcessor() {
		this.topic = "beef_ban";
		this.source = "article";
	}
	
	public TextProcessor(String topic, String source) {
		this.topic = topic;
		this.source = source;
	}
	
	@Override
	public URL getURL() {
		// TODO Auto-generated method stub
		URL file = this.getClass().getResource("files/" + topic + "-" + source + "-output.txt");

		return file;
	}

	@Override
	public String toString(URL fileURL) {
		// TODO Auto-generated method stub
		try {
			@SuppressWarnings("resource")
			String content = new Scanner(new File(fileURL.getPath())).useDelimiter("\\Z").next();
			return content;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}		
	}

}
