package extractor.elastic.lib;

import java.util.HashMap;
import java.util.Map;

public class TopicDict {
	
	private static Map<String, String> topicDict;
	
	private TopicDict(){}
	
	public static Map<String, String> getTopicDict(){
		
		if(topicDict == null){
			topicDict = new HashMap<String, String>();
			topicDict.put("beef_ban", "crl01");
			topicDict.put("gun_control", "csc02");
			topicDict.put("gay_marriage", "chr01");
			topicDict.put("climate_change", "cst01");
			topicDict.put("refugee", "cbp02");
		}
		
		return topicDict;
	}

}
