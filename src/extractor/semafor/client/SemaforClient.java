package extractor.semafor.client;

/*
 * This client will not be used at the moment.
 * 
 * 
 */

public class SemaforClient{
	
	private static SemaforClient instance;
	
	private SemaforClient(){}

	
	public static SemaforClient getInstance(){
		
		if(instance == null) {
			@SuppressWarnings("unused")
			SemaforClient instance = new SemaforClient();
		}
		
		return instance;
	}
	
}
