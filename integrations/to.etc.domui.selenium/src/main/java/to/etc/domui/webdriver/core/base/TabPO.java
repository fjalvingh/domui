package to.etc.domui.webdriver.core.base;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import to.etc.domui.webdriver.core.WebDriverConnector;
import to.etc.util.Pair;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@NonNullByDefault
public class TabPO extends BasePO {
	@Nullable
	private String m_current;

	private String m_id;

	private Map<String, ? extends BasePO> m_tabs;

	/**
	 * @param wd web driver
	 * @param testId of the Tab
	 * @param tabs list of configured tabs.
	 */
	public TabPO(WebDriverConnector wd, String testId, Map<String,? extends BasePO> tabs) {
		super(wd);
		m_id = testId;
		m_tabs = tabs;
	}

	/**
	 * Opens a tab with corresponding testID.
	 * @param testId of the tab
	 * @param <T> so that you can cast it to the tab you want.
	 * @return Page Object of the tab, if found.
	 * @throws IllegalStateException if tab is not configured.
	 */
	public <T extends BasePO> T open(String testId) {
		if(!testId.equals(m_current)) {
			By locator = By.cssSelector(createTestId(m_id).concat(" ").concat(createTestId(testId)));
			wd().cmd().click().on(locator);
			wd().wait(locator);
			m_current = testId;
		}
		var tab = m_tabs.get(testId);
		if(tab == null) {
			throw new IllegalStateException("Tab with testId " +testId + " is not configured");
		}
		return (T) tab;
	}

	public Pair<String, String> getCurrentlyOpenTab() {
		var selected = wd().findElement(By.cssSelector(createTestId(m_id + " .ui-tab-sel")));
		if(selected == null) {
			throw new IllegalStateException("Cant find selected tab?");
		}
		return new Pair<>(selected.getAttribute("testId"), selected.getText());
	}

	/**
	 * Gets the tabs that are on the page, by provided testId
	 * @returns a map, where key is the testId of the tab and value is the label.
	 */
	public Map<String, String> getTabs() {
		var elements = wd().findElements(By.cssSelector(createTestId(m_id) + " .ui-tab-hdr li"));
		return elements.stream().collect(toMap(x->x.getAttribute("testId"), x->x.getText()));
	}

	public static class TabInstancePO<T extends BasePO> {
		private String m_testId;
		private T m_content;

		public TabInstancePO(String testId, T content) {
			m_testId = testId;
			m_content = content;
		}

		public String getTestId() {
			return m_testId;
		}

		public T getContent() {
			return m_content;
		}
	}
}
