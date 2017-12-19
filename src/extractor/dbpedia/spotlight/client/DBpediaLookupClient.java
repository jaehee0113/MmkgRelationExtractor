package extractor.dbpedia.spotlight.client;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

//example: http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=Flagstaff&QueryClass=XML&MaxHits=10


public class DBpediaLookupClient extends DefaultHandler {
	
  private SAXParserFactory factory;
  private SAXParser sax;
  private List<Map<String, String>> variableBindings = new ArrayList<Map<String, String>>();
  private Map<String, String> tempBinding = null;
  private String lastElementName = null;
  private String query = "";
  
  public DBpediaLookupClient(String query) throws Exception {
	  
    this.query = query;
    HttpClient client = new HttpClient();

    String query2 = query.replaceAll(" ", "+");
    HttpMethod method =
      new GetMethod("http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" +
        query2);
    
    try {
      client.executeMethod(method);      
      InputStream ins = method.getResponseBodyAsStream();
      factory = SAXParserFactory.newInstance();
      sax = factory.newSAXParser();
      sax.parse(ins, this);
    } catch (HttpException he) {
      System.err.println("Http error connecting to lookup.dbpedia.org");
    } catch (IOException ioe) {
      System.err.println("Unable to connect to lookup.dbpedia.org");
    }
    method.releaseConnection();
  }
  
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equalsIgnoreCase("result")) tempBinding = new HashMap<String, String>();
    lastElementName = qName;
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {

    if (qName.equalsIgnoreCase("result")) {
      if (!variableBindings.contains(tempBinding) && containsSearchTerms(tempBinding)) variableBindings.add(tempBinding);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    
	  String s = new String(ch, start, length).trim();

    if (s.length() > 0) {
      if ("Description".equals(lastElementName)) {
        if (tempBinding.get("Description") == null) tempBinding.put("Description", s);
        tempBinding.put("Description", "" + tempBinding.get("Description") + " " + s);
      }

      if ("URI".equals(lastElementName) && s.indexOf("Category")==-1 && tempBinding.get("URI") == null) tempBinding.put("URI", s);
      if ("Label".equals(lastElementName)) tempBinding.put("Label", s);
    }
  }
  
  private boolean containsSearchTerms(Map<String, String> bindings) {
	  
    StringBuilder sb = new StringBuilder();
    for (String value : bindings.values()) sb.append(value);  // do not need white space
    String text = sb.toString().toLowerCase();
    StringTokenizer st = new StringTokenizer(this.query);
    while (st.hasMoreTokens()) {
      if (text.indexOf(st.nextToken().toLowerCase()) == -1) {
        return false;
      }
    }
    return true;
    
  } 
  
  /*
   * 
   * Getters
   * 
   * 
   */
  
  public Map<String, String> getResult(){
	  return this.tempBinding;
  }
  
  public List<Map<String, String>> getResults(){
	  return this.variableBindings;
  }

}