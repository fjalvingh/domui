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

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable color, with methods to create new colors from it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
final public class CssColor {
	static private final Logger LOG = LoggerFactory.getLogger(CssColor.class);

	/** The color returned when a color string cannot be decoded, so that the mistake is visible on screen. */
	static private final int INVALID_COLOR = 0x3388ee;

	final private int m_r;

	final private int m_g;

	final private int m_b;

	/** Hue, 0..360, shared by the HSL and HSV representations. */
	final private double m_h;

	final private double m_hslS;

	final private double m_hslL;

	final private double m_hsvV;

	final private double m_hsvS;

	public CssColor(float rin, float gin, float bin) {
		this((int) (rin + 0.5), (int) (gin + 0.5), (int) (bin + 0.5));
	}

	public CssColor(double rin, double gin, double bin) {
		this((int) (rin + 0.5), (int) (gin + 0.5), (int) (bin + 0.5));
	}

	public CssColor(CssColor in) {
		this(in.m_r, in.m_g, in.m_b);
	}

	/**
	 * Decode a CSS color string: either #rrggbb or the #rgb shorthand, with or without
	 * the leading #. An undecodable string is logged and yields {@link #INVALID_COLOR}.
	 */
	public CssColor(String rgbin) {
		this(decode(rgbin));
	}

	private CssColor(int[] rgb) {
		this(rgb[0], rgb[1], rgb[2]);
	}

	public CssColor(int rin, int gin, int bin) {
		m_r = clamp(rin);
		m_g = clamp(gin);
		m_b = clamp(bin);

		//-- The HSL/HSV form is calculated once, here: the color is immutable so it can never change, and
		//-- calculating it lazily would make this class unsafe to share between threads.
		double r = m_r / 255.0;
		double g = m_g / 255.0;
		double b = m_b / 255.0;
		double min = Math.min(r, Math.min(g, b));
		double max = Math.max(r, Math.max(g, b));
		double d = max - min;

		m_hslL = (max + min) / 2.0;
		m_hsvV = max;

		//-- hsl saturation
		if(max == min)
			m_hslS = 0.0;
		else if(m_hslL > 0.5)
			m_hslS = d / (2.0 - max - min);
		else
			m_hslS = d / (max + min);

		//-- hsv saturation
		m_hsvS = max == 0.0 ? 0.0 : d / max;

		//-- Hue
		if(max == min) {
			m_h = 0.0;
		} else {
			double h;
			if(max == r)
				h = (g - b) / d + (g < b ? 6 : 0);
			else if(max == g)
				h = (b - r) / d + 2;
			else
				h = (r - g) / d + 4;
			m_h = (h / 6.0) * 360;
		}
	}

	static private int clamp(int in) {
		return in < 0 ? 0 : in > 255 ? 255 : in;
	}

	static private int[] decode(String rgbin) {
		String rgb = rgbin.trim();
		if(rgb.startsWith("#"))
			rgb = rgb.substring(1).trim();
		if(rgb.length() == 3) {
			//-- CSS shorthand: #abc is #aabbcc.
			StringBuilder sb = new StringBuilder(6);
			for(int i = 0; i < 3; i++) {
				char c = rgb.charAt(i);
				sb.append(c).append(c);
			}
			rgb = sb.toString();
		}

		int iv = INVALID_COLOR;
		if(rgb.length() != 6 || !isHex(rgb)) {
			LOG.error("Invalid color value: " + rgbin);
		} else {
			iv = Integer.parseInt(rgb, 16);
		}
		return new int[]{(iv >> 16) & 0xff, (iv >> 8) & 0xff, iv & 0xff};
	}

	static private boolean isHex(String in) {
		for(int i = in.length(); --i >= 0; ) {
			if(Character.digit(in.charAt(i), 16) < 0)
				return false;
		}
		return true;
	}

	/**
	 * The colour a CSS value names, or null when the value does not name one: it is null or
	 * blank, one of the keywords that is not a colour (transparent, inherit, currentColor,
	 * initial, unset), a var() reference, a function like rgba() that carries more than a
	 * colour, or simply a typo.
	 *
	 * <p>This is the difference with {@link #CssColor(String)}: that constructor is for a value
	 * which is known to be a colour and turns anything else into a visible wrong one, so the
	 * mistake is noticed. Use this when the value comes from somewhere that may legitimately
	 * hold something else, and leave such a value alone rather than mangling it.</p>
	 */
	@Nullable
	static public CssColor of(@Nullable String color) {
		if(null == color)
			return null;
		String value = color.trim();
		if(value.isEmpty())
			return null;

		Integer named = NAMED_COLORS.get(value.toLowerCase());
		if(null != named) {
			int iv = named.intValue();
			return new CssColor((iv >> 16) & 0xff, (iv >> 8) & 0xff, iv & 0xff);
		}

		String rgb = value.startsWith("#") ? value.substring(1).trim() : value;
		if(rgb.length() != 3 && rgb.length() != 6)
			return null;
		if(!isHex(rgb))
			return null;
		return new CssColor(decode(rgb));
	}

