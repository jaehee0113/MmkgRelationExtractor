package extractor.export.gexf;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import static java.nio.charset.StandardCharsets.*;
import edu.stanford.nlp.ie.util.RelationTriple;
import extractor.models.MMKGRelationTriple;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.Attribute;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

public class GexfGraph {
	
	Gexf gexf;
	Graph graph;
	
	public GexfGraph(){
		this.gexf = new GexfImpl();
		setGraph();
	}
	
	public void setGraph(){
		this.graph = gexf.getGraph();
	}
	
	public Edge getEdge(String id){
		List<Edge> edges = graph.getAllEdges();
		for(Edge edge: edges){
			if(edge.getId().equals(id))
				return edge;
		}
		
		return null;
	}
	
	//Map<Sentence, corresponding triple [order: subject, relation, triple>
	public void createGraphFromTriples(List<MMKGRelationTriple> triples){
			
		AttributeList attrEdgeList = new AttributeListImpl(AttributeClass.EDGE);
		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		
		graph.getAttributeLists().add(attrList);
		//URL of the DBpedia concept link
		Attribute attUrl = attrList.createAttribute(AttributeType.STRING, "url");
		
		//Subject or Object
		Attribute entType = attrList.createAttribute(AttributeType.STRING, "entity_type");
		
		//Concept (e.g. Person)
		Attribute type = attrList.createAttribute(AttributeType.LONG, "type");
		
		graph.getAttributeLists().add(attrEdgeList);
		Attribute attEdgeUrl = attrEdgeList.createAttribute(AttributeType.STRING, "url");
		Attribute time = attrEdgeList.createAttribute(AttributeType.STRING, "time");
		
		for (MMKGRelationTriple triple : triples) {
			
			String modified_date = "";
			
			if(triple.getTimestamp() != null){
				Calendar cal = Calendar.getInstance();
				cal.setTime(triple.getTimestamp());
				modified_date = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());	
			}else{
				//set the date that is easily distinguishable
				modified_date = "2011-01-01";
			}
			
			RelationTriple original_triple = triple.getTriple();
			
			/*
			 * Node id: concept (if null, just null) + ent_type (subj, obj) + gloss
			 * Edge id: frame (if null, just null) + gloss
			 */
			String sID = "";
			String oID = "";
			String rID = "";
			String lowercased_subj_gloss = original_triple.subjectGloss().replaceAll(" ", "_").toLowerCase();
			String lowercased_obj_gloss = original_triple.objectGloss().replaceAll(" ", "_").toLowerCase();
			String lowercased_rel_gloss = original_triple.relationGloss().replaceAll(" ", "_").toLowerCase();
			
			if(triple.getSubjectConcept() == null) sID = "subj_" + lowercased_subj_gloss;
			else sID = triple.getSubjectConcept().replaceAll(" ", "_").toLowerCase() + "_subj_" + lowercased_subj_gloss;
			
			if(triple.getObjectConcept() == null) oID = "obj_" + lowercased_obj_gloss;
			else oID = triple.getObjectConcept().replaceAll(" ", "_").toLowerCase() + "_obj_" + lowercased_obj_gloss;
			
			if(triple.getRelationFrame() == null) rID = "rel_" + lowercased_subj_gloss + "_" + lowercased_rel_gloss + "_" + lowercased_obj_gloss;
			else rID = "rel_" + lowercased_subj_gloss + "_" + lowercased_rel_gloss + "_" + triple.getRelationFrame().replaceAll(" ", "_").toLowerCase() + "_" + lowercased_obj_gloss;
			
			//encoding
			byte[] stext = sID.getBytes(ISO_8859_1); 
			String encoded_sID = new String(stext, UTF_8);
			
			byte[] otext = oID.getBytes(ISO_8859_1); 
			String encoded_oID = new String(otext, UTF_8);
			
			byte[] rtext = rID.getBytes(ISO_8859_1); 
			String encoded_rID = new String(rtext, UTF_8);
			
			//if that node is not available, then render
			if(graph.getNode(encoded_sID) == null){
				Node subject = graph.createNode(encoded_sID);
				subject.setLabel(original_triple.subjectGloss());
				subject.getAttributeValues().addValue(entType, "subject");
				
				if(triple.getSubjectConceptType() != null)
					subject.getAttributeValues().addValue(type, triple.getSubjectConceptType());
				else
					subject.getAttributeValues().addValue(type, "99");

				
				if(triple.getSubjectConcept() != null)
					subject.getAttributeValues().addValue(attUrl, triple.getSubjectConcept());
				else
					subject.getAttributeValues().addValue(attUrl, "null");
			}
			
			Node subject = graph.getNode(encoded_sID);
			
			if(graph.getNode(encoded_oID) == null){
				Node object = graph.createNode(encoded_oID);
				object.setLabel(original_triple.objectGloss());
				object.getAttributeValues().addValue(entType, "object");
				
				if(triple.getObjectConceptType() != null)
					object.getAttributeValues().addValue(type, triple.getObjectConceptType());
				else
					object.getAttributeValues().addValue(type, "99");
				
				if(triple.getObjectConcept() != null)
					object.getAttributeValues().addValue(attUrl, triple.getObjectConcept());	
				else
					object.getAttributeValues().addValue(attUrl, "null");
			}
			
			Node object = graph.getNode(encoded_oID);
			
			if(getEdge(encoded_rID) == null && !subject.hasEdgeTo(encoded_oID)){
				Edge relation = subject.connectTo(encoded_rID, object);
				relation.setWeight(1);
				relation.setLabel(original_triple.relationGloss());
				if(triple.getRelationFrame() != null)
					relation.getAttributeValues().addValue(attEdgeUrl, triple.getRelationFrame());
				else
					relation.getAttributeValues().addValue(attEdgeUrl, "null");
				
				relation.getAttributeValues().addValue(time, modified_date);
			}else{
				System.out.println("(" + original_triple.subjectGloss() + "," + original_triple.relationGloss() + "," + original_triple.objectGloss() + ") has been ignored due to duplicates.");
			}
			
		}
		
	}
	
	public void exportGexfGraph(String file_name){
		
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File("app/app/graph/" + file_name + ".gexf");
		Writer out;
		try{
			out =  new FileWriter(f, false);
			graphWriter.writeToStream(this.gexf, out, "UTF-8");
			System.out.println("Exported to " + f.getAbsolutePath());
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}

}
