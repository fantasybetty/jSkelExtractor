package jSkelExtractor.views.colorizers;

import java.awt.Color;
import java.util.ArrayList;

import soam.Vertex;

public class SpecialPointColorizer implements IColorizer {

	private ArrayList<Vertex> specialPoints;
	private boolean markFirst;
	
	public SpecialPointColorizer(ArrayList<Vertex> points, boolean markFirst){
		this.specialPoints = points;
		this.markFirst = markFirst;
	}
	
	@Override
	public Color getColorFor(Vertex vertex) {
		if(markFirst) if(vertex.equals(specialPoints.get(0))) return Color.GREEN;
		for(Vertex v:specialPoints){
			if(vertex.equals(v)) return Color.YELLOW;
		}
		return Color.RED;
	}

}
