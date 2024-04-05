package to.etc.domui.webdriver.core;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

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
	}

	@Override
	protected void succeeded(Description description) {
		m_testBase.internalClosePerTestResources();
	}
}
