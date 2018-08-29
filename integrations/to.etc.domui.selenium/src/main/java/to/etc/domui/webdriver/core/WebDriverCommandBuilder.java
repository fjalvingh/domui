package to.etc.domui.webdriver.core;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

final public class WebDriverCommandBuilder {
	@NonNull
	final private WebDriverConnector m_wd;

	@Nullable
	private List<CharSequence> m_text;

	enum WaitType {
		NONE, PRESENT, VISIBLE, INVISIBLE, CLICKABLE
	}

	enum ActionType {
		NONE, CLICK, TEXT, CHECK, UNCHECK
	}

	@NonNull
	private WaitType m_wait = WaitType.NONE;

	@NonNull
	private ActionType m_action = ActionType.NONE;

	@Nullable
	private Keys[] m_clickKeys;

	WebDriverCommandBuilder(@NonNull WebDriverConnector wd) {
		m_wd = wd;
	}

	@NonNull
	protected WebDriver driver() {
		return m_wd.driver();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Wait indicators.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Wait for the element to become present.
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder present() {
		m_wait = WaitType.PRESENT;
		return this;
	}

	/**
	 * Wait until the element is clickable.
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder clickable() {
		m_wait = WaitType.CLICKABLE;
		return this;
	}

	/**
	 * Wait until the element is visible.
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder visible() {
		m_wait = WaitType.VISIBLE;
		return this;
	}

	/**
	 * Wait until the element is invisible.
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder invisible() {
		m_wait = WaitType.INVISIBLE;
		return this;
	}

	/**
	 * Alter the default timeout for this action.
	 * @param milliseconds
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder timeout(int milliseconds) {
		m_wd.setNextWaitTimeout(milliseconds);
		return this;
	}

	/**
	 * Alter the default wait interval for this action.
	 * @param milliseconds
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder interval(int milliseconds) {
		m_wd.setNextInterval(milliseconds);
		return this;
	}

	private void action(@NonNull ActionType act) {
		if(m_action != ActionType.NONE && m_action != act)
			throw new IllegalStateException("You already specified an action: " + m_action);
		m_action = act;
	}

	private void appendText(@NonNull CharSequence what) {
		List<CharSequence> list = m_text;
		if(null == list) {
			list = m_text = new ArrayList<>();
		}
		list.add(what);
	}

	/**
	 * Enter text in an input field, clearing the contents before. The text can be
	 * ext and special keycodes, see {@link Keys}. The \n and \t characters will
	 * be translated to the ENTER key and the TAB key.
	 */
	@NonNull
	public WebDriverCommandBuilder type(@NonNull CharSequence... text) {
		action(ActionType.TEXT);
		for(CharSequence cs : text)
			appendText(cs);
		return this;
	}

	/**
	 * Click something, with optional keys pressed.
	 * @param withKeys
	 * @return
	 */
	@NonNull
	public WebDriverCommandBuilder click(Keys... withKeys) {
		action(ActionType.CLICK);
		m_clickKeys = withKeys;
		return this;
	}

	@NonNull
	public WebDriverCommandBuilder check() {
		action(ActionType.CHECK);
		return this;
	}

	@NonNull
	public WebDriverCommandBuilder check(boolean checked) {
		action(checked ? ActionType.CHECK : ActionType.UNCHECK);
		return this;
	}

	@NonNull
	public WebDriverCommandBuilder uncheck() {
		action(ActionType.UNCHECK);
		return this;
	}

	/**
	 * Define the item the actions are do be done on, and executes the actions.
	 * @param testid
	 */
	public void on(@NonNull String testid) {
		on(m_wd.byId(testid));
	}

	public void on(@NonNull String testid, @NonNull String subCss) {
		on(m_wd.byId(testid, subCss));
	}

	public void on(@NonNull By locator) {
		//-- All WAIT actions.
		switch(m_wait){
			default:
				throw new IllegalStateException(m_wait + ": wait type not implemented?");
			case NONE:
				break;
			case CLICKABLE:
				m_wd.waitForElementClickable(locator);
				break;
			case PRESENT:
				m_wd.waitForElementPresent(locator);
				break;
			case VISIBLE:
				m_wd.waitForElementVisible(locator);
				break;
			case INVISIBLE:
				m_wd.waitForElementInvisible(locator);
				break;
		}

		switch(m_action){
			default:
				throw new IllegalStateException(m_action + ": unimplemented action");
			case NONE:
				break;

			case CLICK:
				m_wd.internalClick(locator, m_clickKeys);
				m_clickKeys = null;
				break;

			case TEXT:
				List<CharSequence> list = m_text;
				if(null != list) {
					m_wd.text(locator, list);
					m_text = null;
				}
				break;

			case CHECK:
			case UNCHECK:
				m_wd.internalCheck(locator, m_action == ActionType.CHECK);
				break;
		}
		m_wd.resetWaits();
	}
}
