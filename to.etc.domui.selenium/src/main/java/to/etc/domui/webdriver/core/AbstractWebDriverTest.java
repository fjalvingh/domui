package to.etc.domui.webdriver.core;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.UnhandledAlertException;
import ru.yandex.qatools.allure.annotations.Attachment;
import to.etc.pater.OnTestFailure;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

/**
 * Abstract base class for Selenium JUnit tests.
 */
@DefaultNonNull
abstract public class AbstractWebDriverTest {
	@Rule
	public TestName m_testName = new TestName();

	@Rule
	public JUnitOnFailedRule m_failRule = new JUnitOnFailedRule(this);

	@Nullable
	private WebDriverConnector m_wd;

	private int m_screenShotCount;

	/**
	 * Get the webdriver to use for tests.
	 * @return
	 */
	@Nonnull
	final protected WebDriverConnector wd() {
		WebDriverConnector wd = m_wd;
		if(null == wd) {
			try {
				m_wd = wd = WebDriverConnector.get();
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
		return wd;
	}

	protected WebDriverCommandBuilder c() {
		return wd().cmd();
	}

	@Nonnull
	public static String prefix(int len) {
		return StringTool.getRandomStringWithPrefix(len, "wd_");
	}

	/**
	 * EXPERIMENTAL: this method gets called by the "new" test runner if a test has failed. The implementation
	 * uses the "test report" mechanism to register screenshots with the test runner so that they can be
	 * part of the result report.
	 * @param failedMethod
	 * @throws Exception
	 */
	@OnTestFailure
	public void onTestFailure(Method failedMethod) throws Exception {
		WebDriverConnector.onTestFailure(wd(), failedMethod);
	}

	//@Nonnull
	//public File getSnapshotName() {
	//	return getSnapshotName(Integer.toString(m_screenShotCount++));
	//}

	@Nonnull
	private File getSnapshotName(String baseName) {
		File testReportDir = findTestReportDir();
		String testName = m_testName.getMethodName();
		String reportName = getClass().getSimpleName() + "_" + testName + "-" + baseName + ".png";
		m_screenShotCount++;
		return new File(testReportDir, reportName);
	}

	public void saveImage(BufferedImage bi, String baseName, String description) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", bos);
			byte[] bytes = bos.toByteArray();
			allureAttachment(bytes, description);
			File file = getSnapshotName(baseName);
			try(FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(bytes);
			}
		} catch(Exception x) {
			System.err.println("AbstractWebDriverTest: failed to save image " + baseName + ": " + x);
		}
	}

	/**
	 * Creates a snapshot of the current screen inside the failsafe directory.
	 */
	public void snapshot(String description) {
		File testReportDir = findTestReportDir();
		String testName = m_testName.getMethodName();
		String reportName = getClass().getSimpleName() + "_" + testName + ".png";
		File out = new File(testReportDir, reportName);

		Exception failure = null;
		try {
			wd().screenshot(out);
		} catch(UnhandledAlertException x) {
			System.err.println("snapshot: alert present when taking screenshot - trying to get rid of it");

			//-- Try to handle alert
			try {
				String message = wd().alertGetMessage();
				System.err.println("snapshot: alert message is:\n" + message);
			} catch(Exception xx) {
				System.err.println("snapshot: exception accepting alert");
				xx.printStackTrace();
			}

			//-- Try screenshot a 2nd time, die this time if it fails again
			try {
				wd().screenshot(out);
			} catch(Exception xx) {
				failure = xx;
			}
		} catch(Exception x) {
			failure = x;
		}

		if(null == failure) {
			System.out.println("snapshot taken as " + out + " (" + out.getAbsolutePath() + ")");
			try {
				byte[] bytes = FileTool.readFileAsByteArray(out);
				allureAttachment(bytes, description);
			} catch(Exception x) {
			}

		} else {
			System.err.println("Failed to take a screenshot");
			failure.printStackTrace();
		}
	}

	public File findTestReportDir() {
		File f = new File("target/failsafe-reports");
		if(f.exists() && f.isDirectory())
			return f;

		f = new File("target/surefire-reports");
		if(f.exists() && f.isDirectory())
			return f;

		f = new File("/tmp/testoutput");
		f.mkdirs();
		return f;
	}

	@Attachment(value = "{method} {1}", type = "image/png")
	public byte[] allureAttachment(byte[] image, String description) {
		return image;
	}

	@Attachment(value = "{1}", type="text/plain")
	public String allureText(String data, String description) {
		return data;
	}

}
