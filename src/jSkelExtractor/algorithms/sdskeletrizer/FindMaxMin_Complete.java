package jSkelExtractor.algorithms.sdskeletrizer;

import jSkelExtractor.algorithms.AbstractAlgorithm;
import jSkelExtractor.algorithms.DiamContainer;
import jSkelExtractor.algorithms.Dijkstra;
import jSkelExtractor.configs.ConfigObj;

import java.util.ArrayList;
import java.util.Map;

import soam.Mesh;
import soam.Vertex;

public class FindMaxMin_Complete extends AbstractAlgorithm {

	protected SDSResContainer res;
	protected ArrayList<DiamContainer> diameters;
	
	public FindMaxMin_Complete(Mesh mesh){
		super(mesh,ConfigObj.getInstance());
		this.res = configObj.getResContainer();
		this.diameters = new ArrayList<DiamContainer>();
		this.reset();
	}

	@Override
	public void execute() {
		res.setStartingVertex(mesh.vertexlist.get(configObj.getStartingPoint()));
		Dijkstra dijkstra = new Dijkstra(mesh,res.getStartingVertex(),false);
		
		// Runs Dijkstra a lots of times (one for each vertex) and stores its relative diameter
		Map.Entry<Vertex, Double> diameter;
		for(Vertex v:mesh.vertexlist){
				dijkstra.reset();
				dijkstra.setStartingPoint(v);
				dijkstra.execute();
		
				diameter = dijkstra.getDiameter();
				this.diameters.add(new DiamContainer(diameter.getValue(),v,diameter.getKey()));
				System.out.println("SP: "+v.hashCode()+" Diameter Computed. "+diameter.getValue());
		}
		
		// Search for maximum of diameters
		DiamContainer max = null;
		for(DiamContainer entry:this.diameters){
			if((max == null)||(entry.getDiameter() > max.getDiameter())) max = entry;
		}
		
		//The maximum is the diameter of the mesh
		this.res.setStartingVertex(max.getV1());
		this.res.setDiameterVertex(max.getV1());
		this.res.setDiameterVertex(max.getV2());
		this.res.setMaxDistance(max.getDiameter());
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("Maximum Diameter: "+this.res.getMaxDistance()+ " Vertexes: "+max.getV1()+" "+max.getV2());
		System.out.println("----------------------------------------------------------------------------------------");

		
		setChanged();
		notifyObservers();
	}


	@Override
	public void reset() {
		this.diameters = new ArrayList<DiamContainer>();
		
	}
	
	
}
