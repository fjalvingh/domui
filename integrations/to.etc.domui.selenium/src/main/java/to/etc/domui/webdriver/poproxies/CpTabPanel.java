package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.webdriver.core.WebDriverConnector;

import java.util.function.Supplier;

@NonNullByDefault
public class CpTabPanel extends AbstractCpComponent {
	@Nullable
	private String m_current;

	public CpTabPanel(WebDriverConnector wd, Supplier<String> selectorProvider) {
		super(wd, selectorProvider);
	}

	///**
	// * Opens a tab with corresponding testID.
	// * @param testId of the tab
	// * @param <T> so that you can cast it to the tab you want.
	// * @return Page Object of the tab, if found.
	// * @throws IllegalStateException if tab is not configured.
	// */
	//public <T extends AbstractCpBase> T open(String testId) {
	//	if(!testId.equals(m_current)) {
	//		By locator = By.cssSelector(createTestIdSelector().concat(" ").concat(createTestIdSelector(testId)));
	//		wd().cmd().click().on(locator);
	//		wd().wait(locator);
	//		m_current = testId;
	//	}
	//	var tab = m_tabs.get(testId);
	//	if(tab == null) {
	//		throw new IllegalStateException("Tab with testId " + testId + " is not configured");
	//	}
	//	return (T) tab;
	//}
	//
	//public Pair<String, String> getCurrentlyOpenTab() {
	//	var selected = wd().findElement(By.cssSelector(createTestIdSelector() + " .ui-tab-sel"));
	//	if(selected == null) {
	//		throw new IllegalStateException("Cant find selected tab?");
	//	}
	//	return new Pair<>(selected.getAttribute("testId"), selected.getText());
	//}
	//
	///**
	// * Gets the tabs that are on the page, by provided testId
	// * @returns a map, where key is the testId of the tab and value is the label.
	// */
	//public Map<String, String> getTabs() {
	//	var elements = wd().findElements(By.cssSelector(createTestIdSelector() + " .ui-tab-hdr li"));
	//	return elements.stream().collect(toMap(x -> x.getAttribute("testId"), x -> x.getText()));
	//}
	//
	//public static class TabInstancePO<T extends AbstractCpBase> {
	//	private String m_testId;
	//
	//	private T m_content;
	//
	//	public TabInstancePO(String testId, T content) {
	//		m_testId = testId;
	//		m_content = content;
	//	}
	//
	//	public String getTestId() {
	//		return m_testId;
	//	}
	//
	//	public T getContent() {
	//		return m_content;
	//	}
	//}
}
