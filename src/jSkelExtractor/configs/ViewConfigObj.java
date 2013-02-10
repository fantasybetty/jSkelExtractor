package jSkelExtractor.configs;

import jSkelExtractor.views.colorizers.GraphColorizer;
import jSkelExtractor.views.colorizers.IColorizer;
import jSkelExtractor.views.colorizers.SDSColorizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewConfigObj extends HashMap<String, Boolean>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String SHOW_MESH 				= "showMesh";
	public static final String SHOW_STARTING_POINT 		= "showStartingPoint";
	public static final String SHOW_DIAMETER 			= "showDiameter";
	public static final String SHOW_FIRST_MAX_PTS 		= "showFirstMaxPts";
	public static final String SHOW_FIRST_MAX_SET_DM 	= "firstMaximumSetDistanceMap";
	public static final String SHOW_SECOND_MAX_PTS 		= "showSecondMaxPts";
	public static final String SHOW_SECOND_MAX_SET_DM 	= "secondMaximumSetDistanceMap";
	public static final String SHOW_FEATURE_POINTS 		= "showFeaturePoints";
	public static final String SHOW_BELONGING_MAP 		= "showBelongingMap";
	public static final String SHOW_DISTANCE_MAP 		= "showDistanceMap";
	public static final String SHOW_CD					= "showDiscreteContours"; 
	
	private static ViewConfigObj instance;

	private ArrayList<String> relevance;
	private IColorizer algorithmColorizer;
	private IColorizer graphColorizer;
	
	private ViewConfigObj(){
		super();
		relevance = new ArrayList<String>();

		this.put(SHOW_MESH, true);
		this.put(SHOW_STARTING_POINT, true);
		this.put(SHOW_DIAMETER, true);
		this.put(SHOW_FIRST_MAX_PTS, true);
		this.put(SHOW_FIRST_MAX_SET_DM, false);
		this.put(SHOW_SECOND_MAX_PTS, true);
		this.put(SHOW_SECOND_MAX_SET_DM, false);
		this.put(SHOW_FEATURE_POINTS, true);
		this.put(SHOW_BELONGING_MAP, false);
		this.put(SHOW_DISTANCE_MAP, false);
		this.put(SHOW_CD, true);
		
		
		this.setAlgorithmColorizer(new SDSColorizer(ConfigObj.getInstance().getResContainer(),this));
		this.setGraphColorizer(new GraphColorizer(ConfigObj.getInstance().getResContainer().getGraph(),this));
		
	}
	
	@Override 
	public Boolean put(String key, Boolean value){
		relevance.add(key);
		return super.put(key, value);
	}
	
	@Override
	public Boolean get(Object key){
		Boolean res;
		res = super.get(key);
		if(res == null) {
			this.put((String)key, false);
			return false;
		} else return res;
	}
	
	public Map.Entry<String, Boolean> getIndexedKey(int index){
		String keyName = relevance.get(index);
		for(Map.Entry<String, Boolean> entry:this.entrySet()){
			if(entry.getKey().equals(keyName)) return entry;
		}
		return null;
	}
	

	/**
	 * Implements a Singleton Pattern
	 * @return
	 */
	public static ViewConfigObj getInstance(){
		if(instance == null){
			instance = new ViewConfigObj();
		}
		return instance;
	}

	public void setAlgorithmColorizer(IColorizer algorithmColorizer) {
		this.algorithmColorizer = algorithmColorizer;
	}

	public IColorizer getAlgorithmColorizer() {
		return algorithmColorizer;
	}

	public void setGraphColorizer(IColorizer graphColorizer) {
		this.graphColorizer = graphColorizer;
	}

	public IColorizer getGraphColorizer() {
		return graphColorizer;
	}
}
