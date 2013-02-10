package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.configs.ConfigObj;

import java.util.ArrayList;
import java.util.Set;

import soam.Mesh;
import soam.Vertex;

public class FindFeaturePoints_Luchino extends AbstractAlgorithm {

	protected ArrayList<Vertex> fPoints;
	protected SDSResContainer res;
	
	public FindFeaturePoints_Luchino(Mesh mesh){
		super(mesh,ConfigObj.getInstance());

		this.res = configObj.getResContainer();
		this.reset();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() {
		// 1. Get E1 and E2
		ArrayList<Vertex> E1 = res.getFirstMaximumSet();
		ArrayList<Vertex> E2 = res.getSecondMaximumSet();
		

		// 2. Build intersection between E1 and E2
		ArrayList<Vertex> F = new ArrayList<Vertex>();
		for(Vertex v:E1){
			if(E2.contains(v)) F.add(v);



		}
		// Diameter Vertex are feature points
		//F.addAll(res.getDiameter());

		fPoints = new ArrayList<Vertex>();
		// Intersection points are defined as feature points
		fPoints.addAll(F);
		
		Dijkstra dijkstra;
		Set<Vertex> ballVertexes;
		ArrayList<Vertex> tmp;
		
		// Initially it contains all E2 points, step by step points already considered (intersection and inside a ball of E1) are removed
		ArrayList<Vertex> e2residual;
		e2residual = new ArrayList<Vertex>();
		e2residual = (ArrayList<Vertex>) E2.clone();
		
		// Starts removing intersection points
		e2residual.removeAll(F);
		
		// Tries to avoid noise impact over detection, which makes d(E1) and d(E2) functions not having maximums in the same vertexes
		for(Vertex vE1:E1){
			

		// For each point of E1 check if there is almost an E2 point which is inside epsilon distance. 
		// Build the list of points contained in an epsilon radius ball
			dijkstra =  new Dijkstra(mesh,vE1,configObj.getEpsilon()*res.getMaxDistance());
			dijkstra.execute();
			dijkstra.normalizeDistances(res.getMaxDistance());
			ballVertexes = dijkstra.getDistanceMap().keySet();
			
			tmp = new ArrayList<Vertex>();
			for(Vertex vE2:E2){
				if(ballVertexes.contains(vE2)){
					tmp.add(vE2);
					
					// If a point is inside an E1 ball, it should be removed from E2 residual
					e2residual.remove(vE2);
				}
			}
			if(tmp.size() > 0){
				// Check if an intersection point is inside that list. If it happens, it is the winner
				boolean fTakesAll = false;
				for(Vertex f:F){
					if(tmp.contains(f)) fTakesAll = true; 
				}
		
		
				if(!fTakesAll){
					// 	If not, take the point with maximum composite distance from diameter vertexes d(E1)+d(E2)
					Vertex maxVertex = tmp.get(0);
					Double maxDistance = res.getFirstMaximumSetDistanceMap().get(maxVertex)+res.getSecondMaximumSetDistanceMap().get(maxVertex);
					
					Double actualDistance;
					for(Vertex actualVertex:tmp){
						actualDistance = res.getFirstMaximumSetDistanceMap().get(actualVertex)+res.getSecondMaximumSetDistanceMap().get(actualVertex);
						if(actualDistance > maxDistance){
							maxVertex = actualVertex;
							maxDistance = actualDistance;
						}
					}
				
					// 6. Add that point to Feature points list
					if(!fPoints.contains(maxVertex)) fPoints.add(maxVertex);
				}	
			}else{
				// If the point is a single evidence it must be added to featurePoints
				if(!fPoints.contains(vE1)) fPoints.add(vE1);
			}
			for(Vertex v:e2residual)
				if(!fPoints.contains(v)) fPoints.add(v);
		}
		
		res.setFeaturePoints(fPoints);




		
		
		setChanged();
		notifyObservers();
	}


	@Override
	public void reset() {
		fPoints = new ArrayList<Vertex>();
	}
	
	

}
