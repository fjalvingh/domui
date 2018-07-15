package to.etc.domui.webdriver.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-9-17.
 */
public class ChromeScreenshotHelper implements IWebdriverScreenshotHelper {
	@Override public boolean createScreenshot(WebDriverConnector webDriverConnector, File screenshotFile) throws Exception {
		new ChromeExtender((MyChromeDriver) webDriverConnector.driver()).takeScreenshot(screenshotFile);
		return true;
	}

	@Nullable @Override public BufferedImage createScreenshot(@NonNull WebDriverConnector webDriverConnector) throws Exception {
		return new ChromeExtender((MyChromeDriver) webDriverConnector.driver()).takeScreenshot();
	}
}
