package jSkelExtractor.algorithms.sdskeletrizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import soam.Edge;
import soam.Mesh;
import soam.Vertex;

public class MeshGraph extends Mesh{
	

	private static final long serialVersionUID = 1L;
	protected Map<Vertex,Double> nodesWeights;
	
	
	public MeshGraph(){
		super();
		this.nodesWeights = new HashMap<Vertex, Double>();
	
	}
	
	public void addNode(Vertex node){
		this.vertexlist.add(node);
		this.nodesWeights.put(node, 0.0);
		this.addNodeConnections(node, node.getNeighbors());
	}
	
	public void addNodeConnections(Vertex node,ArrayList<Vertex> connectedVertexes){
		Edge edge;
		for(Vertex conn:connectedVertexes){
			edge = new Edge(node,conn);
			if(!this.edgelist.contains(edge)){
				node.neighborhood.add(edge);
				this.edgelist.add(edge);
			}
		}
	}
	
	public void setNodeInfluence(Vertex node, ArrayList<Vertex> belongTo){
		nodesWeights.put(node, nodesWeights.get(node)+belongTo.size());
	}
	
	public void normalizeWeights(){
		Double maxVal = null;
		Double minVal = null;
		for(Double val:nodesWeights.values()){
			if((maxVal == null)||(val>maxVal)) maxVal = val;
			if((minVal == null)||(val<minVal)) minVal = val;
		}
		for(Map.Entry<Vertex, Double> entry:nodesWeights.entrySet()){
			entry.setValue((entry.getValue()-minVal)/(maxVal-minVal));
		}
	}
	
	
	
	

}
