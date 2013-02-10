package jSkelExtractor.utils;

import soam.Vertex;

public class DistanceMeter {
	
    /**
     * Calculates Square Euclidean Distance between current vertex and a given vertex
     * Added by Luca Bianchi
     */
	public static double squareEuclideanDistance(Vertex v1, Vertex v2){
    	double distance = 0.0;
    	for(int i=0; i<v1.position.length;i++){
    		distance+= ((v1.position[i]-v2.position[i])*(v1.position[i]-v2.position[i]));
    	}
    	if(distance < 0) throw new NumberFormatException();
    	return distance;
    }

    /**
     * Calculates Euclidean Distance between current vertex and a given vertex
     * Added by Luca Bianchi
     */
	public static double euclideanDistance(Vertex v1, Vertex v2){
		return Math.sqrt(squareEuclideanDistance(v1, v2));
	}

}
