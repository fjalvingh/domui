package to.etc.domui.webdriver.core;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-9-17.
 */
public class JUnitOnFailedRule extends TestWatcher {
	final private AbstractWebDriverTest m_testBase;

	public JUnitOnFailedRule(AbstractWebDriverTest testBase) {
		m_testBase = testBase;
	}

	@Override protected void failed(Throwable e, Description description) {
		m_testBase.snapshot();
	}
}
