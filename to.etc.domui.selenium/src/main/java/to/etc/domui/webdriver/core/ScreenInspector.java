package to.etc.domui.webdriver.core;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

import javax.annotation.DefaultNonNull;
import java.awt.image.BufferedImage;

/**
 * Handles bitmap lookup of components on the test browser's window.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 25-8-17.
 */
@DefaultNonNull
final public class ScreenInspector {
	private final WebDriverConnector m_wd;

	private final BufferedImage m_image;

	public ScreenInspector(WebDriverConnector wd, BufferedImage image) {
		m_wd = wd;
		m_image = image;
	}

	/**
	 * Returns an element's screenshot.
	 */
	public BufferedImage elementScreenshot(WebElement lay) {
		Point location = lay.getLocation();
		Dimension size = lay.getSize();
		return m_image.getSubimage(location.getX(), location.getY(), size.width, size.height);
	}

	/**
	 * Returns the element's screenshot.
	 * @param by
	 * @return
	 */
	public BufferedImage elementScreenshot(By by) {
		WebElement element = m_wd.findElement(by);
		if(null == element)
			throw new ElementNotFoundException(by.toString());
		return elementScreenshot(element);
	}

	/**
	 * Return the element's screenshot.
	 * @param by
	 * @return
	 */
	public BufferedImage elementScreenshot(String by) {
		WebElement element = m_wd.findElement(by);
		if(null == element)
			throw new ElementNotFoundException(by.toString());
		return elementScreenshot(element);
	}

	/**
	 * Get a color histogram for the most used colors.
	 * @param bi
	 * @param max
	 * @return
	 */
	static public int[][] getMostUsedColors(BufferedImage bi, int max) {
		final int bitshft = 3;
		final int bitrest = 8 - bitshft;
		final int bitmask = (1 << bitshft) - 1;
		final int restmask = (1 << bitrest) - 1;

		int percolor = 1 << bitrest;
		int buckets = percolor * percolor * percolor;
		int[] histogram = new int[buckets];

		int width = bi.getWidth();
		int height = bi.getHeight();
		for(int y = height; --y >= 0;) {
			for(int x = width; --x >= 0;) {
				int pixel = bi.getRGB(x, y);
				int b = pixel & 0xff;
				pixel = pixel >> 8;
				int g = pixel & 0xff;
				pixel = pixel >> 8;
				int r = pixel & 0xff;

				int index = ((r & ~bitmask) << (2*bitrest - bitshft))
					| ((g & ~bitmask) << (bitrest - bitshft))
					| ((b & ~bitmask) >> bitshft)
					;
				histogram[index]++;
			}
		}

		//for(int i = 0; i < histogram.length; i++) {
		//	if(histogram[i] > 0) {
		//		System.out.println("$$ " + Integer.toString(i, 16) + "   " + histogram[i]);
		//	}
		//}

		//-- Now get the largest #of colors.
		int[] indexArray = new int[max];
		for(int i = 0; i < buckets; i++) {
			insertBucket(histogram, i, indexArray);
		}

		int[][] result = new int[max][2];
		for(int i = 0; i < indexArray.length; i++) {
			int color = indexArray[i];
			int b = (color & restmask) << bitshft;
			color = color >> bitrest;
			int g = (color & restmask) << bitshft;
			int r = (color >> bitrest) << bitshft;

			result[i][0] = (r << 16) | (g << 8) | b;
			result[i][1] = histogram[indexArray[i]];
		}
		return result;
	}

	private static void insertBucket(int[] histogram, int bucketIndex, int[] indexArray) {
		int cur = histogram[bucketIndex];
		for(int i = 0; i < indexArray.length; i++) {
			if(cur > histogram[indexArray[i]]) {
				System.arraycopy(indexArray, i, indexArray, i+1, indexArray.length - i - 1);
				indexArray[i] = bucketIndex;
				return;
			}
		}
	}
}
