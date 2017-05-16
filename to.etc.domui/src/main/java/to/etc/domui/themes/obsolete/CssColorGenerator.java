package to.etc.domui.themes.obsolete;

import to.etc.domui.themes.*;

/**
 * Utility class to generate all kinds of colors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 20, 2012
 */
public class CssColorGenerator {
	private int m_index;

	static private final int COL_1 = 16;

	static private final int LUMD = 3;

	static private final int SATD = 3;

	public CssColor createColor() {
		int c = m_index++;

		//-- We handle 10*color, 2 luminance levels (0.5, 1.0) and 2 saturation levels (0.5, 1.0) total of 40 colors @ the 1st ring
		if(c < COL_1 * LUMD * SATD) {
			double color = (c % COL_1) * (360.0/COL_1);					// Color divider
			int r = c / COL_1;

			double sat = 1.0 - (r % SATD) * (1.0 / SATD);
			double lum = 1.0 - (r / SATD) * (1.0 / LUMD);

			System.out.println("color: " + color + ", sat=" + sat + ", lum=" + lum);

			return CssColor.createHSV(color, sat, lum);

			/*
			 * 0..9:		color, 1.0, 1.0;
			 * 10..19:		color, 0.5, 1.0
			 * 20..29:		color, 1.0, 0.5
			 * 30..39:		color, 0.5, 0.5
			 */
		}

		throw new IllegalStateException("Out of unique colors 8-/");
	}

}
