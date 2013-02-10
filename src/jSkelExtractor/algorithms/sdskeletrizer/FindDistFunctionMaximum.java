package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.configs.ConfigObj;

import java.util.ArrayList;
import java.util.Map;

import soam.Mesh;
import soam.Vertex;

public class FindDistFunctionMaximum extends AbstractAlgorithm {
	protected SDSResContainer res;
	private ArrayList<Vertex> firstSet;
	private ArrayList<Vertex> secondSet;
	private Dijkstra dijkstra;
	
	
	public FindDistFunctionMaximum(Mesh mesh) {
		super(mesh,ConfigObj.getInstance());
		this.res = configObj.getResContainer();
		this.reset();
	}

	@Override
	public void execute() {
		Map<Vertex, Double> distances;
		ArrayList<Vertex> neighbors;
		Double fatherDistance;
		boolean isFeature;
		int first = 0;
		int second = 1;
		
		
		dijkstra = new Dijkstra(mesh,res.getDiameter().get(first),false);

		/****************************************/
		/* Primo punto							*/
		/****************************************/
		dijkstra.execute();
		dijkstra.normalizeDistances();
		distances = dijkstra.getDistanceMap();
		res.setFirstMaximumSetDistanceMap(dijkstra.getDistanceMap());

		
		for(Map.Entry<Vertex, Double> entry:distances.entrySet()){
			neighbors = entry.getKey().getNeighbors();
			fatherDistance = entry.getValue();
			isFeature = true;
			
			/*
			 * Impongo la condizione che per essere feature point deve avere distanza rispetto al padre, maggiore di tutto il suo vicinato
			 */
			for(Vertex v:neighbors){
				if(fatherDistance<distances.get(v)) isFeature = false;
			}
			
			/*
			 * Verifico se sono in prossimità di un altro feature point. Impongo il vincolo che in una palla di raggio epsilon non vi sia più di un feature point
			 */
			for(Vertex v:firstSet){
				if((Math.abs(fatherDistance-distances.get(v)))<configObj.getVertexNeighborhoodDivergence()) isFeature = false;
			}
			if(isFeature) firstSet.add(entry.getKey());
		}

		this.res.setFirstMaximumSet(firstSet);
		

		/****************************************/
		/* Secondo punto							*/
		/****************************************/
		dijkstra.setStartingPoint(res.getDiameter().get(second));
		dijkstra.reset();
		dijkstra.execute();
		dijkstra.normalizeDistances();
		distances = dijkstra.getDistanceMap();
		res.setSecondMaximumSetDistanceMap(dijkstra.getDistanceMap());

		
		for(Map.Entry<Vertex, Double> entry:distances.entrySet()){
			neighbors = entry.getKey().getNeighbors();
			fatherDistance = entry.getValue();
			isFeature = true;
			
			/*
			 * Impongo la condizione che per essere feature point deve avere distanza rispetto al padre, maggiore di tutto il suo vicinato
			 */
			for(Vertex v:neighbors){
				if(fatherDistance<distances.get(v)) isFeature = false;
			}
			
			/*
			 * Verifico se sono in prossimità di un altro feature point. Impongo il vincolo che in una palla di raggio epsilon non vi sia più di un feature point
			 */
			for(Vertex v:secondSet){
				if((Math.abs(fatherDistance-distances.get(v)))<configObj.getVertexNeighborhoodDivergence()) isFeature = false;
			}
			if(isFeature) secondSet.add(entry.getKey());
		}

		
		this.res.setSecondMaximumSet(secondSet);

		
		
		setChanged();
		notifyObservers();
		
	}

	@Override
	public void reset() {
		this.firstSet = new ArrayList<Vertex>();
		this.secondSet = new ArrayList<Vertex>();		
	}

}