	/**
	 * The CSS colour keywords, as the CSS Color specification defines them. The keywords that
	 * are aliases of one another (grey/gray and their compounds, aqua/cyan, fuchsia/magenta)
	 * are all present, so that a value spelled either way is recognised.
	 */
	static private final Map<String, Integer> NAMED_COLORS = createNamedColors();

	static private Map<String, Integer> createNamedColors() {
		Map<String, Integer> m = new HashMap<>();
		m.put("aliceblue", 0xf0f8ff); m.put("antiquewhite", 0xfaebd7); m.put("aqua", 0x00ffff);
		m.put("aquamarine", 0x7fffd4); m.put("azure", 0xf0ffff); m.put("beige", 0xf5f5dc);
		m.put("bisque", 0xffe4c4); m.put("black", 0x000000); m.put("blanchedalmond", 0xffebcd);
		m.put("blue", 0x0000ff); m.put("blueviolet", 0x8a2be2); m.put("brown", 0xa52a2a);
		m.put("burlywood", 0xdeb887); m.put("cadetblue", 0x5f9ea0); m.put("chartreuse", 0x7fff00);
		m.put("chocolate", 0xd2691e); m.put("coral", 0xff7f50); m.put("cornflowerblue", 0x6495ed);
		m.put("cornsilk", 0xfff8dc); m.put("crimson", 0xdc143c); m.put("cyan", 0x00ffff);
		m.put("darkblue", 0x00008b); m.put("darkcyan", 0x008b8b); m.put("darkgoldenrod", 0xb8860b);
		m.put("darkgray", 0xa9a9a9); m.put("darkgrey", 0xa9a9a9); m.put("darkgreen", 0x006400);
		m.put("darkkhaki", 0xbdb76b); m.put("darkmagenta", 0x8b008b); m.put("darkolivegreen", 0x556b2f);
		m.put("darkorange", 0xff8c00); m.put("darkorchid", 0x9932cc); m.put("darkred", 0x8b0000);
		m.put("darksalmon", 0xe9967a); m.put("darkseagreen", 0x8fbc8f); m.put("darkslateblue", 0x483d8b);
		m.put("darkslategray", 0x2f4f4f); m.put("darkslategrey", 0x2f4f4f); m.put("darkturquoise", 0x00ced1);
		m.put("darkviolet", 0x9400d3); m.put("deeppink", 0xff1493); m.put("deepskyblue", 0x00bfff);
		m.put("dimgray", 0x696969); m.put("dimgrey", 0x696969); m.put("dodgerblue", 0x1e90ff);
		m.put("firebrick", 0xb22222); m.put("floralwhite", 0xfffaf0); m.put("forestgreen", 0x228b22);
		m.put("fuchsia", 0xff00ff); m.put("gainsboro", 0xdcdcdc); m.put("ghostwhite", 0xf8f8ff);
		m.put("gold", 0xffd700); m.put("goldenrod", 0xdaa520); m.put("gray", 0x808080);
		m.put("grey", 0x808080); m.put("green", 0x008000); m.put("greenyellow", 0xadff2f);
		m.put("honeydew", 0xf0fff0); m.put("hotpink", 0xff69b4); m.put("indianred", 0xcd5c5c);
		m.put("indigo", 0x4b0082); m.put("ivory", 0xfffff0); m.put("khaki", 0xf0e68c);
		m.put("lavender", 0xe6e6fa); m.put("lavenderblush", 0xfff0f5); m.put("lawngreen", 0x7cfc00);
		m.put("lemonchiffon", 0xfffacd); m.put("lightblue", 0xadd8e6); m.put("lightcoral", 0xf08080);
		m.put("lightcyan", 0xe0ffff); m.put("lightgoldenrodyellow", 0xfafad2); m.put("lightgray", 0xd3d3d3);
		m.put("lightgrey", 0xd3d3d3); m.put("lightgreen", 0x90ee90); m.put("lightpink", 0xffb6c1);
		m.put("lightsalmon", 0xffa07a); m.put("lightseagreen", 0x20b2aa); m.put("lightskyblue", 0x87cefa);
		m.put("lightslategray", 0x778899); m.put("lightslategrey", 0x778899); m.put("lightsteelblue", 0xb0c4de);
		m.put("lightyellow", 0xffffe0); m.put("lime", 0x00ff00); m.put("limegreen", 0x32cd32);
		m.put("linen", 0xfaf0e6); m.put("magenta", 0xff00ff); m.put("maroon", 0x800000);
		m.put("mediumaquamarine", 0x66cdaa); m.put("mediumblue", 0x0000cd); m.put("mediumorchid", 0xba55d3);
		m.put("mediumpurple", 0x9370db); m.put("mediumseagreen", 0x3cb371); m.put("mediumslateblue", 0x7b68ee);
		m.put("mediumspringgreen", 0x00fa9a); m.put("mediumturquoise", 0x48d1cc); m.put("mediumvioletred", 0xc71585);
		m.put("midnightblue", 0x191970); m.put("mintcream", 0xf5fffa); m.put("mistyrose", 0xffe4e1);
		m.put("moccasin", 0xffe4b5); m.put("navajowhite", 0xffdead); m.put("navy", 0x000080);
		m.put("oldlace", 0xfdf5e6); m.put("olive", 0x808000); m.put("olivedrab", 0x6b8e23);
		m.put("orange", 0xffa500); m.put("orangered", 0xff4500); m.put("orchid", 0xda70d6);
		m.put("palegoldenrod", 0xeee8aa); m.put("palegreen", 0x98fb98); m.put("paleturquoise", 0xafeeee);
		m.put("palevioletred", 0xdb7093); m.put("papayawhip", 0xffefd5); m.put("peachpuff", 0xffdab9);
		m.put("peru", 0xcd853f); m.put("pink", 0xffc0cb); m.put("plum", 0xdda0dd);
		m.put("powderblue", 0xb0e0e6); m.put("purple", 0x800080); m.put("rebeccapurple", 0x663399);
		m.put("red", 0xff0000); m.put("rosybrown", 0xbc8f8f); m.put("royalblue", 0x4169e1);
		m.put("saddlebrown", 0x8b4513); m.put("salmon", 0xfa8072); m.put("sandybrown", 0xf4a460);
		m.put("seagreen", 0x2e8b57); m.put("seashell", 0xfff5ee); m.put("sienna", 0xa0522d);
		m.put("silver", 0xc0c0c0); m.put("skyblue", 0x87ceeb); m.put("slateblue", 0x6a5acd);
		m.put("slategray", 0x708090); m.put("slategrey", 0x708090); m.put("snow", 0xfffafa);
		m.put("springgreen", 0x00ff7f); m.put("steelblue", 0x4682b4); m.put("tan", 0xd2b48c);
		m.put("teal", 0x008080); m.put("thistle", 0xd8bfd8); m.put("tomato", 0xff6347);
		m.put("turquoise", 0x40e0d0); m.put("violet", 0xee82ee); m.put("wheat", 0xf5deb3);
		m.put("white", 0xffffff); m.put("whitesmoke", 0xf5f5f5); m.put("yellow", 0xffff00);
		m.put("yellowgreen", 0x9acd32);
		return Collections.unmodifiableMap(m);
	}

