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

	/** T if HSL values have been calculated. */
	private boolean m_hsl;

	/** HSL when calculated */
	private double m_h, m_s, m_l;

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
		return (double) (r + g + b) / 3;
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



	/*--------------------------------------------------------------*/
	/*	CODING:	HSL calculations.									*/
	/*--------------------------------------------------------------*/
	/**
	 * See http://www.had2know.com/technology/hsl-rgb-color-converter.html
	 */
	private void calcHSL() {
		if(m_hsl)
			return;

		int um = r; // M = max(r, g, b)
		if(g > um)
			um = g;
		if(b > um)
			um = b;

		int lm = r; // m = min(r, g, b)
		if(g < lm)
			lm = g;
		if(b < lm)
			lm = b;

		//-- Luminance
		m_l = (lm + um) / 510.0;

		//-- Saturation
		double d = (um - lm) / 255.0;
		if(m_l > 0.0)
			m_s = d / (1.0 - Math.abs(2 * m_l - 1));
		else
			m_s = 0.0;

		//-- Hue (chroma).
		m_h = Math.acos((r - 0.5 * g - 0.5 * b) / Math.sqrt(r * r + b * b + g * g - r * g - r * b - g * b)) / Math.PI * 180;
		if(b > g)
			m_h = 360 - m_h;
		m_hsl = true;
	}

	/**
	 * Lightness L (HSL)
	 * @return
	 */
	public double getL() {
		calcHSL();
		return m_l;
	}

	/**
	 * Saturation S (HSL)
	 * @return
	 */
	public double getS() {
		calcHSL();
		return m_s;
	}

	public double getHue() {
		calcHSL();
		return m_h;
	}

	/**
	 * Create an HSB color.
	 * @param h
	 * @param s
	 * @param b2
	 * @return
	 */
	public static CssColor createHSL(double h, double s, double l) {
		double d = s * (1 - Math.abs(2 * l - 1));
		double m = 255 * (l - 0.5 * d);

		double mod2 = h / 60.0;
		int fac = (int) (mod2 / 2);
		mod2 = mod2 - (fac * 2);

		double x = d * (1 - Math.abs(mod2 - 1));

		int r, g, b;
		int sextant = (int) (h / 60);
		switch(sextant){
			default:
				throw new IllegalArgumentException("Bad h=" + h);
			case 0:
				//-- [0..60>
				r = (int) (255 * d + m);
				g = (int) (255 * x + m);
				b = (int) m;
				break;
			case 1:
				//-- [60..120>
				r = (int) (255 * x + m);
				g = (int) (255 * d + m);
				b = (int) m;
				break;

			case 2:
				//-- [120..180>
				r = (int) m;
				g = (int) (255 * d + m);
				b = (int) (255 * x + m);
				break;

			case 3:
				//-- [180..240>
				r = (int) m;
				g = (int) (255 * x + m);
				b = (int) (255 * d + m);
				break;
			case 4:
				//-- [240..300>
				r = (int) (255 * x + m);
				g = (int) m;
				b = (int) (255 * d + m);
				break;

			case 5:
				r = (int) (255 * d + m);
				g = (int) m;
				b = (int) (255 * x + m);
				break;
		}
		return new CssColor(r, g, b);
	}

	public static void main(String[] args) {
		CssColor c = new CssColor(0xcf, 0xcf, 0xfb);
		System.out.println("HSL=" + c.getHue() + ", " + c.getS() + ", " + c.getL());

		//		System.out.println("a=" + new CssColor("#006611").lighter(0.2));
	}



}