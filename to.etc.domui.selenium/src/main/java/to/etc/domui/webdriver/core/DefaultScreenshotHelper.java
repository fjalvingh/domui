package to.etc.domui.webdriver.core;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;
import to.etc.util.FileTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-9-17.
 */
public class DefaultScreenshotHelper implements IWebdriverScreenshotHelper {
	@Override public boolean createScreenshot(WebDriverConnector wd, File screenshotFile) throws Exception {
		WebDriver ad = wd.driver();

		if(ad instanceof RemoteWebDriver && wd.getDriverType() == WebDriverType.REMOTE) {
			//for remote drivers we need to do augmenter thingy, for local we must not
			ad = new Augmenter().augment(ad);
		}

		if(ad instanceof TakesScreenshot) {
			File f = ((TakesScreenshot) ad).getScreenshotAs(OutputType.FILE);
			try {
				FileTool.copyFile(screenshotFile, f);
			} finally {
				FileTool.closeAll(f);
			}
			return true;
		}
		return false;
	}

	@Nullable @Override public BufferedImage createScreenshot(@Nonnull WebDriverConnector wd) throws Exception {
		WebDriver ad = wd.driver();

		if(ad instanceof RemoteWebDriver && wd.getDriverType() == WebDriverType.REMOTE) {
			//for remote drivers we need to do augmenter thingy, for local we must not
			ad = new Augmenter().augment(ad);
		}

		if(ad instanceof TakesScreenshot) {
			File f = ((TakesScreenshot) ad).getScreenshotAs(OutputType.FILE);
			return ImageIO.read(f);
		}
		return null;
	}
}
