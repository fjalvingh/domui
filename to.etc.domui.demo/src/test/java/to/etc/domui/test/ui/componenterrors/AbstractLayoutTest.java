package to.etc.domui.test.ui.componenterrors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import to.etc.domui.test.ui.imagehelper.ImageHelper;
import to.etc.domui.webdriver.core.AbstractWebDriverTest;
import to.etc.domui.webdriver.core.ScreenInspector;
import to.etc.domui.webdriver.core.WebDriverConnector;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * The static and ThreadLocal idiocy is needed because of JUnit's horrible behavior
 * of reallocating a new test class for every test.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-9-17.
 */
@DefaultNonNull
abstract public class AbstractLayoutTest extends AbstractWebDriverTest {
	static private ThreadLocal<ScreenInspector> m_inspector = new ThreadLocal<>();

	protected abstract void initializeScreen() throws Exception;

	@AfterClass
	static public void releaseInspector() {
		if(m_inspector.get() != null) {
			System.out.println("AbstractLayoutTest: releasing inspector");
		}
		m_inspector.set(null);
	}

	@Before
	public void setUpForm() throws Exception {
		si();
	}

	protected final ScreenInspector si() throws Exception {
		ScreenInspector si = m_inspector.get();
		if(null == si) {
			initializeScreen();

			si =  wd().screenInspector();
			if(null == si) {
				Assume.assumeTrue("Cannot take screenshots", false);
				throw new IllegalStateException();								// Satisfy nullchecking
			} else {
				saveImage(si.getScreenImage(), "screenInspector", "Inspected screen");
				//si.save(getSnapshotName());
			}
			m_inspector.set(si);

		}
		return si;
	}

	public void checkBaseLine(@Nonnull String testID, @Nonnull String componentInputCSS) throws Exception {
		si();											// Make sure screen is open
		WebDriverConnector wd = wd();
		WebElement comp = wd.getElement(wd.byId(testID, componentInputCSS));
		WebElement all = getParentTR(comp, "ui-f4-row");
		if(null == all) {
			Assert.assertNotNull("The form's parent row cannot be located for testid " + testID);
			return;
		}

		WebElement label = all.findElement(By.tagName("label"));

		BufferedImage ssOne = si().elementScreenshot(comp);
		int blOne = ImageHelper.findBaseLine(ssOne);
		saveBi(ssOne, blOne, "label-screenshot", "The label");

		int blOneAbs = comp.getLocation().y + blOne;

		BufferedImage ssTwo = si().elementScreenshot(label);
		int blTwo = ImageHelper.findBaseLine(ssTwo);
		int blTwoAbs = label.getLocation().y + blTwo;

		saveBi(ssTwo, blTwo, "component-screenshot", "The component");

		if(blOneAbs == blTwoAbs)
			return;

		//-- Create an image showing the problemfamiliar
		BufferedImage biAll = si().elementScreenshot(all);
		int relOne = all.getLocation().y - comp.getLocation().y + blOne;
		int relTwo = all.getLocation().y - label.getLocation().y + blTwo;

		Graphics2D graphics = (Graphics2D) biAll.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, relOne, biAll.getWidth()-1, relOne);

		graphics.setColor(Color.GREEN);
		graphics.drawLine(0, relTwo, biAll.getWidth()-1, relTwo);
		graphics.dispose();

		saveImage(biAll, "baseline", "The baseline between both elements");

		//ImageIO.write(biAll, "png", getSnapshotName("baseline"));

		//-- Dump properties for label and control
		Map<String, String> styles = wd().getComputedStyles(label, a -> ! a.startsWith("-"));
		System.out.println("label styles = " + styles);
		styles = wd().getComputedStyles(comp, a -> ! a.startsWith("-"));
		System.out.println("comp styles = " + styles);

		Assert.fail("The baseline for the first element is " + ImageHelper.distance(blOneAbs, blTwoAbs) + " the second");
	}

	/**
	 * Find the TR on the vertical form that contains the element specified by testid.
	 */
	@Nullable
	private static WebElement getParentTR(@Nonnull WebElement comp, @Nonnull String needClass) {
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

	private void saveBi(BufferedImage biAll, int relOne, String baseName, String desc) throws IOException {
		Graphics2D graphics = (Graphics2D) biAll.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, relOne, biAll.getWidth()-1, relOne);
		saveImage(biAll, baseName, desc);

		//ImageIO.write(biAll, "png", getSnapshotName(baseName));
	}



}
