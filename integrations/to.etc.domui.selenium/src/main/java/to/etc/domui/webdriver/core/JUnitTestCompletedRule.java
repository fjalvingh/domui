package to.etc.domui.webdriver.core;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.openqa.selenium.By;

/**
 * This rule causes a screenshot to be written to test output when a
 * test fails. It also calls a specific method to close resources
 * after the rule has executed.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-9-17.
 */
public class JUnitTestCompletedRule extends TestWatcher {
	final private AbstractWebDriverTestBase m_testBase;

	public JUnitTestCompletedRule(AbstractWebDriverTestBase testBase) {
		m_testBase = testBase;
	}

	@Override
	protected void failed(Throwable e, Description description) {
		m_testBase.snapshot("Screen after test failure");		// ORDERED
		m_testBase.internalClosePerTestResources();							// ORDERED

		try {
			String body = m_testBase.wd().getHtmlText(By.tagName("body"));
			System.err.println("---- HTML ----");
			System.err.println(body);
			System.err.println("---- /HTML ----");
		} catch(Exception x) {
			System.err.println("debug: failed to get html: " + x);
		}
	}

	@Override
	protected void succeeded(Description description) {
		m_testBase.internalClosePerTestResources();
	}
}
