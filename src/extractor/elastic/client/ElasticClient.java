package extractor.elastic.client;

import java.net.InetAddress;
import java.net.UnknownHostException;


import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import extractor.elastic.config.ElasticConfig;

public class ElasticClient {
	
	private static TransportClient instance;
	
	private ElasticClient(){}
	
	@SuppressWarnings("resource")
	public static TransportClient getInstance(){
		
		if(instance == null) {
			Settings settings = Settings.builder().put("cluster.name", ElasticConfig.CLUSTER_NAME).build();
			try {
				instance = new PreBuiltTransportClient(settings).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ElasticConfig.SERVER_IP), 9300));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return instance;
	}
	
	public String toString(){
		return "Node name :" + instance.nodeName() + " asdf" + instance.listedNodes();
	}

}
