/**
 * This file is part of the Abeel Java Toolkit (AJT)
 * 
 * The Abeel Java Toolkit (AJT) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * The Abeel Java Toolkit (AJT) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Abeel Java Toolkit (AJT); if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Copyright (c) 2006-2008, Thomas Abeel
 * 
 * Project: http://sourceforge.net/projects/ajt/
 * 
 */

/** Adapted by pmarco */

package jSkelExtractor.utils;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class provides utility methods to export objects that implement the
 * Component to files.
 * 
 * 
 * 
 * @author Thomas Abeel
 * 
 */
public class GraphicsFileExport {

    private static void imageIOExport(String type, Component d, File file,
            int x, int y) throws IOException {

        BufferedImage bi = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        d.paint(g);
        try {
            ImageIO.write(bi, type, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Export the Component to a file in PNG format.
     * 
     * @param c
     *            the object to export
     * @param fileName
     *            the filename of the file
     * @param x
     *            the width in pixels
     * @param y
     *            the height in pixels
     */
    public static boolean exportPNG(Component c, File file, int x, int y) {
        try {
            imageIOExport("PNG", c, file, x, y);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * Export the Component to a file in EPS format.
     * 
     * @param c
     *            the object to export
     * @param fileName
     *            the filename of the file
     * @param x
     *            the width in pixels
     * @param y
     *            the height in pixels
     */
    public static boolean exportEPS(Component c, File file, int x, int y) {

        try {
            EpsGraphics g = new EpsGraphics("GraphicsExportFactory",
                    new FileOutputStream(file), 0, 0, x, y,
                    EpsGraphics.ColorMode.COLOR_CMYK);
            c.paint(g);
            g.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
