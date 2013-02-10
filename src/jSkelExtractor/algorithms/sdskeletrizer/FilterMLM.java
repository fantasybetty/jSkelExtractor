package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.configs.ConfigObj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import soam.Mesh;
import soam.Vertex;

public class FilterMLM extends AbstractAlgorithm {

	protected ArrayList<Vertex> featurePoints;
	protected SDSResContainer res;

	public FilterMLM(Mesh mesh) {
		super(mesh, ConfigObj.getInstance());
		this.res = configObj.getResContainer();
		this.reset();
	}

	@Override
	public void execute() {
		ArrayList<Vertex> E1 = res.getFirstMaximumSet();
		ArrayList<Vertex> E2 = res.getSecondMaximumSet();

		E1.add(res.getDiameter().get(1));
		E2.add(res.getDiameter().get(0));
		
		Dijkstra dijkstra;
		double vertexDistance;
		Vertex currentVertex;
		Set<Vertex> inquiryMapList;

		Iterator<Vertex> it = E1.iterator();
		while(it.hasNext()) {
			currentVertex = it.next();
			vertexDistance = res.getFirstMaximumSetDistanceMap().get(currentVertex);
			dijkstra = new Dijkstra(mesh, currentVertex, configObj.getEpsilon()
					* res.getMaxDistance());
			dijkstra.execute();
			dijkstra.normalizeDistances(res.getMaxDistance());
			inquiryMapList = dijkstra.getDistanceMap().keySet();
			inquiryMapList.remove(currentVertex);
			
			for(Vertex val:inquiryMapList){
				if(res.getFirstMaximumSetDistanceMap().get(val)>=vertexDistance){
					it.remove();
					break;
				}
			}
		}

		it = E2.iterator();
		while(it.hasNext()) {
			currentVertex = it.next();
			vertexDistance = res.getSecondMaximumSetDistanceMap().get(currentVertex);
			dijkstra = new Dijkstra(mesh, currentVertex, configObj.getEpsilon()
					* res.getMaxDistance());
			dijkstra.execute();
			dijkstra.normalizeDistances(res.getMaxDistance());
			inquiryMapList = dijkstra.getDistanceMap().keySet();
			inquiryMapList.remove(currentVertex);
			
			for(Vertex val:inquiryMapList){
				if(res.getSecondMaximumSetDistanceMap().get(val)>=vertexDistance){
					it.remove();
					break;
				}
			}
		}


		setChanged();
		notifyObservers();
	}

	@Override
	public void reset() {
		featurePoints = new ArrayList<Vertex>();
	}

}
