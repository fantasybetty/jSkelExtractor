package jSkelExtractor.views.colorizers;

import jSkelExtractor.algorithms.sdskeletrizer.MeshGraph;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.configs.ViewConfigObj;

import java.awt.Color;
import java.util.ArrayList;

import soam.Vertex;

public class GraphColorizer implements IColorizer {

	private ViewConfigObj viewConfigObj;
	private MeshGraph graph;
	
	public GraphColorizer(MeshGraph graph,ViewConfigObj configObj){
		this.graph = graph;
		this.viewConfigObj = configObj;
	}
	
	@Override
	public Color getColorFor(Vertex vertex) {

		
		return Color.LIGHT_GRAY;
	}

}
