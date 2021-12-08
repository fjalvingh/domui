package to.etc.domui.uitest.pogenerator;

/**
 * A selector that uses the test ID, the default selector for discovered thingies.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 08-12-21.
 */
public class PoSelectorTestId implements IPoSelector {
	private final String m_testId;

	public PoSelectorTestId(String testID) {
		m_testId = testID;
	}

	@Override
	public String selectorAsCode() {
		return "By.cssSelector(\"*[testId='" + m_testId + "']\")";
	}
}
