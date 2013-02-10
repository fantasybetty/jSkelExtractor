package jSkelExtractor.algorithms.sdskeletrizer;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.text.Position;

import soam.Edge;
import soam.Vertex;

public class ConnectedComponent {
	
	private ArrayList<Vertex> vertexes;
	private Vertex baricenter;
	private int baricenterWeight;
	private ArrayList<Vertex> ancestorsList;
	private ArrayList<Vertex> followersList;
	private boolean closed;
	private ArrayList<Vertex> vertexForBelongingDetection;
	
	public ConnectedComponent(){
		this.baricenter = new Vertex();
		this.ancestorsList = new ArrayList<Vertex>();
		this.followersList = new ArrayList<Vertex>();
		this.vertexes = new ArrayList<Vertex>();
		baricenterWeight = -1;
		this.closed = false;

	}
	
	public void addAncestors(ArrayList<Vertex> ancestors){
		this.ancestorsList.addAll(ancestors);
		Edge edge = null;
		for(Vertex ancestor:ancestors){
			edge = new Edge(ancestor,baricenter);
			baricenter.neighborhood.add(edge);
		}

	}
	
	public void addAncestors(Vertex ancestor){
		this.ancestorsList.add(ancestor);
		Edge edge = new Edge(ancestor,baricenter);
		baricenter.neighborhood.add(edge);

	}

	public void addFollowers(ArrayList<Vertex> followers,boolean closeComponent){
		this.followersList.addAll(followers);
		Edge edge = null;
		for(Vertex follower:followers){
			edge = new Edge(follower,baricenter);
			baricenter.neighborhood.add(edge);
		}
		if(closeComponent) this.close();
	}
	
	public void addFollowers(Vertex follower,boolean closeComponent){
		this.followersList.add(follower);
		Edge edge = new Edge(follower,baricenter);
		baricenter.neighborhood.add(edge);
		if(closeComponent) this.close();
	}

	
	public void addVertexes(ArrayList<Vertex> vertexes){
		this.vertexes.addAll(vertexes);
		this.vertexForBelongingDetection = vertexes;
	}
	
	public void close(){
		if(!this.closed){
			if(vertexes.size()<1) throw new RuntimeException("Unable to close a Connected Component without vertexes");
		
		double[] barPosition = new double[vertexes.get(0).position.length];
		for(Vertex v:vertexes){
			for(int i=0;i<v.position.length;i++){
				barPosition[i] += v.position[i]/vertexes.size(); //faccio una media delle coordinate di tutti i punti
			}
		}
		this.baricenter.position = barPosition;
		this.baricenterWeight = vertexes.size();
		this.vertexes = null;
		this.closed = true;
		}
	}
	
	public int getBaricenterWeight(){
		if(!this.closed) this.close();
		return baricenterWeight;
	}
	
	public Vertex getBaricenter(){
		if(!this.closed) this.close();
		return this.baricenter;
	}
	
	public ArrayList<Vertex> getFollowers(){
		return this.followersList;
	}

	public ArrayList<Vertex> getAncestors(){
		return this.ancestorsList;
	}

//////////////////////TODO: VERIFICARE ////////////////////////////////

	// Uso un trucco: verifico che i nuovi vertici siano nel vicinato dei vertici aggiunti al passo precedente, se ci sono do l'OK
	public boolean contains(ArrayList<Vertex> currentDC) {
		ArrayList<Vertex> neighborhoodList = new ArrayList<Vertex>();
		for(Vertex v:vertexForBelongingDetection){
			for(Vertex nv:v.getNeighbors()){
				if(!neighborhoodList.contains(nv)) neighborhoodList.add(nv); //mi assicuro che non vengano aggiunti duplicati alla lista
			}
			
		}
		return neighborhoodList.containsAll(currentDC);
	}

	public ArrayList<Vertex> getVertexes() {
		return this.vertexes;
	}

}
