/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.themes;

import javax.annotation.concurrent.*;

import to.etc.util.*;

/**
 * A color, with methods to create new colors from it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
@Immutable
final public class CssColor {
	final int r;

	final int g;

	final int b;

	public CssColor(double rin, double gin, double bin) {
		this((int) (rin + 0.5), (int) (gin + 0.5), (int) (bin + 0.5));
	}

	public CssColor(int rin, int gin, int bin) {
		r = rin < 0 ? 0 : rin > 255 ? 255 : rin;
		g = gin < 0 ? 0 : gin > 255 ? 255 : gin;
		b = bin < 0 ? 0 : bin > 255 ? 255 : bin;
	}

	public CssColor(CssColor in) {
		g = in.g;
		r = in.r;
		b = in.b;
	}

	public CssColor(String rgbin) {
		int iv;
		String rgb = rgbin.trim();
		if(rgb.startsWith("#"))
			rgb = rgb.substring(1).trim();
		try {
			iv = Integer.parseInt(rgb, 16);
		} catch(Exception x) {
			System.out.println("Invalid color value: " + rgbin);
			iv = 0x3388ee; // Invalid color signal value.
		}

		r = (iv >> 16) & 0xff;
		g = (iv >> 8) & 0xff;
		b = (iv & 0xff);
	}

	public double luminance() {
		//			return 0.2126 * r + 0.7152 * g + 0.0722 * b;
		return (r + g + b) / 3;
	}

	public boolean isLight() {
		return luminance() >= 128;
	}

	public boolean isDark() {
		return luminance() < 128;
	}

	public CssColor brighter(double factor) {
		if(factor < 0.0d || factor > 1.0d)
			throw new IllegalStateException("Factor must be 0..1");

		//-- How much room is there to get brighter?
		int dr = 255 - r;
		int dg = 255 - g;
		int db = 255 - b;

		return new CssColor(r + dr * factor, g + dg * factor, b + db * factor);
	}

	public CssColor lighter(double factor) {
		return brighter(factor);
	}

	public CssColor darker(double factor) {
		if(factor < 0.0 || factor >= 1.0)
			throw new IllegalStateException("Factor must be 0..1");
		factor = 1.0 - factor;
		return new CssColor(r * factor, g * factor, b * factor);
	}

	public CssColor inverse() {
		return new CssColor(255 - r, 255 - g, 255 - b);
	}

	/**
	 * Make a dark color lighter. Make a light color darker.
	 */
	public CssColor lume(double factor) {
		if(isLight())
			return darker(factor);
		else
			return lighter(factor);
	}


	/**
	 * See:
	 * http://juicystudio.com/article/luminositycontrastratioalgorithm.php
	 * @param other
	 * @return
	 */
	public double luminanceContrast(CssColor other) {
		return luminanceContrast(this, other);
	}

	static public double luminanceContrast(CssColor a, CssColor b) {
		double l1 = a.luminance();
		double l2 = b.luminance();
		if(l2 > l1) {
			double t = l1;
			l1 = l2;
			l2 = t;
		}
		return (l1 + 0.5) / (l2 + 0.5);
	}

	public double brightnessDifference(CssColor col) {
		return brightnessDifference(this, col);
	}

	static public double brightnessDifference(CssColor a, CssColor b) {
		double ba = a.colorBrightness();
		double bb = b.colorBrightness();
		return ba > bb ? ba - bb : bb - ba;
	}

	/**
	 * Relative brightness from 0..1
	 * @return
	 */
	public double colorBrightness() {
		return (r * 299.0 + g * 587.0 + b * 114.0) / (255000);
	}

	public double colorContrast(CssColor col) {
		return colorContrast(this, col);
	}

	/**
	 * color contrast indicator.
	 * @param a
	 * @param b
	 * @return
	 */
	static public double colorContrast(CssColor a, CssColor b) {
		int dr = Math.abs(a.r - b.r);
		int dg = Math.abs(a.g - b.g);
		int db = Math.abs(a.b - b.b);
		return ((double) dr + (double) dg + db) / (3.0 * 256.0);
	}

	@Override
	public String toString() {
		return "#" + StringTool.intToStr(r, 16, 2) + StringTool.intToStr(g, 16, 2) + StringTool.intToStr(b, 16, 2);
	}

	public String det() {
		return toString() + " (lum=" + luminance() + ", bri=" + colorBrightness() + ")";
	}


	public static void main(String[] args) {
		System.out.println("a=" + new CssColor("#006611").lighter(0.2));

	}
}