	public int getRed() {
		return m_r;
	}

	public int getGreen() {
		return m_g;
	}

	public int getBlue() {
		return m_b;
	}

	/**
	 * The plain average of the three channels, 0..255. This is a cheap approximation used to
	 * decide whether a color reads as light or dark; use {@link #relativeLuminance()} when an
	 * accurate luminance is needed.
	 */
	public double luminance() {
		return (double) (m_r + m_g + m_b) / 3;
	}

	/**
	 * The WCAG relative luminance of this color, 0 (black) .. 1 (white).
	 * See <a href="https://www.w3.org/TR/WCAG21/#dfn-relative-luminance">the WCAG definition</a>.
	 */
	public double relativeLuminance() {
		return 0.2126 * linearize(m_r) + 0.7152 * linearize(m_g) + 0.0722 * linearize(m_b);
	}

	/**
	 * Undo the sRGB gamma encoding for a single 0..255 channel.
	 */
	static private double linearize(int channel) {
		double c = channel / 255.0;
		return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

	public boolean isLight() {
		return luminance() >= 128;
	}

	public boolean isDark() {
		return luminance() < 128;
	}

	public CssColor brighter(double factor) {
		checkFactor(factor);

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
		checkFactor(factor);
		factor = 1.0 - factor;
		return new CssColor(m_r * factor, m_g * factor, m_b * factor);
	}

	static private void checkFactor(double factor) {
		if(factor < 0.0d || factor > 1.0d)
			throw new IllegalArgumentException("Factor must be 0..1, not " + factor);
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
	 * The WCAG contrast ratio between this color and the other one, 1 (identical) .. 21 (black
	 * against white). The usual thresholds are 4.5 for normal text and 3 for large text.
	 */
	public double luminanceContrast(CssColor other) {
		return luminanceContrast(this, other);
	}

	static public double luminanceContrast(CssColor a, CssColor b) {
		double l1 = a.relativeLuminance();
		double l2 = b.relativeLuminance();
		if(l2 > l1) {
			double t = l1;
			l1 = l2;
			l2 = t;
		}
		return (l1 + 0.05) / (l2 + 0.05);
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
	 * Color contrast indicator, 0 (the same color) .. 1 (black against white).
	 */
	static public double colorContrast(CssColor a, CssColor b) {
		int dr = Math.abs(a.m_r - b.m_r);
		int dg = Math.abs(a.m_g - b.m_g);
		int db = Math.abs(a.m_b - b.m_b);
		return ((double) dr + dg + db) / (3.0 * 255.0);
	}

	@Override
	public String toString() {
		return "#" + StringTool.intToStr(m_r, 16, 2) + StringTool.intToStr(m_g, 16, 2) + StringTool.intToStr(m_b, 16, 2);
	}

	public String det() {
		return toString() + " (lum=" + luminance() + ", bri=" + colorBrightness() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(!(obj instanceof CssColor))
			return false;
		CssColor c = (CssColor) obj;
		return m_r == c.m_r && m_g == c.m_g && m_b == c.m_b;
	}

	@Override
	public int hashCode() {
		return (m_r << 16) | (m_g << 8) | m_b;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	HSL calculations.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Lightness L (HSL)
	 */
	public double getHslL() {
		return m_hslL;
	}

	public double getHsvV() {
		return m_hsvV;
	}

	/**
	 * Saturation S (HSL)
	 */
	public double getHslS() {
		return m_hslS;
	}

	public double getHsvS() {
		return m_hsvS;
	}

	public double getHslH() {
		return m_h;
	}

	public double getHsvH() {
		return m_h;
	}

	/**
	 * Normalize a hue in degrees to [0..360>, so that -30 and 690 both mean 330.
	 */
	static private double normalizeHue(double h) {
		h = h % 360;
		return h < 0 ? h + 360 : h;
	}

	/**
	 * Create a color from HSL, with h in degrees and s and l in 0..1.
	 */
	public static CssColor createHSL(double h, double s, double l) {
		h = normalizeHue(h);

		double d = s * (1 - Math.abs(2 * l - 1));
		double m = 255 * (l - 0.5 * d);
		double mod2 = (h / 60.0) % 2;
		double c = 255 * d;
		double x = c * (1 - Math.abs(mod2 - 1));

		switch((int) (h / 60)) {
			default:
				throw new IllegalStateException("Bad h=" + h);
			case 0:                                    //-- [0..60>
				return new CssColor(c + m, x + m, m);
			case 1:                                    //-- [60..120>
				return new CssColor(x + m, c + m, m);
			case 2:                                    //-- [120..180>
				return new CssColor(m, c + m, x + m);
			case 3:                                    //-- [180..240>
				return new CssColor(m, x + m, c + m);
			case 4:                                    //-- [240..300>
				return new CssColor(x + m, m, c + m);
			case 5:                                    //-- [300..360>
				return new CssColor(c + m, m, x + m);
		}
	}

	/**
	 * HSV calculation with h = [0..360], s and v in 0..1
	 */
	public static CssColor createHSV(double h, double s, double v) {
		double r;
		double g;
		double b;

		h = normalizeHue(h) / 360.0;            // Get hue 0..1

		double i = Math.floor(h * 6); // Sextant
		double f = h * 6 - i; // Mod
		double p = v * (1 - s);
		double q = v * (1 - f * s);
		double t = v * (1 - (1 - f) * s);

		switch((int) i % 6) {        // Per sextant
			default:
				throw new IllegalStateException();

			case 0:
				r = v;
				g = t;
				b = p;
				break;
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

	/**
	 * Create the given number of colors, spread evenly over the hue circle.
	 */
	static public List<CssColor> calculateColors(int count) {
		if(count <= 0)
			return new ArrayList<>(0);
		List<CssColor> list = new ArrayList<>(count);
		double hueStep = 1.0 / count;
		double hue = 0.198765;                        // Arbitrary start, so that the first color is not pure red
		for(int i = 0; i < count; i++) {
			list.add(createHSL(hue * 360, 0.55, 0.85));
			hue = (hue + hueStep) % 1;
		}
		return list;
	}
}
