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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A color, with methods to create new colors from it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
final public class CssColor {
	static private final Logger LOG = LoggerFactory.getLogger(CssColor.class);

	final private int m_r;

	final private int m_g;

	final private int m_b;

	/** T if HSL values have been calculated. */
	private boolean m_hsl;

	/** HSL when calculated */
	private double m_h, m_hsl_s, m_hsl_l, m_hsv_v, m_hsv_s;

	public CssColor(float rin, float gin, float bin) {
		this((int) (rin + 0.5), (int) (gin + 0.5), (int) (bin + 0.5));
	}

	public CssColor(double rin, double gin, double bin) {
		this((int) (rin + 0.5), (int) (gin + 0.5), (int) (bin + 0.5));
	}

	public CssColor(int rin, int gin, int bin) {
		m_r = rin < 0 ? 0 : rin > 255 ? 255 : rin;
		m_g = gin < 0 ? 0 : gin > 255 ? 255 : gin;
		m_b = bin < 0 ? 0 : bin > 255 ? 255 : bin;
	}

	public CssColor(CssColor in) {
		m_g = in.m_g;
		m_r = in.m_r;
		m_b = in.m_b;
	}

	public CssColor(String rgbin) {
		int iv;
		String rgb = rgbin.trim();
		if(rgb.startsWith("#"))
			rgb = rgb.substring(1).trim();
		try {
			iv = Integer.parseInt(rgb, 16);
		} catch(Exception x) {
			LOG.error("Invalid color value: " + rgbin);
			iv = 0x3388ee; // Invalid color signal value.
		}

		m_r = (iv >> 16) & 0xff;
		m_g = (iv >> 8) & 0xff;
		m_b = (iv & 0xff);
	}

	public double luminance() {
		//			return 0.2126 * r + 0.7152 * g + 0.0722 * b;
		return (double) (m_r + m_g + m_b) / 3;
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
		int dr = 255 - m_r;
		int dg = 255 - m_g;
		int db = 255 - m_b;

		return new CssColor(m_r + dr * factor, m_g + dg * factor, m_b + db * factor);
	}

	public CssColor lighter(double factor) {
		return brighter(factor);
	}

	public CssColor darker(double factor) {
		if(factor < 0.0 || factor >= 1.0)
			throw new IllegalStateException("Factor must be 0..1");
		factor = 1.0 - factor;
		return new CssColor(m_r * factor, m_g * factor, m_b * factor);
	}

