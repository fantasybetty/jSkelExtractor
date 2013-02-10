package jSkelExtractor.configs;

import java.util.ArrayList;

import soam.Vertex;
import jSkelExtractor.algorithms.sdskeletrizer.SDSResContainer;

public class ConfigObj {
	
	private int startingPoint;
	private double vertexNeighborhoodDivergence;
	private double epsilon;
	
	
	private SDSResContainer resContainer;

	private boolean loop;
	private boolean isInteractiveExecution;

	
	
	private ConfigObj(){
		this.startingPoint = 0;
		this.vertexNeighborhoodDivergence = 0.0;
		this.epsilon = 0.1;
		this.resContainer = new SDSResContainer();
		
		this.isInteractiveExecution = false;
		this.loop = false;
	}
	
	
	

	private static ConfigObj instance; 
	
	
	public int getStartingPoint() {
		return startingPoint;
	}




	public void setStartingPoint(int startingPoint) {
		this.startingPoint = startingPoint;
	}




	public double getVertexNeighborhoodDivergence() {
		return vertexNeighborhoodDivergence;
	}




	public void setVertexNeighborhoodDivergence(double vertexNeighborhoodDivergence) {
		this.vertexNeighborhoodDivergence = vertexNeighborhoodDivergence;
	}




	public double getEpsilon() {
		return epsilon;
	}




	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}



	

	/**
	 * Implements a Singleton Pattern
	 * @return
	 */
	public static ConfigObj getInstance(){
		if(instance == null){
			instance = new ConfigObj();
		}
		return instance;
	}




	public SDSResContainer getResContainer() {
		return resContainer;
	}



	public boolean isLooping() {
		return this.loop;
	}

	public void setLooping(boolean doLoop){
		this.loop = doLoop;
	}
	
	public boolean isInteractiveExecution(){
		return this.isInteractiveExecution;
	}



	public void setInteractiveExecution(boolean isInteractiveExecution) {
		this.isInteractiveExecution = isInteractiveExecution;
	}
	
}
