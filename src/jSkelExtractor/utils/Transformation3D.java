/**
 *  Copyright 2009 Universita' degli Studi di Pavia
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

/**
 * A 3D matrix object that can transform sets of 3D points and perform a variety
 * of manipulations on the transform
 */
public class Transformation3D {
    double xx, xy, xz;
    double yx, yy, yz;
    double zx, zy, zz;

    /** Create a new unit matrix */
    public Transformation3D() {
	xx = 1.0d;
	yy = 1.0d;
	zz = 1.0d;
    }

    /** Scale by f in all dimensions */
    public void scale(double f) {
	xx *= f;
	xy *= f;
	xz *= f;
	yx *= f;
	yy *= f;
	yz *= f;
	zx *= f;
	zy *= f;
	zz *= f;
    }

    /** Scale along each axis independently */
    public void scale(double xf, double yf, double zf) {
	xx *= xf;
	xy *= xf;
	xz *= xf;
	yx *= yf;
	yy *= yf;
	yz *= yf;
	zx *= zf;
	zy *= zf;
	zz *= zf;
    }

    /** rotate theta radians about the x axis */
    public void xRotate(double theta) {
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	double Nxy = (double) (xy * ct + xz * st);
	double Nyy = (double) (yy * ct + yz * st);
	double Nzy = (double) (zy * ct + zz * st);

	double Nxz = (double) (xz * ct - xy * st);
	double Nyz = (double) (yz * ct - yy * st);
	double Nzz = (double) (zz * ct - zy * st);

	xy = Nxy;
	yy = Nyy;
	zy = Nzy;
	xz = Nxz;
	yz = Nyz;
	zz = Nzz;
    }

    /** rotate theta radians about the y axis */
    public void yRotate(double theta) {
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	double Nxx = (double) (xx * ct - xz * st);
	double Nyx = (double) (yx * ct - yz * st);
	double Nzx = (double) (zx * ct - zz * st);

	double Nxz = (double) (xx * st + xz * ct);
	double Nyz = (double) (yx * st + yz * ct);
	double Nzz = (double) (zx * st + zz * ct);

	xx = Nxx;
	yx = Nyx;
	zx = Nzx;
	xz = Nxz;
	yz = Nyz;
	zz = Nzz;
    }

    /** rotate theta radians about the z axis */
    public void zRotate(double theta) {
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	double Nxx = (double) (xx * ct + xy * st);
	double Nyx = (double) (yx * ct + yy * st);
	double Nzx = (double) (zx * ct + zy * st);

	double Nxy = (double) (xy * ct - xx * st);
	double Nyy = (double) (yy * ct - yx * st);
	double Nzy = (double) (zy * ct - zx * st);

	xx = Nxx;
	yx = Nyx;
	zx = Nzx;
	xy = Nxy;
	yy = Nyy;
	zy = Nzy;
    }

    /** Multiply this matrix by a second: M = M*R */
    public void multiply(Transformation3D rhs) {
	double lxx = xx * rhs.xx + yx * rhs.xy + zx * rhs.xz;
	double lxy = xy * rhs.xx + yy * rhs.xy + zy * rhs.xz;
	double lxz = xz * rhs.xx + yz * rhs.xy + zz * rhs.xz;

	double lyx = xx * rhs.yx + yx * rhs.yy + zx * rhs.yz;
	double lyy = xy * rhs.yx + yy * rhs.yy + zy * rhs.yz;
	double lyz = xz * rhs.yx + yz * rhs.yy + zz * rhs.yz;

	double lzx = xx * rhs.zx + yx * rhs.zy + zx * rhs.zz;
	double lzy = xy * rhs.zx + yy * rhs.zy + zy * rhs.zz;
	double lzz = xz * rhs.zx + yz * rhs.zy + zz * rhs.zz;

	xx = lxx;
	xy = lxy;
	xz = lxz;

	yx = lyx;
	yy = lyy;
	yz = lyz;

	zx = lzx;
	zy = lzy;
	zz = lzz;
    }

    /**
     * Get Euler angle alpha
     */
    public double getAlpha() {
	return Math.atan2(-zy, zz);
    }

    /**
     * Get Euler angle beta
     */
    public double getBeta() {
	return Math.atan2(zz, Math.sqrt(zx * zx + zy * zy));
    }

    /**
     * Get Euler angle gamma
     */
    public double getGamma() {
	return Math.atan2(yz, xz);
    }

    /** Reinitialize to the unit matrix */
    public void unit() {
	xx = 1;
	xy = 0;
	xz = 0;
	yx = 0;
	yy = 1;
	yz = 0;
	zx = 0;
	zy = 0;
	zz = 1;
    }

    /**
     * Transform point v into tv.
     */
    public void transform(double v[], double tv[]) {
	double x = v[0];
	double y = v[1];
	double z = v[2];

	tv[0] = x * xx + y * xy + z * xz;
	tv[1] = x * yx + y * yy + z * yz;
	tv[2] = x * zx + y * zy + z * zz;
    }

    public String toString() {
	return ("[" + xx + "," + xy + "," + xz + ";" + yx + "," + yy + "," + yz
		+ ";" + zx + "," + zy + "," + zz + "]");
    }
}
