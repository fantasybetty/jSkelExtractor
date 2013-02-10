package jSkelExtractor.algorithms;



import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.controllers.DijkstraController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soam.Mesh;
import soam.Vertex;

public class Dijkstra extends AbstractAlgorithm{
	
	private Mesh mesh;
	private Vertex startingPoint;
	private Map<Vertex, Double> distanceMap;
	private Map.Entry<Vertex, Double> diameter;
	private boolean isNormalized;
	private HashMap<Vertex, Double> neighborsList;
	private Set<Vertex> visitedVertexes;
	private ArrayList<Vertex> neighbors;
	private ArrayList<Vertex> vertexesTmp;
	private double vertexDistanceTmp;
	private Map.Entry<Vertex, Double> minDistanceOfNeighbors;
	private double endDistance;

	private boolean interactive;
	private long iteration;
	private Double maxDistance;

	
	public Dijkstra(Mesh mesh, Vertex startingPoint, double stopDistance,boolean interactive){
		super(mesh,ConfigObj.getInstance());
		this.mesh = mesh;
		this.startingPoint = startingPoint;
		this.interactive = interactive;
		
		this.reset();
		this.endDistance = stopDistance;
		if(interactive) new DijkstraController(this);
		
		
	}
	
	public Dijkstra(Mesh mesh, Vertex startingPoint){
		this(mesh,startingPoint,-1.0,false);
	}
	
	public Dijkstra(Mesh mesh, Vertex startingPoint, boolean interactive){
		this(mesh,startingPoint,-1.0,interactive);
	}
	public Dijkstra(Mesh mesh, Vertex startingPoint, double stopDistance){
		this(mesh,startingPoint,stopDistance,false);
	}

	
	public HashMap<Vertex, Double> getNeighborsList() {
		return neighborsList;
	}


	public boolean isNormalized() {
		return isNormalized;
	}


	public void setNormalized(boolean isNormalized) {
		this.isNormalized = isNormalized;
	}


	public boolean isInteractive() {
		return interactive;
	}


	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}


	@Override
	public void execute() {

		while(true){

			/* Aggiorno la "frontiera" cioè l'insieme dei vertici direttamente connessi ai vertici già visitati. Per fare questo esploro il vicinato di
			 * ciascun vertice contenuto nell'elenco di quelli visitati, a condizione che il vicino in esame NON sia a sua volta già visitato.
			 * NB: non ringrazierò mai abbastanza SUN per aver introdotto le Collection :-)
			 */
			for(Vertex visited:visitedVertexes){
				neighbors = visited.getNeighbors();
				if(!visitedVertexes.containsAll(neighbors)){
				for(Vertex neighbor:neighbors){
					vertexesTmp = new ArrayList<Vertex>();
					if(!visitedVertexes.contains(neighbor)){ 
						
						/* Verifico tutti i vicini del vertice che voglio inserire nella frontiera, quali di questi appartengono alla lista dei vertici visitati, 
						 * quindi possono darmi un valore utile per la distanza. Questo passaggio serve per gestire il caso in cui un vertice abbia più padri già presenti 
						 * nell'elenco dei vertici visitati. Praticamente sempre.
						 */
						for(Vertex connected:neighbor.getNeighbors()){ // Verifica tutti i percorsi possibili per giungere al vertice
							if(visitedVertexes.contains(connected)) vertexesTmp.add(connected);
						}
						
						
						/* Calcolo la distanza minima tra quelle possibili (sempre rispetto al vertice di partenza) */
						vertexDistanceTmp = getClosestDistance(neighbor,vertexesTmp);
						
						/* Inserisco il verticce di frontiera nella lista dei vicini con il valore distanza calcolato. 
						 * se esiste già il valore di distanza viene aggiornato (sostituito) con quello minimo che ho appena calcolato.
						 * Questo posso senza verificare esplicitamente la cosa perchè non mi interessa tenere traccia dei padri lungo il cammino minimo
						 */
						neighborsList.put(neighbor, vertexDistanceTmp);
					}
				}
				}
			}
			
			/* Cerco il vertice a distanza minima (da quelli visitati) e lo aggiungo alla lista di quelli visitati */
			minDistanceOfNeighbors = null;
			for(Map.Entry<Vertex, Double> entry:neighborsList.entrySet()){
				if((minDistanceOfNeighbors==null)||(entry.getValue()<minDistanceOfNeighbors.getValue())){
					minDistanceOfNeighbors = entry;
				}
			}
			distanceMap.put(minDistanceOfNeighbors.getKey(), minDistanceOfNeighbors.getValue());
			neighborsList.remove(minDistanceOfNeighbors.getKey());
			
			//System.out.println("DM size: "+distanceMap.size()+" of "+mesh.vertexlist.size()+" Remaining "+neighborsList.size()+" neighbors.");
			
			if((endDistance>0)&&(minDistanceOfNeighbors.getValue()>endDistance)){
				this.isFinished = true;
				break;
			}
			
			if((neighborsList.size() == 0) && (distanceMap.size() == mesh.vertexlist.size())) {
				this.isFinished = true;
				break;
			}
			iteration++;
			if(interactive) break;

		}
	}
	

	private double getClosestDistance(Vertex v1, ArrayList<Vertex> tmp) {
		double min = distanceMap.get(tmp.get(0))+v1.distanceFrom(tmp.get(0));
		double distTmp;
		//System.out.println(tmp.size());
		for(Vertex v2:tmp){
			distTmp = distanceMap.get(v2) + v2.distanceFrom(v1);
			if(distTmp<min) min = distTmp;
		}
		return min;
	}


	@Override
	public void reset() {
		distanceMap = new HashMap<Vertex, Double>();
		neighborsList = new HashMap<Vertex, Double>();
		visitedVertexes = distanceMap.keySet();
		neighbors = null;
		diameter = null;		
		iteration = 0;
		setEndDistance(-1);
		distanceMap.put(startingPoint, 0.0);

	}

	public Mesh getMesh() {
		return mesh;
	}

	public void setMesh(Mesh mesh) {
		this.mesh = mesh;
	}

	public Vertex getStartingPoint() {
		return startingPoint;
	}

	public void setStartingPoint(Vertex startingPoint) {
		this.startingPoint = startingPoint;
	}

	public Map<Vertex, Double> getDistanceMap() {
		return distanceMap;
	}
	
	public boolean normalizeDistances(){
		maxDistance = this.getDiameter().getValue();
		return this.normalizeDijkstradistances();
	}
	
	private boolean normalizeDijkstradistances(){
		Set<Map.Entry<Vertex, Double>> values = distanceMap.entrySet();
		for(Map.Entry<Vertex, Double> val:values){
			val.setValue(val.getValue()/maxDistance);
		}
		this.isNormalized = true;
		return this.isNormalized;
	}


	public Map.Entry<Vertex, Double> getDiameter(){
		if(this.diameter == null){
			Set<Map.Entry<Vertex, Double>> distances = distanceMap.entrySet();
			Map.Entry<Vertex, Double> max = null;
			for(Map.Entry<Vertex, Double> dist:distances){
				if((max == null )||(dist.getValue() > max.getValue())) {
					max = dist;
				}
			}
			this.diameter = max;
		}
		return diameter;
	}

	public void setEndDistance(double endDistance) {
		this.endDistance = endDistance;
	}

	public double getEndDistance() {
		return endDistance;
	}

	public void normalizeDistances(double maxDistance) {
		this.maxDistance = maxDistance;
		
	}


}


