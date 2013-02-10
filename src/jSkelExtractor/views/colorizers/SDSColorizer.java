package jSkelExtractor.views.colorizers;

import jSkelExtractor.algorithms.sdskeletrizer.SDSResContainer;
import jSkelExtractor.configs.ConfigObj;
import jSkelExtractor.configs.ViewConfigObj;

import java.awt.Color;
import java.util.ArrayList;

import soam.Vertex;

public class SDSColorizer implements IColorizer {

	private SDSResContainer data;
	private ViewConfigObj viewConfigObj;
	
	public SDSColorizer(SDSResContainer data, ViewConfigObj viewConfigObj){
		this.data = data;
		this.viewConfigObj = viewConfigObj;
	}
	
	@Override
	public Color getColorFor(Vertex vertex) {
		this.data = ConfigObj.getInstance().getResContainer();
		this.viewConfigObj = ViewConfigObj.getInstance();

		
		if(viewConfigObj.get(ViewConfigObj.SHOW_FEATURE_POINTS)){
			if(data.getFeaturePoints().contains(vertex)) return Color.YELLOW;
		}		
		if(viewConfigObj.get(ViewConfigObj.SHOW_SECOND_MAX_PTS)){
			if(data.getSecondMaximumSet().contains(vertex)) return Color.CYAN;
		}
		if(viewConfigObj.get(ViewConfigObj.SHOW_FIRST_MAX_PTS)){
			if(data.getFirstMaximumSet().contains(vertex)) return Color.BLUE;
		}
		if(viewConfigObj.get(ViewConfigObj.SHOW_DIAMETER)){
			if(data.getDiameter().contains(vertex)) return Color.MAGENTA;
		}
		if(viewConfigObj.get(ViewConfigObj.SHOW_STARTING_POINT)){
			if(vertex.equals(data.getStartingVertex())) 
				return Color.RED;
		}

		
		//Maps. Mutually Exclusive selections
		if(viewConfigObj.get(ViewConfigObj.SHOW_SECOND_MAX_SET_DM)&&(data.getSecondMaximumSetDistanceMap().get(vertex)!=null)){
			double dist = data.getSecondMaximumSetDistanceMap().get(vertex);
			return 
				(new Color(.9f,1.0f,0.0f,(float)dist));

		}

		if(viewConfigObj.get(ViewConfigObj.SHOW_FIRST_MAX_SET_DM)&&(data.getFirstMaximumSetDistanceMap().get(vertex)!=null)){
			double dist = data.getFirstMaximumSetDistanceMap().get(vertex);
			return 
				(new Color(.6f,1.0f,0.0f,(float)dist));

		}

		if(viewConfigObj.get(ViewConfigObj.SHOW_BELONGING_MAP)&&(data.getFeaturePoints().size()!=0)){
			float index = ((float)data.getVertexFPmatching().get(vertex))/data.getFeaturePoints().size();
			return Color.getHSBColor(index, 1.0f, 1.0f);
		}

		
		if(viewConfigObj.get(ViewConfigObj.SHOW_DISTANCE_MAP)&&(data.getDistanceMap().get(vertex)!=null)){
			double dist = data.getDistanceMap().get(vertex);
			if(dist == 0) return Color.LIGHT_GRAY;

			return 
				(new Color(0.0f,0.0f,(float)dist));

		}

		if(viewConfigObj.get(ViewConfigObj.SHOW_CD)&&(data.getDiscreteContoursList()!=null)){
			ArrayList<ArrayList<Vertex>> contours = data.getDiscreteContoursList();
			float base = 0.0f;
			float step = 1.0f/contours.size();
			for(ArrayList<Vertex> contour:contours){
				if(contour.contains(vertex)) return Color.getHSBColor(base, 1.0f, 1.0f);
				base+=step;
			}
		}
		
		
		return Color.DARK_GRAY;
	}

}
