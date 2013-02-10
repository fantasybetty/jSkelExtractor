package jSkelExtractor.utils;

import java.util.Iterator;

import soam.Mesh;
import soam.Vertex;

public class MeshManipulator {
	
	public static void removeIsolatedVertexes(Mesh mesh){
		
		Iterator<Vertex> vertIterator = mesh.vertexlist.iterator();
		
		Vertex currentVertex;
		while(vertIterator.hasNext()){
			currentVertex = vertIterator.next();
			if(currentVertex.neighborhood.size()<2){
				vertIterator.remove();
				System.out.println(currentVertex+" removed due to lack of connections.");
			}
		}
		
	}

}