	public CssColor inverse() {
		return new CssColor(255 - m_r, 255 - m_g, 255 - m_b);
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
	 * Create a more color-saturated version of the color.
	 */
	public CssColor saturate(double factor) {
		double s = getHsvS();
		s = s * factor;
		if(s > 1.0)
			s = 1.0;
		return createHSV(getHsvH(), s, getHsvV());
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
	 */
	public double colorBrightness() {
		return (m_r * 299.0 + m_g * 587.0 + m_b * 114.0) / (255000);
	}

	public double colorContrast(CssColor col) {
		return colorContrast(this, col);
	}

	/**
	 * color contrast indicator.
	 */
	static public double colorContrast(CssColor a, CssColor b) {
		int dr = Math.abs(a.m_r - b.m_r);
		int dg = Math.abs(a.m_g - b.m_g);
		int db = Math.abs(a.m_b - b.m_b);
		return ((double) dr + (double) dg + db) / (3.0 * 256.0);
	}

	@Override
	public String toString() {
		return "#" + StringTool.intToStr(m_r, 16, 2) + StringTool.intToStr(m_g, 16, 2) + StringTool.intToStr(m_b, 16, 2);
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
	private void calcHSLOld() {
		if(m_hsl)
			return;

		int um = m_r; // M = max(r, g, b)
		if(m_g > um)
			um = m_g;
		if(m_b > um)
			um = m_b;

		int lm = m_r; // m = min(r, g, b)
		if(m_g < lm)
			lm = m_g;
		if(m_b < lm)
			lm = m_b;

		//-- Luminance / "Value"
		m_hsl_l = (lm + um) / 510.0;
		m_hsv_v = um / 255.0;
		if(um - lm == 0) {
			//-- Achromatic
			m_hsv_s = m_hsl_s = 0;
			m_h = 0.0;
		} else {
			//-- Saturation
			double d = (um - lm) / 255.0;
			if(m_hsl_l > 0.0)
				m_hsl_s = d / (1.0 - Math.abs(2 * m_hsl_l - 1));
			else
				m_hsl_s = 0.0;

			if(um - lm == 0)
				m_hsv_s = 0.0;
			else
				m_hsv_s = d / m_hsv_v;

			//-- Hue (chroma).
			m_h = Math.acos((m_r - 0.5 * m_g - 0.5 * m_b) / Math.sqrt(m_r * m_r + m_b * m_b + m_g * m_g - m_r * m_g - m_r * m_b - m_g * m_b)) / Math.PI * 180;
			if(m_b > m_g)
				m_h = 360 - m_h;
		}
		m_hsl = true;
	}

	/**
	 * Lightness L (HSL)
	 * @return
	 */
	public double getHslL() {
		calcHSL();
		return m_hsl_l;
	}

	public double getHsvV() {
		calcHSL();
		return m_hsv_v;
	}

	/**
	 * Saturation S (HSL)
	 * @return
	 */
	public double getHslS() {
		calcHSL();
		return m_hsl_s;
	}

	public double getHsvS() {
		calcHSL();
		return m_hsv_s;
	}

	public double getHslH() {
		calcHSL();
		return m_h;
	}

	public double getHsvH() {
		calcHSL();
		return m_h;
	}

	/**
	 * Create an HSL color.
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

	private void calcHSL() {
		if(m_hsl)
			return;
		double r = m_r / 255.0;
		double g = m_g / 255.0;
		double b = m_b / 255.0;

		double min = r;
		if(g < min)
			min = g;
		if(b < min)
			min = b;
		double max = r;
		if(g > max)
			max = g;
		if(b > max)
			max = b;
		m_hsl_l = (max + min) / 2.0;
		m_hsv_v = max;

		double d = max - min;

		//-- hsl saturation
		if(max == min) {
			m_hsl_s = 0;
		} else {
			if(m_hsl_l > 0.5)
				m_hsl_s = d / (2.0 - max - min);
			else
				m_hsl_s = d / (max + min);
		}

		//-- hsv saturation
		m_hsv_s = max == 0.0 ? 0.0 : d / max;

		//-- Hue
		if(max == min) {
			m_h = 0.0;
		} else {
			if(max == r) {
				m_h = (g - b) / d + (g < b ? 6 : 0);
			} else if(max == g) {
				m_h = (b - r) / d + 2;
			} else if(max == b) {
				m_h = (r - g) / d + 4;
			} else
				throw new IllegalStateException();
			m_h = (m_h / 6.0) * 360;
		}
		m_hsl = true;
	}

	/**
	 * HSV calculation with h = [0..360], s and v in 0..1
	 */
	public static CssColor createHSV(double h, double s, double v) {
		double r, g, b;

		h	/= 360.0;			// Get hue 0..1

		double i = Math.floor(h * 6); // Sextant
		double f = h * 6 - i; // Mod
		double p = v * (1 - s);
		double q = v * (1 - f * s);
		double t = v * (1 - (1 - f) * s);

		switch((int)i % 6) {		// Per sextant
			default:
				throw new IllegalStateException();

			case 0: r = v; g = t; b = p; break;
			case 1:
				r = q;
				g = v;
				b = p;
				break;
			case 2:
				r = p;
				g = v;
				b = t;
				break;
			case 3:
				r = p;
				g = q;
				b = v;
				break;
			case 4:
				r = t;
				g = p;
				b = v;
				break;
			case 5:
				r = v;
				g = p;
				b = q;
				break;
		}
		return new CssColor(255 * r, 255 * g, 255 * b);
	}

	public static void main(String[] args) {
		CssColor c = new CssColor(0xce, 0xce, 0xfa);
		System.out.println("HSL=" + c.getHslH() + ", " + c.getHslS() + ", " + c.getHslL());
		System.out.println("HSV=" + c.getHsvH() + ", " + c.getHsvS() + ", " + c.getHsvV());

		//-- Create a thing with higher saturation
		CssColor nw = CssColor.createHSV(c.getHsvH(), 1.0, c.getHsvV());
		System.out.println("new = " + nw);
		System.out.println("HSL=" + nw.getHslH() + ", " + nw.getHslS() + ", " + nw.getHslL());
		System.out.println("HSV=" + nw.getHsvH() + ", " + nw.getHsvS() + ", " + nw.getHsvV());


		//		System.out.println("a=" + new CssColor("#006611").lighter(0.2));
	}

//	/**
//	 * Return n relatively light colors.
//	 */
//	static public List<CssColor> calculateColors() {
//		double golden_ratio_conjugate = 0.618033988749895;
//		double hue = 0.198765;
//		List<CssColor> list = new ArrayList<>();
//		for(int i = 0; i < 32; i++) {
//
//			CssColor cssc = CssColor.createHSL(hue * 360, 0.55, 0.85);
//			list.add(cssc);
////			System.out.println(" css " + cssc.toString());
//
//			hue = hue += golden_ratio_conjugate;
//			hue %= 1;
//		}
//		return list;
//	}

	static public List<CssColor> calculateColors(int count) {
		double golden_ratio_conjugate = 1.0 / count;
		double hue = 0.198765;
		List<CssColor> list = new ArrayList<>(count);
		for(int i = 0; i < count; i++) {

			CssColor cssc = CssColor.createHSL(hue * 360, 0.55, 0.85);
			list.add(cssc);
//			System.out.println(" css " + cssc.toString());

			hue = hue += golden_ratio_conjugate;
			hue %= 1;
		}
		return list;
	}


	private final static float
		U_OFF = .436f,
		V_OFF = .615f;

	private static final long RAND_SEED = 0;

	private static Random rand = new Random(RAND_SEED);

	/*
	 * Returns an array of ncolors RGB triplets such that each is as unique from the rest as possible
	 * and each color has at least one component greater than minComponent and one less than maxComponent.
	 * Use min == 1 and max == 0 to include the full RGB color range.
	 *
	 * Warning: O N^2 algorithm blows up fast for more than 100 colors.
	 */
	public static List<CssColor> generateVisuallyDistinctColors(int ncolors, float minComponent, float maxComponent) {
		if(ncolors > 100)
			throw new IllegalStateException("Too many colors requested");

		rand.setSeed(RAND_SEED); 						// So that we get consistent results for each combination of inputs

		float[][] yuv = new float[ncolors][3];

		// initialize array with random colors
		for(int got = 0; got < ncolors; ) {
			System.arraycopy(randYUVinRGBRange(minComponent, maxComponent), 0, yuv[got++], 0, 3);
		}

		// continually break up the worst-fit color pair until we get tired of searching
		for(int c = 0; c < ncolors * 1000; c++) {
			float worst = 8888;
			int worstID = 0;
			for(int i = 1; i < yuv.length; i++) {
				for(int j = 0; j < i; j++) {
					float dist = sqrdist(yuv[i], yuv[j]);
					if(dist < worst) {
						worst = dist;
						worstID = i;
					}
				}
			}
			float[] best = randYUVBetterThan(worst, minComponent, maxComponent, yuv);
			if(best == null)
				break;
			else
				yuv[worstID] = best;
		}

		List<CssColor> res = new ArrayList<>(ncolors);
		for(int i = 0; i < ncolors; i++) {
			float[] rgb = new float[3];
			yuv2rgb(yuv[i][0], yuv[i][1], yuv[i][2], rgb);
			res.add(new CssColor(rgb[0]*255, rgb[1]*255, rgb[2]*255));
		}

		return res;
	}

	public static void hsv2rgb(float h, float s, float v, float[] rgb) {
		// H is given on [0->6] or -1. S and V are given on [0->1].
		// RGB are each returned on [0->1].
		float m, n, f;
		int i;

		float[] hsv = new float[3];

		hsv[0] = h;
		hsv[1] = s;
		hsv[2] = v;
		System.out.println("H: " + h + " S: " + s + " V:" + v);
		if(hsv[0] == -1) {
			rgb[0] = rgb[1] = rgb[2] = hsv[2];
			return;
		}
		i = (int) (Math.floor(hsv[0]));
		f = hsv[0] - i;
		if(i % 2 == 0)
			f = 1 - f; // if i is even
		m = hsv[2] * (1 - hsv[1]);
		n = hsv[2] * (1 - hsv[1] * f);
		switch(i){
			case 6:
			case 0:
				rgb[0] = hsv[2];
				rgb[1] = n;
				rgb[2] = m;
				break;
			case 1:
				rgb[0] = n;
				rgb[1] = hsv[2];
				rgb[2] = m;
				break;
			case 2:
				rgb[0] = m;
				rgb[1] = hsv[2];
				rgb[2] = n;
				break;
			case 3:
				rgb[0] = m;
				rgb[1] = n;
				rgb[2] = hsv[2];
				break;
			case 4:
				rgb[0] = n;
				rgb[1] = m;
				rgb[2] = hsv[2];
				break;
			case 5:
				rgb[0] = hsv[2];
				rgb[1] = m;
				rgb[2] = n;
				break;
		}
	}


	// From http://en.wikipedia.org/wiki/YUV#Mathematical_derivations_and_formulas
	public static void yuv2rgb(float y, float u, float v, float[] rgb) {
		rgb[0] = 1 * y + 0 * u + 1.13983f * v;
		rgb[1] = 1 * y + -.39465f * u + -.58060f * v;
		rgb[2] = 1 * y + 2.03211f * u + 0 * v;
	}

	public static void rgb2yuv(float r, float g, float b, float[] yuv) {
		yuv[0] = .299f * r + .587f * g + .114f * b;
		yuv[1] = -.14713f * r + -.28886f * g + .436f * b;
		yuv[2] = .615f * r + -.51499f * g + -.10001f * b;
	}

	private static float[] randYUVinRGBRange(float minComponent, float maxComponent) {
		while(true) {
			float y = rand.nextFloat(); // * YFRAC + 1-YFRAC);
			float u = rand.nextFloat() * 2 * U_OFF - U_OFF;
			float v = rand.nextFloat() * 2 * V_OFF - V_OFF;
			float[] rgb = new float[3];
			yuv2rgb(y, u, v, rgb);
			float r = rgb[0], g = rgb[1], b = rgb[2];
			if(0 <= r && r <= 1 &&
				0 <= g && g <= 1 &&
				0 <= b && b <= 1 &&
				(r > minComponent || g > minComponent || b > minComponent) && // don't want all dark components
				(r < maxComponent || g < maxComponent || b < maxComponent)) // don't want all light components

				return new float[]{y, u, v};
		}
	}

	private static float sqrdist(float[] a, float[] b) {
		float sum = 0;
		for(int i = 0; i < a.length; i++) {
			float diff = a[i] - b[i];
			sum += diff * diff;
		}
		return sum;
	}
	//
	//private static double worstFit(Color[] colors) {
	//	float worst = 8888;
	//	float[] a = new float[3], b = new float[3];
	//	for(int i = 1; i < colors.length; i++) {
	//		colors[i].getColorComponents(a);
	//		for(int j = 0; j < i; j++) {
	//			colors[j].getColorComponents(b);
	//			float dist = sqrdist(a, b);
	//			if(dist < worst) {
	//				worst = dist;
	//			}
	//		}
	//	}
	//	return Math.sqrt(worst);
	//}

	private static float[] randYUVBetterThan(float bestDistSqrd, float minComponent, float maxComponent, float[][] in) {
		for(int attempt = 1; attempt < 100 * in.length; attempt++) {
			float[] candidate = randYUVinRGBRange(minComponent, maxComponent);
			boolean good = true;
			for(int i = 0; i < in.length; i++)
				if(sqrdist(candidate, in[i]) < bestDistSqrd)
					good = false;
			if(good)
				return candidate;
		}
		return null; // after a bunch of passes, couldn't find a candidate that beat the best.
	}

	///**
	// * Simple example program.
	// */
	//public static void main(String[] args) {
	//	final int ncolors = 10;
	//	List<CssColor> colors = generateVisuallyDistinctColors(ncolors, .8f, .3f);
	//	for(int i = 0; i < colors.size(); i++) {
	//		System.out.println(colors.get(i).toString());
	//	}
	//	//System.out.println("Worst fit color = " + worstFit(colors));
	//}



}
