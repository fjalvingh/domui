package to.etc.domui.themes;

import to.etc.util.*;

/**
 * This helper class is passed to the theme factory, and can be used to augment
 * information in the style.properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class ThemeCssUtils {
	static public final Col BLACK = new Col(0, 0, 0);

	static public final Col WHITE = new Col(255, 255, 255);

	/**
	 * A color, with methods to create new colors from it.
	 *
	 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
	 * Created on Jan 4, 2011
	 */
	static public class Col {

		int r;

		int g;

		int b;

		private Col() {}

		public Col(int rin, int gin, int bin) {
			r = rin;
			g = gin;
			b = bin;
		}

		public Col(Col in) {
			g = in.g;
			r = in.r;
			b = in.b;
		}

		public Col(String rgbin) {
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

		public void brighter(double factor) {
			if(factor < 1.0)
				factor += 1.0;
			else if(factor >= 2.0)
				throw new IllegalStateException("Factor must be 0..1 or 1.0 .. 2.0");

			r = (int) (r * factor + 0.5);
			g = (int) (g * factor + 0.5);
			b = (int) (b * factor + 0.5);
			truncate();
		}

		public void darker(double factor) {
			if(factor < 0.0 || factor >= 1.0)
				throw new IllegalStateException("Factor must be 0..1");
			factor = 1.0 - factor;
			r = (int) (r * factor + 0.5);
			g = (int) (g * factor + 0.5);
			b = (int) (b * factor + 0.5);
			truncate();
		}

		private void truncate() {
			if(r > 255)
				r = 255;
			if(b > 255)
				b = 255;
			if(g > 255)
				g = 255;
		}

		public Col inverse() {
			Col c = new Col();
			c.r = 255 - r;
			c.g = 255 - g;
			c.b = 255 - b;
			return c;
		}

		/**
		 * See:
		 * http://juicystudio.com/article/luminositycontrastratioalgorithm.php
		 * @param other
		 * @return
		 */
		public double luminanceContrast(Col other) {
			return luminanceContrast(this, other);
		}

		static public double luminanceContrast(Col a, Col b) {
			double l1 = a.luminance();
			double l2 = b.luminance();
			if(l2 > l1) {
				double t = l1;
				l1 = l2;
				l2 = t;
			}
			return (l1 + 0.5) / (l2 + 0.5);
		}

		public double brightnessDifference(Col col) {
			return brightnessDifference(this, col);
		}

		static public double brightnessDifference(Col a, Col b) {
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

		public double colorContrast(Col col) {
			return colorContrast(this, col);
		}

		/**
		 * color contrast indicator.
		 * @param a
		 * @param b
		 * @return
		 */
		static public double colorContrast(Col a, Col b) {
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
	}


}
