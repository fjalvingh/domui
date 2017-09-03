package to.etc.domui.test.ui.componenterrors;

import to.etc.domui.webdriver.core.ScreenInspector;

import java.awt.image.BufferedImage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-8-17.
 */
final public class TestHelper {
	private TestHelper() {
	}


	static public boolean isReddish(BufferedImage bi) {
		int[][] ints = ScreenInspector.getMostUsedColors(bi, 10);
		int pixel = ints[0][0];
		int b = pixel & 0xff;
		pixel = pixel >> 8;
		int g = pixel & 0xff;
		pixel = pixel >> 8;
		int r = pixel & 0xff;

		return r > 0xf0 && b < 0xf0 && g < 0xf0;
	}


}
