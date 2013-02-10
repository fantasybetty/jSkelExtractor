package jSkelExtractor.algorithms;

import soam.Vertex;

public class DiamContainer {
	
	private Vertex v2;
	private Vertex v1;
	private double diameter;
	
	public DiamContainer(double diameter,Vertex v1, Vertex v2){
		this.diameter = diameter;
		this.v1 = v1;
		this.v2 = v2;
	}

	

	public Vertex getV2() {
		return v2;
	}



	public void setV2(Vertex v2) {
		this.v2 = v2;
	}



	public Vertex getV1() {
		return v1;
	}



	public void setV1(Vertex v1) {
		this.v1 = v1;
	}



	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}
	
	

}
