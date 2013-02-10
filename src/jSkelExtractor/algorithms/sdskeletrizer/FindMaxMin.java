package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.configs.ConfigObj;

import java.util.ArrayList;
import java.util.Map;

import soam.Mesh;
import soam.Vertex;

public class FindMaxMin extends AbstractAlgorithm {

	protected ArrayList<Vertex> minMax;
	protected SDSResContainer res;
	
	public FindMaxMin(Mesh mesh){
		super(mesh,ConfigObj.getInstance());
		this.res = configObj.getResContainer();
		this.reset();
	}

	@Override
	public void execute() {
		res.setStartingVertex(mesh.vertexlist.get(configObj.getStartingPoint()));
		Dijkstra dijkstra = new Dijkstra(mesh,res.getStartingVertex(),false);
		dijkstra.execute();
		
		Map.Entry<Vertex, Double> diameter;
		diameter = dijkstra.getDiameter();
		minMax.add(diameter.getKey());
		res.setDiameterVertex(diameter.getKey());
		res.setMaxDistance(diameter.getValue());

		
		
		dijkstra.setStartingPoint(diameter.getKey());
		dijkstra.reset();
		dijkstra.execute();
		diameter = dijkstra.getDiameter();

		minMax.add(diameter.getKey());
		res.setDiameterVertex(diameter.getKey());

		
		setChanged();
		notifyObservers();
	}


	@Override
	public void reset() {
		minMax = new ArrayList<Vertex>();
		
	}
	
	
}
