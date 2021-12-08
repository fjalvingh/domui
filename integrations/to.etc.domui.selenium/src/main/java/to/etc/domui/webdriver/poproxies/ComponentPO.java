package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.webdriver.core.WebDriverConnector;

@NonNullByDefault
abstract public class ComponentPO extends BasePO {
	private final String m_testId;
	@Nullable
	private String m_testIdSelector;

	public ComponentPO(WebDriverConnector wd, String testId) {
		super(wd);
		m_testId = testId;

	}

	public String getTestId() {
		return m_testId;
	}

	public boolean isPresent() {
		return wd().isPresent(m_testId);
	}

	protected String createTestIdSelector() {
		var selector = m_testIdSelector;
		if(selector == null) {
			selector = m_testIdSelector = createTestIdSelector(m_testId);
		}
		return selector;
	}

	protected String createTestIdSelector(String id) {
		return "*[testId='".concat(id).concat("']");
	}
}
