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
import java.util.Set;
import java.util.TreeSet;

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
		WebElement row = getParentTR(comp, "ui-f4-row");
		if(null == row) {
			Assert.assertNotNull("The form's parent row cannot be located for testid " + testID);
			return;
		}

		WebElement label = row.findElement(By.tagName("label"));

		BufferedImage ssComponent = si().elementScreenshot(comp);
		int blComp = ImageHelper.findBaseLine(ssComponent);
		saveBi(ssComponent, blComp, "component-screenshot", "The component");

		int blCompAbs = comp.getLocation().y + blComp;

		BufferedImage ssLabel = si().elementScreenshot(label);
		int blLabel = ImageHelper.findBaseLine(ssLabel);
		int blLabelAbs = label.getLocation().y + blLabel;

		saveBi(ssLabel, blLabel, "label-screenshot", "The label");

		if(blCompAbs == blLabelAbs)
			return;

		//-- Create an image showing the problemfamiliar
		BufferedImage biRow = si().elementScreenshot(row);

		BufferedImage r = new BufferedImage(biRow.getWidth(), biRow.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics rg = r.getGraphics();
		rg.drawImage(biRow, 0, 0, biRow.getWidth(), biRow.getHeight(), null);
		rg.dispose();
		biRow = r;

		int relComp = blCompAbs - row.getLocation().y;
		int relLabel = blLabelAbs - row.getLocation().y;

		Graphics2D graphics = (Graphics2D) biRow.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		//graphics.setComposite(AlphaComposite.DstOver);
		graphics.drawLine(label.getSize().width, relComp, biRow.getWidth()-1, relComp);

		graphics.setColor(Color.GREEN);
		graphics.drawLine(0, relLabel, label.getSize().width, relLabel);
		graphics.dispose();

		saveImage(biRow, "baseline", "The baseline between both elements");

		//ImageIO.write(biAll, "png", getSnapshotName("baseline"));

		//-- Dump properties for label and control
		Map<String, String> labelStyles = wd().getComputedStyles(label, a -> ! a.startsWith("-"));
		System.out.println("label styles = " + labelStyles);
		Map<String, String> compStyles = wd().getComputedStyles(comp, a -> ! a.startsWith("-"));
		System.out.println("comp styles = " + compStyles);

		StringBuilder sb = new StringBuilder();
		Set<String> names = new TreeSet<>(compStyles.keySet());
		names.addAll(labelStyles.keySet());
		sb.append(String.format("%-20s %-40s %-40s\n", "css property", "label value", "component value"));
		names.forEach(name -> {
			sb.append(String.format("%-20s %-40s %-40s\n", name, labelStyles.get(name), compStyles.get(name)));
		});
		allureText(sb.toString(), "computed css styles");

		Assert.fail("The baseline for the first element is " + ImageHelper.distance(blCompAbs, blLabelAbs) + " the second");
	}

	static public WebElement findFormLabelFor(WebElement one) {
		WebElement row = AbstractLayoutTest.getParentTR(one, "ui-f4-row");
		if(null == row) {
			Assert.assertNotNull("The form's parent row cannot be located for testid one");
			throw new IllegalStateException();
		}
		WebElement label = row.findElement(By.tagName("label"));
		return label;
	}

	/**
	 * Find the TR on the vertical form that contains the element specified by testid.
	 */
	@Nullable
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

	private void saveBi(BufferedImage biAll, int relOne, String baseName, String desc) throws IOException {
		Graphics2D graphics = (Graphics2D) biAll.getGraphics();
		graphics.setStroke(new BasicStroke(1));
		graphics.setColor(Color.RED);
		graphics.drawLine(0, relOne, biAll.getWidth()-1, relOne);
		saveImage(biAll, baseName, desc);

		//ImageIO.write(biAll, "png", getSnapshotName(baseName));
	}



}
