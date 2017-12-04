package extractor.dbpedia.spotlight.client;

/*
 * This client will not be used at the moment.
 * 
 * 
 */

public class DBpediaSpotlightClient{
	
	private static DBpediaSpotlightClient instance;
	
	private DBpediaSpotlightClient(){}

	
	public static DBpediaSpotlightClient getInstance(){
		
		if(instance == null) {
			@SuppressWarnings("unused")
			DBpediaSpotlightClient instance = new DBpediaSpotlightClient();
		}
		
		return instance;
	}	
	
}
