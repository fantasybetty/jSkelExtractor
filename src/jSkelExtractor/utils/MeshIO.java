/*  Copyright 2008 Universita' degli Studi di Pavia
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import soam.Mesh;


public class MeshIO {

    /**
     * Export the Mesh to a file in serialized format.
     * 
     * @param mesh
     *            the mesh to export
     * @param file
     *            the file
     */
    public static boolean exportSerialized(Mesh mesh, File file) {

        try {
            ObjectOutputStream s = new ObjectOutputStream(new BufferedOutputStream(
                    new FileOutputStream(file)));
            s.writeObject(mesh);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Export the Mesh to a file in PLY format.
     * 
     * @param mesh
     *            the mesh to export
     * @param file
     *            the file
     */
    public static boolean exportPly(Mesh mesh, File file) {
	return Mesh2PLY.writeMesh(mesh, file);
    }

    /**
     * Import the Mesh from a file in serialized format.
     * 
     * @param mesh
     *            the mesh to export
     * @param file
     *            the file
     */
    public static Mesh importSerialized(File file) {
        Mesh mesh = null;
        
        try {
            ObjectInputStream s = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(file)));
            mesh = (Mesh) s.readObject();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mesh;
    }
}
