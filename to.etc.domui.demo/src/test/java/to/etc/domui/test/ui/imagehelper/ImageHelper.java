package to.etc.domui.test.ui.imagehelper;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.webdriver.core.ScreenInspector;
import to.etc.domui.webdriver.core.WebDriverConnector;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 31-8-17.
 */
final public class ImageHelper {
	private void ImageHelper() {}

	public static void checkBaseLine(String baseName, WebDriverConnector wd, By first, By second, By whole) throws Exception {
		WebElement one = wd.getElement(first);
		WebElement two = wd.getElement(second);
		WebElement all = wd.getElement(whole);

		ScreenInspector si = wd.screenInspector();
		if(null == si) {
			return;
		}
		BufferedImage ssOne = si.elementScreenshot(one);
		int blOne = findBaseLine(ssOne);
		saveBi(ssOne, blOne, "test-one.png");

		int blOneAbs = one.getLocation().y + blOne;

		BufferedImage ssTwo = si.elementScreenshot(two);
		int blTwo = findBaseLine(ssTwo);
		int blTwoAbs = two.getLocation().y + blTwo;

		saveBi(ssTwo, blTwo, "test-two.png");

		if(blOneAbs == blTwoAbs)
			return;

		//-- Create an image showing the problem
		BufferedImage biAll = si.elementScreenshot(all);
		int relOne = all.getLocation().y - one.getLocation().y + blOne;
		int relTwo = all.getLocation().y - two.getLocation().y + blTwo;

		Graphics2D graphics = (Graphics2D) biAll.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, relOne, biAll.getWidth()-1, relOne);

		graphics.setColor(Color.GREEN);
		graphics.drawLine(0, relTwo, biAll.getWidth()-1, relTwo);
		graphics.dispose();

		File output = new File(baseName + "-baseline.png");
		ImageIO.write(biAll, "png", output);

		Assert.fail("The baseline for the first element is " + distance(blOneAbs, blTwoAbs) + " the second - see " + output);
	}

	public static void checkBaseLine(WebDriverConnector wd, ScreenInspector si, String testID, String componentInputCSS) throws Exception {
		WebElement comp = wd.getElement(wd.byId(testID, componentInputCSS));
		WebElement all = getParentTR(comp, "ui-f4-row");
		if(null == all) {
			Assert.assertNotNull("The form's parent row cannot be located for testid " + testID);
			return;
		}

		WebElement label = all.findElement(By.tagName("label"));

		BufferedImage ssOne = si.elementScreenshot(comp);
		int blOne = findBaseLine(ssOne);
		saveBi(ssOne, blOne, "test-one.png");

		int blOneAbs = comp.getLocation().y + blOne;

		BufferedImage ssTwo = si.elementScreenshot(label);
		int blTwo = findBaseLine(ssTwo);
		int blTwoAbs = label.getLocation().y + blTwo;

		saveBi(ssTwo, blTwo, "test-two.png");

		if(blOneAbs == blTwoAbs)
			return;

		//-- Create an image showing the problem
		BufferedImage biAll = si.elementScreenshot(all);
		int relOne = all.getLocation().y - comp.getLocation().y + blOne;
		int relTwo = all.getLocation().y - label.getLocation().y + blTwo;

		Graphics2D graphics = (Graphics2D) biAll.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, relOne, biAll.getWidth()-1, relOne);

		graphics.setColor(Color.GREEN);
		graphics.drawLine(0, relTwo, biAll.getWidth()-1, relTwo);
		graphics.dispose();

		File output = new File("/tmp/xxx-baseline.png");
		ImageIO.write(biAll, "png", output);

		Assert.fail("The baseline for the first element is " + distance(blOneAbs, blTwoAbs) + " the second - see " + output);
	}


	/**
	 * Find the TR on the vertical form that contains the element specified by testid.
	 */
	public static WebElement getParentTR(@Nonnull WebElement comp, @Nonnull String needClass) {
		WebElement current = comp;
		for(;;) {
			if(current == null) {
				return null;
			}
			current = current.findElement(By.xpath(".."));			// Get parent
			if(null == current) {
				return null;
			}
			if(current.getTagName().equalsIgnoreCase("tr")) {
				String clz = current.getAttribute("class");
				if(null != clz) {
					for(String s : clz.split("\\s+")) {
						if(s.equals(needClass)) {
							return current;
						}
					}

				}
			}
		}
	}





	private static void saveBi(BufferedImage biAll, int relOne, String s) throws IOException {
		Graphics2D graphics = (Graphics2D) biAll.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, relOne, biAll.getWidth()-1, relOne);
		ImageIO.write(biAll, "png", new File("/tmp/" + s));
	}

	public static String distance(int one, int two) {
		if(one > two) {
			return (one - two) + "px below";
		} else {
			return (two - one) + "px above";
		}
	}

	public static int findBaseLine(BufferedImage srcBi) {
		ByteImage bi = ByteImage.create(srcBi);
		ByteImage borderBi = bi.stripBorder();

		int[] bl = borderBi.findFontBaselines();

		int sy = borderBi.getRootLocation().y + bl[0];
		int ey = borderBi.getRootLocation().y + bl[1];
		return ey;
	}

	static public void main(String[] args) throws Exception {
		BufferedImage srcBi = ImageIO.read(new File("/tmp/input-1.png"));
		ByteImage bi = ByteImage.create(srcBi);

		ByteImage borderBi = bi.stripBorder();

		int[] bl = borderBi.findFontBaselines();

		int sy = borderBi.getRootLocation().y + bl[0];
		int ey = borderBi.getRootLocation().y + bl[1];

		BufferedImage outBi = borderBi.save();
		ImageIO.write(outBi, "png", new File("/tmp/o1.png"));

		Graphics2D graphics = (Graphics2D) srcBi.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, ey, srcBi.getWidth()-1, ey);
		graphics.dispose();

		ImageIO.write(srcBi, "png", new File("/tmp/o2.png"));
	}
}
