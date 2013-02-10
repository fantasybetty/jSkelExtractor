/**
 *  Copyright 2007 Universita' degli Studi di Pavia
 *  Laboratorio di Visione Artificiale
 *  http://vision.unipv.it
 * 
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.

 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package jSkelExtractor.utils;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import soam.Edge;
import soam.Mesh;
import soam.Vertex;



/**
 * Writes a 3D mesh into a file in Stanford PLY file format.
 * 
 * @author pmarco
 * 
 */

public class Mesh2PLY {
    public static final String FILE_TYPE = "ascii";
    public static final String TEXT_TYPE = "pc";

    /**
     * Writes the output to a filename, in PLY format
     * 
     * @param mesh
     *            The mesh to be written
     * @param file
     *            A valid file name
     * 
     * @return true if the mesh has been successfully written
     */
    public static boolean writeMesh(Mesh mesh, File file) {

	PLY plyObj = new PLY();
	plyObj.setFileType(FILE_TYPE);
	plyObj.setTextType(TEXT_TYPE);

	ArrayList<Float> xValues = new ArrayList<Float>();
	ArrayList<Float> yValues = new ArrayList<Float>();
	ArrayList<Float> zValues = new ArrayList<Float>();

	Vector<List<Integer>> faces = new Vector<List<Integer>>();

	for (int i = 0; i < mesh.vertexlist.size(); i++) {
	    Vertex firstVertex = mesh.vertexlist.get(i);

	    // vertex values
	    xValues.add((float) firstVertex.position[0]);
	    yValues.add((float) firstVertex.position[1]);
	    zValues.add((float) firstVertex.position[2]);

	    // Faces in the star
	    for (int j = 0; j < firstVertex.neighborhood.size(); j++) {
		Vertex secondVertex =
			firstVertex.neighborhood.get(j).getOtherVertex(
				firstVertex);

		int secondIndex = mesh.vertexlist.indexOf(secondVertex);
		if (secondIndex < i) {
		    // The faces in the star have already been written
		    continue;
		}

		for (Edge edge : secondVertex.neighborhood) {
		    Vertex thirdVertex = edge.getOtherVertex(secondVertex);
		    int tmp = neighborhoodIndexOf(thirdVertex, firstVertex);

		    if (tmp < j) {
			// Either it as already been written or is -1 (i.e. not
			// a neighbor)
			continue;
		    }

		    int thirdIndex = mesh.vertexlist.indexOf(thirdVertex);
		    if (thirdIndex < i) {
			// The faces in the star have already been written
			continue;
		    }

		    if (tmp == j + 1) {
			faces.add(Arrays.asList(i, secondIndex, thirdIndex));
		    } else {
			faces.add(Arrays.asList(i, thirdIndex, secondIndex));
		    }
		}
	    }
	}

	try {
	    plyObj.setFile(file);
	    plyObj.addProperty("x", "vertex", xValues);
	    plyObj.addProperty("y", "vertex", yValues);
	    plyObj.addProperty("z", "vertex", zValues);
	    plyObj.addProperty("vertex_index", "face", faces);
	    plyObj.savefile();
	} catch (Exception e) {
	    e.printStackTrace();
	    return false;
	}

	return true;
    }

    protected static int neighborhoodIndexOf(Vertex vertex, Vertex center) {

	for (int i = 0; i < center.neighborhood.size(); i++) {
	    if (vertex == center.neighborhood.get(i).getOtherVertex(center)) {
		return i;
	    }
	}

	return -1;
    }

    /**
     * Approximate 3D orientation test. Nonrobust.</br>
     * 
     * Return a positive value if the point pd lies below the plane passing
     * through pa, pb, and pc; "below" is defined so that pa, pb, and pc appear
     * in counterclockwise order when viewed from above the plane. Returns a
     * negative value if pd lies above the plane. Returns zero if the points are
     * coplanar. The result is also a rough approximation of six times the
     * signed volume of the tetrahedron defined by the four points.
     * 
     * @see <a href="http://www.cs.cmu.edu/~quake/robust.html">
     *      http://www.cs.cmu.edu/~quake/robust.html</a>
     */
    protected static double orient3d(double[] pa, double[] pb, double[] pc,
	    double[] pd) {

	double adx, bdx, cdx;
	double ady, bdy, cdy;
	double adz, bdz, cdz;

	adx = pa[0] - pd[0];
	bdx = pb[0] - pd[0];
	cdx = pc[0] - pd[0];
	ady = pa[1] - pd[1];
	bdy = pb[1] - pd[1];
	cdy = pc[1] - pd[1];
	adz = pa[2] - pd[2];
	bdz = pb[2] - pd[2];
	cdz = pc[2] - pd[2];

	return adx * (bdy * cdz - bdz * cdy) + bdx * (cdy * adz - cdz * ady)
		+ cdx * (ady * bdz - adz * bdy);
    }

}
