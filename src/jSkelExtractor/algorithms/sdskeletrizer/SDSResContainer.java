package jSkelExtractor.algorithms.sdskeletrizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import soam.Mesh;
import soam.Vertex;

public class SDSResContainer {
	
	private Mesh mesh;
	private ArrayList<Vertex> featurePoints;
	private double maxDistance;
	private Vertex startingVertex;
	private ArrayList<Vertex> diameterVertex;
	private ArrayList<Vertex> firstMaximumSet;
	private Map<Vertex, Double> firstMaximumSetDistanceMap;
	
	private ArrayList<Vertex> secondMaximumSet;
	private Map<Vertex, Double> secondMaximumSetDistanceMap;

	private Map<Vertex, Integer> vertexFPindexMatching;
	private Map<Vertex, Double> distanceMap;
	private ArrayList<ArrayList<Vertex>> featuresLists;
	private MeshGraph graph;
	private ArrayList<ArrayList<Vertex>> discreteContoursList;

	public SDSResContainer(){
		this.reset();
		this.graph = new MeshGraph();
		this.setMesh(null);
	}
	
	public void reset(){
		setFeaturePoints(new ArrayList<Vertex>());
		distanceMap = new HashMap<Vertex, Double>();
		maxDistance = -1;
		startingVertex = null;
		diameterVertex = new ArrayList<Vertex>();
		firstMaximumSet = new ArrayList<Vertex>();
		firstMaximumSetDistanceMap = new HashMap<Vertex, Double>();
		secondMaximumSet = new ArrayList<Vertex>();
		secondMaximumSetDistanceMap = new HashMap<Vertex, Double>();
		setVertexFPmatching(new HashMap<Vertex, Integer>());
		setFeaturesLists(new ArrayList<ArrayList<Vertex>>());
		
	}



	public Map<Vertex, Double> getDistanceMap() {
		return distanceMap;
	}


	public void setDistanceMap(Map<Vertex, Double> distanceMap) {
		this.distanceMap = distanceMap;
	}


	public double getMaxDistance() {
		return maxDistance;
	}


	public void setMaxDistance(double max) {
		this.maxDistance = max;
	}


	public void setDiameterVertex(Vertex vertex) {
		this.diameterVertex.add(vertex);
	}
	
	public ArrayList<Vertex> getDiameter(){
		return this.diameterVertex;
	}


	public void setStartingVertex(Vertex startingVertex) {
		this.startingVertex = startingVertex;
	}


	public Vertex getStartingVertex() {
		return startingVertex;
	}


	/**
	 * @param firstMaximumSet the firstMaximumSet to set
	 */
	public void setFirstMaximumSet(ArrayList<Vertex> firstMaximumSet) {
		this.firstMaximumSet = firstMaximumSet;
	}


	/**
	 * @return the firstMaximumSet
	 */
	public ArrayList<Vertex> getFirstMaximumSet() {
		return firstMaximumSet;
	}


	/**
	 * @param secondMaximumSet the secondMaximumSet to set
	 */
	public void setSecondMaximumSet(ArrayList<Vertex> secondMaximumSet) {
		this.secondMaximumSet = secondMaximumSet;
	}


	/**
	 * @return the secondMaximumSet
	 */
	public ArrayList<Vertex> getSecondMaximumSet() {
		return secondMaximumSet;
	}

	public Map<Vertex, Double> getFirstMaximumSetDistanceMap() {
		return firstMaximumSetDistanceMap;
	}

	public void setFirstMaximumSetDistanceMap(
			Map<Vertex, Double> firstMaximumSetDistanceMap) {
		this.firstMaximumSetDistanceMap = firstMaximumSetDistanceMap;
	}

	public Map<Vertex, Double> getSecondMaximumSetDistanceMap() {
		return secondMaximumSetDistanceMap;
	}

	public void setSecondMaximumSetDistanceMap(
			Map<Vertex, Double> secondMaximumSetDistanceMap) {
		this.secondMaximumSetDistanceMap = secondMaximumSetDistanceMap;
	}

	public void setVertexFPmatching(Map<Vertex, Integer> vertexFPmatching) {
		this.vertexFPindexMatching = vertexFPmatching;
	}

	public Map<Vertex, Integer> getVertexFPmatching() {
		return vertexFPindexMatching;
	}

	public void setFeaturesLists(ArrayList<ArrayList<Vertex>> featuresLists) {
		this.featuresLists = featuresLists;
	}

	public ArrayList<ArrayList<Vertex>> getFeaturesLists() {
		return featuresLists;
	}

	public void setFeaturePoints(ArrayList<Vertex> featurePoints) {
		this.featurePoints = featurePoints;
	}

	public ArrayList<Vertex> getFeaturePoints() {
		return featurePoints;
	}

	public void setMesh(Mesh mesh) {
		this.mesh = mesh;
	}

	public Mesh getMesh() {
		return mesh;
	}

	public MeshGraph getGraph() {
		return this.graph;
	}

	public void setDiscreteContoursList(ArrayList<ArrayList<Vertex>> discreteContoursList) {
		this.discreteContoursList = discreteContoursList;
		
	}

	public ArrayList<ArrayList<Vertex>> getDiscreteContoursList() {
		return discreteContoursList;
	}


}
