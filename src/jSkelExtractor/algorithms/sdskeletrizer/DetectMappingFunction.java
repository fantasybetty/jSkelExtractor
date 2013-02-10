package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.configs.ConfigObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import soam.Mesh;
import soam.Vertex;

public class DetectMappingFunction extends AbstractAlgorithm {

	protected SDSResContainer res;

	protected ArrayList<Vertex> featurePoints;
	protected Map<Vertex, Integer> vertexFPmatching;
	protected Map<Vertex, Double> distanceMap;
	protected ArrayList<ArrayList<Vertex>> featuresLists;
	protected ArrayList<Map<Vertex, Double>> dijkstraMaps;
	
	public DetectMappingFunction(Mesh mesh) {
		super(mesh, ConfigObj.getInstance());
		this.res = configObj.getResContainer();
		this.reset();
	}

	@Override
	public void execute() {
		this.reset();
		
		Dijkstra dijkstra;
		
		// Performs Dijkstra for each starting point
		for(Vertex fp:featurePoints){
			featuresLists.add(new ArrayList<Vertex>());
			dijkstra = new Dijkstra(mesh,fp);
			dijkstra.execute();
			dijkstra.normalizeDistances();
			dijkstraMaps.add(dijkstra.getDistanceMap());
		}
		
		// Match each vertex with its nearest feature point
		Double maximumValue = null;
		Double minimumValue = null;
		for(Vertex vertex:mesh.vertexlist){
			Vertex vertex_minFeaturePoint = null;
			Double vertex_minDistance = null;
			double currentDistance;
			Integer minIndex = null;
			
			for(int fpIndex = 0;fpIndex<dijkstraMaps.size();fpIndex++){
				Map<Vertex,Double> map = dijkstraMaps.get(fpIndex);
				currentDistance = map.get(vertex);
				if((vertex_minFeaturePoint == null)||(currentDistance<vertex_minDistance)){
					vertex_minFeaturePoint = featurePoints.get(fpIndex);
					vertex_minDistance = currentDistance;
					minIndex = fpIndex; 
				}
			}
			distanceMap.put(vertex, vertex_minDistance);
			vertexFPmatching.put(vertex, minIndex);
			featuresLists.get(minIndex).add(vertex);

			if((minimumValue == null)||(vertex_minDistance<minimumValue)) minimumValue = vertex_minDistance;
			else
				if((maximumValue == null)||(vertex_minDistance>maximumValue)) maximumValue = vertex_minDistance;

		}
		
		// Normalizes distance map
		for(Map.Entry<Vertex, Double> entry:distanceMap.entrySet()){
			entry.setValue(1-((entry.getValue()-minimumValue)/(maximumValue-minimumValue)));
		}
		
		res.setDistanceMap(distanceMap);
		res.setFeaturesLists(featuresLists);
		res.setVertexFPmatching(vertexFPmatching);
		
		setChanged();
		notifyObservers();
	}

	@Override
	public void reset() {
		featurePoints = res.getFeaturePoints();
		vertexFPmatching = new HashMap<Vertex, Integer>();;
		distanceMap = new HashMap<Vertex, Double>();
		featuresLists = new ArrayList<ArrayList<Vertex>>();
		dijkstraMaps = new ArrayList<Map<Vertex,Double>>();
	}

}
