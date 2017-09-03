package to.etc.domui.webdriver.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-9-17.
 */
public interface IWebdriverScreenshotHelper {
	/**
	 * Take a screenshot and store it in the specified file.
	 */
	boolean createScreenshot(@Nonnull WebDriverConnector webDriverConnector, @Nonnull File screenshotFile) throws Exception;

	/**
	 * Take a screenshot and return it as a BufferedImage, to play with.
	 */
	@Nullable
	BufferedImage createScreenshot(@Nonnull WebDriverConnector webDriverConnector) throws Exception;
}
