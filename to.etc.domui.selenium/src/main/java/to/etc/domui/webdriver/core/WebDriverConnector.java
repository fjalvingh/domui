package to.etc.domui.webdriver.core;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.PageParameters;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.IExecute;
import to.etc.net.HttpCallException;
import to.etc.pater.IPaterContext;
import to.etc.pater.Pater;
import to.etc.util.FileTool;
import to.etc.util.StringTool;
import to.etc.util.WrappedException;
import to.etc.webapp.testsupport.TUtilTestProperties;
import to.etc.webapp.testsupport.TestProperties;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * This wraps the WebDriver core and exposes ways to use it.
 */
@DefaultNonNull
final public class WebDriverConnector {

	private static final Logger LOG = LoggerFactory.getLogger(WebDriverConnector.class);

	public static final String PAGENAME_PARAMETER = "pagename";

	public static final String BROWSERSTACK = "browserstack://";

	static private List<WebDriverConnector> m_webDriverConnectorList = new ArrayList<WebDriverConnector>();

	static private ThreadLocal<WebDriverConnector> m_webDriverThreadLocal = new ThreadLocal<WebDriverConnector>();

	final private WebDriver m_driver;

	/** When T, exit registration has been done, to ensure things are released when the JVM exits. */
	static private boolean m_jvmExitHandlerRegistered;

	/** Wait timeout in SECONDS */
	private int m_waitTimeout = 60;

	/** Wait interval in MILLISECONDS */
	private int m_waitInterval = 250;

	private int m_nextWaitTimeout = -1;

	private int m_nextInterval = -1;

	/** The base of the application URL, to which page names will be appended. */
	@Nonnull final private String m_applicationURL;

	@Nonnull
	private final WebDriverType m_driverType;

	@Nonnull final private BrowserModel m_kind;

	private boolean m_inhibitAfter;

	private volatile boolean m_closed;

	@Nullable
	private IExecute m_afterCommandCallback;

	/** The default viewport size. */
	@Nonnull
	private Dimension m_viewportSize = new Dimension(1280, 1024);

	@Nullable
	private final IWebdriverScreenshotHelper m_screenshotHelper;

	private WebDriverConnector(@Nonnull WebDriver driver, @Nonnull BrowserModel kind, @Nonnull String webapp, @Nonnull WebDriverType driverType, @Nullable IWebdriverScreenshotHelper helper) {
		m_driver = driver;
		m_kind = kind;
		m_applicationURL = webapp;
		m_driverType = driverType;
		m_waitTimeout = readWaitTimeout(m_waitTimeout);
		m_screenshotHelper = helper;
	}

	public boolean canTakeScreenshot() {
		return m_screenshotHelper != null;
	}

	/**
	 * Registers a JVM exit handler to clear up all WebDriver instances.
	 */
	static synchronized private void registerExitHandler() {
		if(m_jvmExitHandlerRegistered)
			return;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				destroyWebDrivers();
			}
		});
		m_jvmExitHandlerRegistered = true;
	}

	/**
	 * Walk all known WebDriver instances and close them.
	 */
	static private void destroyWebDrivers() {
		System.out.println("destroyWebDriver called");
		for(WebDriverConnector wd : m_webDriverConnectorList) {
			try {
				if(!wd.m_closed) {
					System.out.println("Destroying " + wd);
					wd.m_closed = true;
					wd.m_driver.quit();
				}
			} catch(Exception x) {
//				x.printStackTrace();
			}
		}
		m_webDriverConnectorList.clear();
	}

	private static void initLogging() {
	}

	/**
	 * This gets a connector instance. Instances are allocated once per thread and then bound to it.
	 */
	static public WebDriverConnector get() throws Exception {
		//-- Do we have a driver for this thread?
		WebDriverConnector wd = m_webDriverThreadLocal.get();
		if(null != wd) {
			if(!wd.m_closed)
				return wd;
		}
		initLogging();
		registerExitHandler();

		//-- Get parameters to connect to the application and the webdriver host
		TestProperties p = TUtilTestProperties.getTestProperties();
		String appURL = p.getProperty("webdriver.url");
		if(StringTool.isBlank(appURL) || appURL == null)
			throw new IllegalStateException("The webdriver.url parameter is not present in (a) test properties file");
		if(!appURL.endsWith("/"))
			appURL += "/";

		/*
		 * webdriver spec is browser@destination. If the @destination is missing
		 * we assume local.
		 */
		String ws = Objects.requireNonNull(p.getProperty("webdriver.hub", "chrome"));
		int pos = ws.indexOf('@');
		String browserName;
		String remote;
		if(pos > 0) {
			browserName = ws.substring(0, pos);
			remote = ws.substring(pos + 1);
		} else {
			remote = "local";
			browserName = ws;
		}

		BrowserModel browserModel = BrowserModel.get(browserName);
		WebDriverType webDriverType = getDriverType(remote);
		boolean canTakeScreenshot = browserModel == BrowserModel.PHANTOMJS || webDriverType == WebDriverType.LOCAL || webDriverType == WebDriverType.REMOTE;

		WebDriver wp = WebDriverFactory.allocateInstance(webDriverType, browserModel, remote, null);

		IWebdriverScreenshotHelper sshelper = WebDriverFactory.getScreenshotHelper(webDriverType, browserModel);

		final WebDriverConnector tu = new WebDriverConnector(wp, browserModel, appURL, webDriverType, sshelper);
		initializeAfterCommandListener(tu);
		m_webDriverConnectorList.add(tu);
		m_webDriverThreadLocal.set(tu);
		return tu;
	}

	@Nonnull
	private static WebDriverType getDriverType(@Nullable String hubUrl) {
		if(null == hubUrl || hubUrl.trim().length() == 0)
			return WebDriverType.HTMLUNIT;                    // Used as a target because it can emulate multiple browser types
		if("local".equals(hubUrl.trim()))
			return WebDriverType.LOCAL;
		if(hubUrl.startsWith(BROWSERSTACK)) {
			return WebDriverType.BROWSERSTACK;
		}
		return WebDriverType.REMOTE;
	}

	/**
	 * Called after every screen action, this checks whether the DomUI "waiting" backdrop is present and waits for it
	 * to be gone.
	 */
	private static void initializeAfterCommandListener(final @Nonnull WebDriverConnector tu) {
		tu.setAfterCommandCallback(new IExecute() {
			@Override
			public void execute() {
				try {
					tu.waitForNoneOfElementsPresent(By.className("ui-io-blk"), By.className("ui-io-blk2"));
				} catch(UnhandledAlertException e) {
					//-- If an alert is present then we just ignore and continue.
				} catch(Exception x) {
					throw WrappedException.wrap(x);
				}
			}
		});
	}


	public int getWaitTimeout() {
		return m_nextWaitTimeout != -1 ? m_nextWaitTimeout : m_waitTimeout;
	}

	public int getWaitInterval() {
		return m_nextInterval != -1 ? m_nextInterval : m_waitInterval;
	}

	public void setNextWaitTimeout(int millis) {
		m_nextWaitTimeout = millis;
	}

	public void setNextInterval(int nextInterval) {
		m_nextInterval = nextInterval;
	}

	public void resetWaits() {
		m_nextWaitTimeout = -1;
		m_nextInterval = -1;
	}

	@Nonnull public WebDriverType getDriverType() {
		return m_driverType;
	}

	@Nonnull
	public WebDriver driver() {
		WebDriver d = m_driver;
		if(null == d)
			throw new IllegalStateException("no webdriver connected");
		return d;
	}

	protected final void handleAfterCommandCallback() {
		if(m_inhibitAfter) {
			m_inhibitAfter = false;
			return;
		}
		IExecute callback = m_afterCommandCallback;
		if(null != callback) {
			try {
				callback.execute();
			} catch(Exception x) {
				throw WrappedException.wrap(x);
			}
		}
	}

	protected void setAfterCommandCallback(@Nullable IExecute callback) {
		m_afterCommandCallback = callback;
	}

	public void inhibitAfter() {
		m_inhibitAfter = true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Locators.											*/
	/*--------------------------------------------------------------*/

	/**
	 * Create the locator for a given testid.
	 */
	@Nonnull final public By byId(@Nonnull String testid) {
		return By.cssSelector("*[testid='" + testid + "']");
	}

	@Nonnull final public By byId(@Nonnull String testid, @Nonnull String elementType) {
		return By.cssSelector("*[testid='" + testid + "'] " + elementType);
	}


	/**
	 * Create a full locator using any supported expression.
	 */
	@Nonnull final public By locator(@Nonnull String locator) {
		if(locator.startsWith("//")) {
			return By.xpath(locator);
		} else if(locator.startsWith("#")) {
			return By.id(locator.substring(1));
		} else {
			return By.cssSelector(locator);
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getting multiple nodes..							*/
	/*--------------------------------------------------------------*/

	/**
	 * Returns the number of nodes matching the css.
	 */
	public int countMatching(@Nonnull String locator) {
		return countMatching(locator(locator));
	}

	/**
	 * Returns the number of nodes matching the specified locator.
	 */
	public int countMatching(@Nonnull By locator) {
		List<WebElement> elements = driver().findElements(locator);
		return elements.size();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Waiting.											*/
	/*--------------------------------------------------------------*/

	/**
	 * Wait for the given element to appear.
	 */
	public void wait(@Nonnull By locator) {
		WebDriverWait wait = new WebDriverWait(driver(), getWaitTimeout(), getWaitInterval());
		wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	@Nullable
	public WebElement wait(@Nonnull ExpectedCondition<WebElement> exc) {
		Wait<WebDriver> wait = new FluentWait<>(driver())
			.withTimeout(getWaitTimeout(), TimeUnit.SECONDS)
			.pollingEvery(getWaitInterval(), TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class);

		return wait.until(exc);
	}

	/**
	 * Waits until the element is present.
	 */
	public void wait(@Nonnull String testid) {
		wait(byId(testid));
	}

	void waitForElementClickable(@Nonnull By locator) {
		WebDriverWait wait = new WebDriverWait(driver(), getWaitTimeout(), getWaitInterval());
		wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	void waitForElementPresent(@Nonnull By locator) {
		WebDriverWait wait = new WebDriverWait(driver(), getWaitTimeout(), getWaitInterval());
		wait.until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public void waitForElementVisible(@Nonnull By locator) {
		WebDriverWait wait = new WebDriverWait(driver(), getWaitTimeout(), getWaitInterval());
		wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
	}

	public void waitForElementVisible(@Nonnull String testid) {
		waitForElementVisible(byId(testid));
	}

	void waitForElementInvisible(@Nonnull By locator) {
		WebDriverWait wait = new WebDriverWait(driver(), getWaitTimeout(), getWaitInterval());
		wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
	}

	void waitForLink(@Nonnull String linkText) {
		wait(By.linkText(linkText));
	}

	public void waitAlertAndAccept() {
		waitAlert();
		alertAccept();
	}

	public void waitAlert() {
		WebDriverWait wait = new WebDriverWait(driver(), getWaitTimeout(), getWaitInterval());
		wait.until(ExpectedConditions.alertIsPresent());
	}

	private int readWaitTimeout(int defaultTimeout) {
		TestProperties p = TUtilTestProperties.getTestProperties();
		String waitTimeout = p.getProperty("webdriver.waittimeout", defaultTimeout + "");
		try {
			return Integer.parseInt(waitTimeout);
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("webdriver.waittimeout parameter in .test.properties file can't be converted to int");
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Specials.											*/
	/*--------------------------------------------------------------*/
	void internalClick(@Nonnull By locator, @Nullable Keys[] withKeys) {
		try {
			waitForElementClickable(locator);
			clickNoWait(locator, withKeys);
		} catch(WebDriverException ex) {
			String msg = ex.getLocalizedMessage();
			if(null != msg && msg.contains("Other element would receive the click") && msg.contains("ui-io-blk")) {
				handleAfterCommandCallback();
				internalClickNoRetry(locator, withKeys);
			} else {
				throw ex;
			}
		}
	}

	private void internalClickNoRetry(@Nonnull By locator, @Nullable Keys[] withKeys) {
		waitForElementClickable(locator);
		clickNoWait(locator, withKeys);
	}

	void clickNoWait(@Nonnull By locator, @Nullable Keys[] withKeys) {
		WebElement elem = driver().findElement(locator);
		clickNoWait(elem, withKeys);
	}

	void clickNoWait(@Nonnull WebElement elem, @Nullable Keys[] withKeys) {
		if(withKeys == null || withKeys.length == 0) {
			elem.click();
		} else {
			Actions builder = new Actions(driver());
			for(Keys key : withKeys) {
				builder.keyDown(key);
			}
			builder.click(elem);
			for(int i = withKeys.length; --i >= 0; ) {
				builder.keyUp(withKeys[i]);
			}
			builder.build().perform();
		}

		handleAfterCommandCallback();
	}

	/**
	 * Clicks on specified occurrence of element located by locator.
	 *
	 * @throws Exception
	 */
	public void clickInstance(@Nonnull final By locator, final int index, final Keys... optionalKeys) throws Exception {
		wait(locator);
		timed(new WebDriverConnector.Action<Boolean>() {

			@Override
			@Nullable
			public Boolean execute() throws Exception {
				List<WebElement> elements = driver().findElements(locator);
				if(elements.size() <= index)
					return null;
				clickNoWait(elements.get(index), optionalKeys);
				handleAfterCommandCallback();
				return Boolean.TRUE;
			}
		});
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Check methods.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Returns T if the specified element is present.
	 * @param locator
	 * @return
	 */
	final public boolean isPresent(@Nonnull By locator) {
		return !driver().findElements(locator).isEmpty();
	}

	/**
	 * Returns T if the specified element is present.
	 * @param testId
	 * @return
	 */
	final public boolean isPresent(@Nonnull String testId) {
		return isPresent(byId(testId));
	}

	/**
	 * Returns T if a link with the specified text is present.
	 *
	 * @param linkText
	 * @return
	 */
	final public boolean isLinkPresent(@Nonnull String linkText) {
		return isPresent(By.linkText(linkText));
	}

	/**
	 * Return T if the element specified by testid is visible.
	 * @param testid
	 * @return
	 */
	final public boolean isVisible(@Nonnull String testid) {
		return isVisible(byId(testid));
	}

	/**
	 * Return T if the element is visible.
	 * @param locator
	 * @return
	 */
	final public boolean isVisible(@Nonnull By locator) {
		if(!isPresent(locator)) {
			return false;
		}
		on(locator);
		WebElement elem = driver().findElement(locator);
		return elem.isDisplayed();
	}

	/**
	 * Return T if the element specified by testid is enabled.
	 *
	 * @param testid
	 * @return
	 */
	final public boolean isEnabled(@Nonnull String testid) {
		return isEnabled(byId(testid));
	}

	/**
	 * Return T if the element is enabled.
	 * @param locator
	 * @return
	 */
	final public boolean isEnabled(@Nonnull By locator) {
		on(locator);
		WebElement elem = driver().findElement(locator);
		return elem.isEnabled();
	}

	/**
	 * Return T if the element is not a readonly element.
	 * @param testid
	 * @return
	 */
	final public boolean isEditable(@Nonnull String testid) {
		return isEditable(byId(testid));
	}

	/**
	 * Return T if the element is not a readonly element.
	 * @param locator
	 * @return
	 */
	final public boolean isEditable(@Nonnull By locator) {
		on(locator);
		WebElement elem = driver().findElement(locator);
		boolean enabled = elem.isEnabled();
		String readOnlyVal = elem.getAttribute("readOnly");
		return enabled && !"true".equals(readOnlyVal) && !"readonly".equalsIgnoreCase(readOnlyVal);
	}

	/**
	 * T if the component is checked; only applies to checkboxes, radiobuttons or select options.
	 * @param testid
	 * @return
	 */
	final public boolean isChecked(@Nonnull String testid) {
		return isChecked(byId(testid));
	}

	/**
	 * T if the component is checked; only applies to checkboxes, radiobuttons or select options.
	 * @param locator
	 * @return
	 */
	final public boolean isChecked(@Nonnull By locator) {
		on(locator);
		WebElement elem = driver().findElement(locator);
		return elem.isSelected();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Value getters and setters.							*/
	/*--------------------------------------------------------------*/

	/**
	 * Get the value for a given input thing; this returns the value for the "value=" attribute.
	 * @param testid
	 * @return
	 */
	@Nullable final public String getValue(@Nonnull String testid) {
		return getValue(byId(testid));
	}

	/**
	 * Get the value for a given input thing; this returns the value for the "value=" attribute.
	 * @param locator
	 * @return
	 */
	@Nullable final public String getValue(@Nonnull By locator) {
		WebElement elem = driver().findElement(locator);
		return elem.getAttribute("value");
	}

	/**
	 * Get the HTML text that is inside the specified node (the thing between the tag and the end tag).
	 * @param testid
	 * @return
	 */
	@Nonnull final public String getHtmlText(@Nonnull String testid) {
		return getHtmlText(byId(testid));
	}

	/**
	 * Get the HTML text that is inside the specified node (the thing between the tag and the end tag).
	 * @param locator
	 * @return
	 */
	@Nonnull final public String getHtmlText(@Nonnull By locator) {
		WebElement elem = driver().findElement(locator);
		return elem.getText();
	}

	/**
	 * Locate the specified expected attribute value
	 * @param locator
	 * @return
	 */
	@Nonnull
	public String getAttribute(@Nonnull By locator, @Nonnull String attribute) {
		String value = driver().findElement(locator).getAttribute(attribute);
		if(value == null) {
			throw new IllegalStateException("No value for expected attribute " + attribute + " at locator " + locator);
		}
		return value;
	}

	/**
	 * Locate the specified expected attribute value
	 * @param testid
	 * @return
	 */
	@Nonnull
	public String getAttribute(@Nonnull String testid, @Nonnull String attribute) {
		return getAttribute(byId(testid), attribute);
	}

	/**
	 * Locate the expected specified attribute using "testid@attribute" syntax.
	 *
	 * @param testidandattr
	 * @return
	 */
	@Nonnull
	public String getAttribute(@Nonnull String testidandattr) {
		int loc = testidandattr.indexOf('@');
		if(loc == -1)
			throw new IllegalStateException("No @ in specification");
		return getAttribute(testidandattr.substring(0, loc), testidandattr.substring(loc + 1));
	}

	/**
	 * Locate the specified attribute, null if no attribute is found
	 *
	 * @param locator
	 * @return
	 */
	@Nullable
	public String findAttribute(@Nonnull By locator, @Nonnull String attribute) {
		return driver().findElement(locator).getAttribute(attribute);
	}

	/**
	 * Locate the specified attribute, null if no attribute is found
	 * @param testid
	 * @return
	 */
	@Nullable
	public String findAttribute(@Nonnull String testid, @Nonnull String attribute) {
		return findAttribute(byId(testid), attribute);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Setting values.										*/
	/*--------------------------------------------------------------*/

	/**
	 * Set a (new) selected value in a select (combobox). The value can either be the
	 * literal label for the combo (the string presented on screen) or the string can
	 * contain an indicator of what is wanted:
	 * <dl>
	 * 	<dt>label=xxx</dt><dd>Select the option that is shown on-screen as "xxx"</dd>
	 *	<dt>id=xxx</dt><dd>Select the option whose xml id is xxx</dd>
	 *	<dt>value=</dt><dd>Select the option whose value (the code reported to the server) is xxx</dd>
	 * </dl>
	 * @param testid
	 * @param value
	 * @return
	 */
	public void select(@Nonnull String testid, @Nonnull String value) throws Exception {
		select(byId(testid), value);
	}

	/**
	 * Set a (new) selected value in a select (combobox). The value can either be the
	 * literal label for the combo (the string presented on screen) or the string can
	 * contain an indicator of what is wanted:
	 * <dl>
	 * 	<dt>label=xxx</dt><dd>Select the option that is shown on-screen as "xxx"</dd>
	 *	<dt>id=xxx</dt><dd>Select the option whose xml id is xxx</dd>
	 *	<dt>value=</dt><dd>Select the option whose value (the code reported to the server) is xxx</dd>
	 * </dl>
	 * @param locator
	 * @param value
	 * @return
	 */
	public void select(@Nonnull By locator, @Nonnull String value) throws Exception {
		List<WebElement> options = getSelectElementOptions(locator);
		WebElement toSelectOption = null;
		for(WebElement option : options) {
			String text = option.getText();
			if(null != text)
				text = text.trim();
			if(value.startsWith("label=")) {
				if(value.substring(6).equals(text)) {
					toSelectOption = option;
					break;
				}
			} else if(value.startsWith("id=")) {
				if(value.substring(3).equals(option.getAttribute("id"))) {
					toSelectOption = option;
					break;
				}
			} else if(value.startsWith("value=")) {
				if(value.substring(7).equals(option.getAttribute("value"))) {
					toSelectOption = option;
					break;
				}
			} else if(value.equals(text)) {
				toSelectOption = option;
				break;
			}
		}
		if(toSelectOption == null)
			throw new IllegalStateException("option [" + value + "]not found at select [" + locator + "]");
		toSelectOption.click();
		handleAfterCommandCallback();
	}

	/**
	 * Selects a value by matching a partial string in either value or label. The match is case-independent.
	 * @param locator
	 * @param value
	 * @return
	 */
	public void selectContaining(@Nonnull By locator, @Nonnull String value) throws Exception {
		List<WebElement> options = getSelectElementOptions(locator);
		WebElement toSelectOption = null;
		value = value.toLowerCase();
		int found = 0;
		for(WebElement option : options) {
			String text = option.getText();
			if(null != text)
				text = text.trim().toLowerCase();
			if(value.startsWith("label=")) {
				if(text != null && text.contains(value.substring(6))) {
					toSelectOption = option;
					found++;
				}
			} else if(value.startsWith("id=")) {                // We allow id, but it is questionable: it is a literal match.
				if(value.substring(3).equals(option.getAttribute("id"))) {
					toSelectOption = option;
					found++;
					break;
				}
			} else if(value.startsWith("value=")) {
				String aval = option.getAttribute("value");
				if(null != aval) {
					aval = aval.trim().toLowerCase();
					if(aval.contains(value.substring(7))) {
						toSelectOption = option;
						found++;
					}
				}
			} else if(text != null && text.contains(value)) {
				toSelectOption = option;
				found++;
			}
		}
		if(toSelectOption == null)
			throw new IllegalStateException("option [" + value + "] not contained in select [" + locator + "]");
		if(found > 1)
			throw new IllegalStateException("Multiple options contain the value [" + value + "] in select [" + locator + "]");
		toSelectOption.click();
		handleAfterCommandCallback();
	}

	private List<WebElement> getSelectElementOptions(@Nonnull By locator) {
		Select selectElement = getSelectElement(locator);
		return selectElement.getOptions();
	}

	@Nonnull private Select getSelectElement(@Nonnull By locator) {
		on(locator);
		WebElement elem = driver().findElement(locator);
		WebElement select = elem.findElement(By.tagName("select"));
		return new Select(select);
	}

	/**
	 * Locate the option that at least contains the specified text in its presentation string, case-independent compared.
	 * @param locator
	 * @param optionContainsText
	 * @return
	 */
	public void selectOptionContainingText(@Nonnull By locator, @Nonnull String optionContainsText) throws Exception {
		String option = getSelectOptionContaining(locator, optionContainsText);
		select(locator, option);
	}

	/**
	 * Get <b>all</b> of the options in a &lt;select&gt; tag with either their text (presentation) or value (data to send in request) node. The
	 * resulting set is a sorted set.
	 * @param testid
	 * @param value
	 * @return
	 */
	@Nonnull final public TreeSet<String> selectGetOptionSet(@Nonnull String testid, boolean value) {
		return selectGetOptionSet(byId(testid), value);
	}

	/**
	 * Get <b>all</b> of the options in a &lt;select&gt; tag with either their text (presentation) or value (data to send in request) node. The
	 * resulting set is a sorted set.
	 *
	 * @param locator
	 * @param value
	 * @return
	 */
	@Nonnull final public TreeSet<String> selectGetOptionSet(@Nonnull By locator, boolean value) {
		List<WebElement> options = getSelectElementOptions(locator);
		TreeSet<String> optionList = new TreeSet<String>();                // Ordered set.

		for(WebElement option : options) {
			optionList.add(value ? option.getAttribute("value") : option.getText());
		}
		return optionList;
	}

	/**
	 * Get <b>all</b> options of a &lt;select&gt; tag with either their text (presentation) or value (data to send in request)
	 * as a comma-separated string. No attempt is made to quote comma's in either value or text. The order of the items in the
	 * string is alphabetically sorted.
	 *
	 * @param locator
	 * @param value
	 * @return
	 */
	@Nonnull final public String selectGetOptionsString(@Nonnull By locator, boolean value) {
		StringBuilder sb = new StringBuilder(128);
		int count = 0;
		for(String s : selectGetOptionSet(locator, value)) {
			if(count++ > 0)
				sb.append(',');
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Get <b>all</b> options of a &lt;select&gt; tag with either their text (presentation) or value (data to send in request)
	 * as a comma-separated string. No attempt is made to quote comma's in either value or text. The order of the items in the
	 * string is alphabetically sorted.
	 * @param testid
	 * @param value
	 * @return
	 */
	@Nonnull final public String selectGetOptionsString(@Nonnull String testid, boolean value) {
		return selectGetOptionsString(byId(testid), value);
	}

	/**
	 * Get the currently selected item's label text from a select.
	 * @param testid
	 * @return
	 */
	@Nullable final public String selectGetSelectedLabel(@Nonnull String testid) {
		return selectGetSelected(byId(testid), false);
	}

	/**
	 * Get the currently selected item's value (not the visible label, but the value reported to the server) from a select.
	 * @param testid
	 * @return
	 */
	@Nullable final public String selectGetSelectedValue(@Nonnull String testid) {
		return selectGetSelected(byId(testid), true);
	}

	/**
	 * Get the currently selected value either as text or as the value item from a select item.
	 * @param locator
	 * @param byvalue
	 * @return
	 */
	@Nonnull final public String selectGetSelected(@Nonnull By locator, boolean byvalue) {
		Select selectElement = getSelectElement(locator);
		WebElement option = selectElement.getFirstSelectedOption();
		if(null == option) {
			return "";
		}
		if(byvalue)
			return option.getAttribute("value");
		else
			return option.getText();
	}

	@Nonnull final private String getSelectOptionContaining(@Nonnull String testid, @Nonnull String optionContainsText) {
		return getSelectOptionContaining(byId(testid), optionContainsText);
	}

	@Nonnull final private String getSelectOptionContaining(@Nonnull By locator, @Nonnull String optionContainsText) {
		waitForElementPresent(locator);
		Set<String> options = selectGetOptionSet(locator, false);

		for(String option : options) {
			if(option.toLowerCase().contains(optionContainsText.toLowerCase())) {
				return option;
			}
		}
		throw new IllegalStateException("Unable to select option [" + optionContainsText + "] in select locator: " + locator);
	}


	/**
	 * Maximizes browser window.
	 */
	public void maximizeBrowserWindow() {
		driver().manage().window().maximize();
	}

	/**
	 * Sets size of browser window.
	 */
	public void setSize(@Nonnull Dimension dimension) {
		driver().manage().window().setSize(dimension);
		m_viewportSize = dimension;
	}

	/**
	 *
	 * @return size of the browser window
	 */
	@Nonnull
	public Dimension getSize() {
		return driver().manage().window().getSize();
	}

	/**
	 * Takes screenshot and stores it on specified location.
	 *
	 * @param screenshotFile
	 * @return T if the screenshot was made.
	 * @throws IOException
	 */
	public boolean screenshot(@Nonnull File screenshotFile) throws Exception {
		IWebdriverScreenshotHelper helper = m_screenshotHelper;
		if(null == helper)
			return false;

		return helper.createScreenshot(this, screenshotFile);
	}

	/**
	 * Returns a ScreenInspector: something to play with the actual screen bitmap.
	 * @return
	 */
	@Nullable
	public ScreenInspector screenInspector() throws Exception {
		IWebdriverScreenshotHelper helper = m_screenshotHelper;
		if(null == helper)
			return null;

		BufferedImage bi = helper.createScreenshot(this);
		if(null == bi)
			return null;
		return new ScreenInspector(this, bi);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Out-of-bound talking with the same server session.	*/
	/*--------------------------------------------------------------*/

	/**
	 * This obtains a list of session cookies that should be used to talk with the
	 * remote server using the same session.
	 * @return
	 */
	@Nonnull
	private List<Cookie> getSessionCookies() {
		List<Cookie> res = new ArrayList<Cookie>();
		Cookie cookie = driver().manage().getCookieNamed("JSESSIONID"); // Tomcat specific.
		if(null != cookie)
			res.add(cookie);

		//-- FIXME Add other server's state cookie names here.


		//--
		if(res.size() == 0)
			throw new IllegalStateException("Cannot obtain session cookies");
		return res;
	}

	/**
	 * Send an out-of-bound request to the server under test, allowing us to manipulate that
	 * server's state outside of the state of the webdriver session.
	 *
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private String sendOobRequest(@Nonnull String url) throws Exception {
		List<Cookie> sescook = getSessionCookies();

		URL u = new URL(url);
		if(!u.getProtocol().equals("http") && !u.getProtocol().equalsIgnoreCase("https"))
			throw new IllegalStateException("This call can only accept http(s):// connections.");
		HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		Reader r = null;
		try {
			huc.setReadTimeout(30 * 1000);
			huc.setAllowUserInteraction(false);
			huc.setDoOutput(false);

			StringBuilder sb = new StringBuilder();
			for(Cookie c : sescook) {
				if(sb.length() > 0)
					sb.append(";");
				sb.append(c.getName());
				sb.append("=");
				sb.append(c.getValue());
				System.out.println(">>>> sending to sessionid=" + c.getValue());
			}

			huc.setRequestProperty("Cookie", sb.toString());
			huc.connect();

			//-- Check for a response...
			int code = huc.getResponseCode();
			if(code != HttpURLConnection.HTTP_OK) {
				throw handleHttpError(url, huc); // This throws an exception indicating the problem
			}
			String encoding = huc.getContentEncoding();
			if(encoding == null)
				encoding = "UTF-8";
			r = new InputStreamReader(huc.getInputStream(), encoding);
			String res = FileTool.readStreamAsString(r);
			return res;
		} finally {
			try {
				if(r != null)
					r.close();
			} catch(Exception x) {
			}
			try {
				if(huc != null)
					huc.disconnect();
			} catch(Exception x) {
			}
		}
	}

	@Nonnull
	static private HttpCallException handleHttpError(String url, HttpURLConnection huc) throws Exception {
		return new HttpCallException(url, huc.getResponseCode(), huc.getResponseMessage());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	DomUI screen(s).									*/
	/*--------------------------------------------------------------*/
	@Nonnull
	public WebDriverConnector openScreen(@Nonnull Class< ? extends UrlPage> clz, Object... parameters) throws Exception {
		return openScreen(null, clz, parameters);
	}

	/**
	 * Open the specified screen, and wait for it to be fully loaded.
	 * @param clz
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public WebDriverConnector openScreen(@Nullable Locale locale, @Nonnull Class< ? extends UrlPage> clz, Object... parameters) throws Exception {
		m_lastTestClass = null;
		m_lastTestPage = null;

		checkSize();

		String sb = calculatePageURL(locale, clz, parameters);
		m_driver.navigate().to(sb);
		checkSize();

		ExpectedCondition<WebElement> xdomui = ExpectedConditions.presenceOfElementLocated(locator("body[id='_1'], #loginPageBody"));

		WebElement we = wait(xdomui);

		String id = DomUtil.nullChecked(we).getAttribute("id");
		if("_1".equals(id)) 						// If this is a domUI body then be done
			return this;

		doLogin();

		xdomui = ExpectedConditions.presenceOfElementLocated(locator("body[id='_1']"));
		we = wait(xdomui);
		waitForNoneOfElementsPresent(By.className("ui-io-blk"), By.className("ui-io-blk2"));
		return this;
	}

	/**
	 * Called for Chrome, which resizes the viewpoint without question 8-(
	 */
	private void checkSize() {
		Dimension size = getSize();
		if(size.height == m_viewportSize.height && size.width == m_viewportSize.width)
			return;
		driver().manage().window().setSize(m_viewportSize);
	}

	@Nullable
	private Class<?> m_lastTestClass;

	@Nullable
	private String m_lastTestPage;

	@Nonnull
	public WebDriverConnector openScreenIf(@Nonnull Object testClass, @Nonnull Class< ? extends UrlPage> clz, Object... parameters) throws Exception {
		String sb = calculatePageURL(null, clz, parameters);
		if(m_lastTestClass == testClass.getClass() && sb.equals(m_lastTestPage)) {
			//-- Already open
			return this;
		}
		m_lastTestClass = null;
		m_lastTestPage = null;

		checkSize();
		Dimension size = getSize();

		m_driver.navigate().to(sb);
		checkSize();

		size = getSize();

		ExpectedCondition<WebElement> xdomui = ExpectedConditions.presenceOfElementLocated(locator("body[id='_1'], #loginPageBody"));

		WebElement we = wait(xdomui);

		String id = DomUtil.nullChecked(we).getAttribute("id");
		if(!"_1".equals(id)) {							// If this is a domUI body then be done
			doLogin();

			xdomui = ExpectedConditions.presenceOfElementLocated(locator("body[id='_1']"));
			we = wait(xdomui);
			waitForNoneOfElementsPresent(By.className("ui-io-blk"), By.className("ui-io-blk2"));
		}
		m_lastTestClass = testClass.getClass();
		m_lastTestPage = sb;
		size = getSize();
		return this;
	}

	@NotNull private String calculatePageURL(@Nullable Locale locale, @Nonnull Class<? extends UrlPage> clz, Object[] parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append(m_applicationURL);
		sb.append(clz.getName());
		sb.append(".ui");
		PageParameters pp = new PageParameters(parameters);
		if(null != locale) {
			pp.addParameter("___locale", locale.toString());
		}
		DomUtil.addUrlParameters(sb, pp, true);
		return sb.toString();
	}


	/**
	 * Cause the browser screen to refresh.
	 * @return
	 */
	public WebDriverConnector refresh() throws Exception {
		driver().navigate().refresh();
		ExpectedCondition<WebElement> xdomui = ExpectedConditions.presenceOfElementLocated(locator("body[id='_1']"));
		WebElement we = wait(xdomui);
		waitForNoneOfElementsPresent(By.className("ui-io-blk"), By.className("ui-io-blk2"));
		return this;
	}

	private void doLogin() throws Exception {
		String login = TUtilTestProperties.getViewpointLoginName();
		TestProperties p = TUtilTestProperties.getTestProperties();
		String password = p.getProperty("webdriver.loginpassword");

		//-- We have the login screen. Enter credentials.
		cmd().type(login).on(locator("#given_username"));
		cmd().type(password).on(locator("#given_password"));
		cmd().click().on(locator("#loginbutton"));
//		System.out.println("Logging in as " + login);
	}

	/**
	 * Wait for the specified DomUI screen to be fully loaded.
	 * @param clz
	 * @return
	 */
	@Nonnull
	public WebDriverConnector waitScreen(@Nonnull Class< ? extends UrlPage> clz) {
		wait(locator("body[" + PAGENAME_PARAMETER + "='" + clz.getName() + "']"));
		return this;
	}

	/**
	 * Wait for the specified DomUI screen to be replaced by some other screen.
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public WebDriverConnector waitScreenNotPresent(@Nonnull Class< ? extends UrlPage> clz) throws Exception {
		notPresent(locator("body[" + PAGENAME_PARAMETER + "='" + clz.getName() + "']"));
		return this;
	}

	@Nonnull
	public WebDriverCommandBuilder cmd() {
		return new WebDriverCommandBuilder(this);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Internal.											*/
	/*--------------------------------------------------------------*/

	private int calcTimeout(int milliseconds) {
		return milliseconds > 0 ? milliseconds : getWaitTimeout();
	}

	private int calcInterval(int milliseconds) {
		return milliseconds > 0 ? milliseconds : getWaitInterval();
	}

	/**
	 * Send a list of things to the keyboard.
	 * @param locator
	 * @param textlist
	 */
	void text(@Nonnull By locator, @Nonnull List<CharSequence> textlist) {
		waitForElementVisible(locator);
		WebElement elem = m_driver.findElement(locator);
		elem.sendKeys(Keys.chord(Keys.CONTROL, Keys.HOME));
		elem.sendKeys(Keys.chord(Keys.CONTROL, Keys.SHIFT, Keys.END));
		elem.sendKeys(Keys.DELETE);
		StringBuilder sb = new StringBuilder();
		for(Object o : textlist) {
			text(sb, o);
		}
		elem.sendKeys(sb);
	}

	private void text(@Nonnull StringBuilder sb, @Nullable Object o) {
		if(null == o)
			return;
		if(o instanceof String) {
			String s = (String) o;
			for(int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if(c == '\t')
					sb.append(Keys.TAB);
				else if(c == '\n')
					sb.append(Keys.ENTER);
				else
					sb.append(c);
			}
		} else if(o instanceof Keys) {
			sb.append((Keys) o);
		} else
			throw new IllegalStateException("Only accepting strings or Keys.");
	}


	//	public void click(@Nonnull WebElement element) {
	//		element.click();
	//	}

	void internalCheck(@Nonnull By locator, boolean selected) {
		WebElement elem = m_driver.findElement(locator);
		if(elem.isSelected() != selected) {
			elem.click();
			handleAfterCommandCallback();
		}
	}

	protected void on(@Nonnull By locator) {}


	/*--------------------------------------------------------------*/
	/*	CODING:	Frameset handling.									*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 */
	@Nonnull
	public WebDriverConnector resetFrames() {
		//String whandle = m_driver.getWindowHandle();
		m_driver.switchTo().defaultContent();
		return this;
	}

	@Nonnull
	public WebDriverConnector switchToFrame(@Nonnull String... names) {
		switchToFrameInternal(names.length, names);
		return this;
	}

	/**
	 * Used only internally. Needed since if can happen that frames are loading while driver start locating them.
	 * To make tests more robust, all what we can is to try to restart locating frames, with decreased retryCount.
	 * Initial retryCount is number of nested frames that we want to access and switch too.
	 *
	 * @param retryCount
	 * @param names
	 */
	private void switchToFrameInternal(int retryCount, @Nonnull String... names) {
		try {
			resetFrames();
			for(String name : names) {
				if(m_kind == BrowserModel.CHROME) {
					//bug in ChromeDriver, we must do it using xpath 8-/
					// http://code.google.com/p/chromedriver/issues/detail?id=107
					WebElement elem = m_driver.findElement(By.cssSelector("frame[name='" + name + "']"));
					m_driver.switchTo().frame(elem);
				} else {
					WebDriverWait wait = new WebDriverWait(m_driver, getWaitTimeout(), getWaitInterval());
					wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(name));
				}
			}
		} catch(TimeoutException ex) {
			if(retryCount > 1) {
				StringBuilder sb = new StringBuilder();
				for(String name : names) {
					sb.append(" ");
					sb.append(name);
				}
				LOG.warn("Retries to switch to frames: " + sb.toString());
				switchToFrameInternal(retryCount - 1, names);
			} else {
				throw ex;
			}
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Command stuff - old									*/
	/*--------------------------------------------------------------*/
//	/**
//	 * Wait for the element to become present.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder present() {
//		return cmd().present();
//	}
//
//	/**
//	 * Wait for the element to become visible.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder visible() {
//		return cmd().visible();
//	}
//
//	/**
//	 * Wait for the element to become invisible.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder invisible() {
//		return cmd().invisible();
//	}
//
//
//	/**
//	 * Wait until the element is clickable.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder clickable() {
//		return cmd().clickable();
//	}
//
//	/**
//	 * Type the specified text in the target. The characters \t (tab) and \n (return) are sent as
//	 * the specified keys.
//	 * @param text
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder type(@Nonnull String text) {
//		return cmd().type(text);
//	}
//
//	/**
//	 * Click the later selected element. You can optionally specify keys to keep pressed while the thingy is clicked.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder click(Keys... optionalWityKeys) {
//		return cmd().click(optionalWityKeys);
//	}
//	/**
//	 * Check a checkbox, readiobutton or combobox option.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder check() {
//		return cmd().check();
//	}
//
//	/**
//	 * Uncheck a checkbox, readiobutton or combobox option.
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder uncheck() {
//		return cmd().uncheck();
//	}
//
//	/**
//	 * Check or uncheck a checkbox, readiobutton or combobox option.
//	 * @param selected
//	 * @return
//	 */
//	@Nonnull
//	public WebDriverCommandBuilder check(boolean selected) {
//		return cmd().check(selected);
//	}


	/**
	 * Wait until the element is not present.
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public WebDriverConnector notPresent(@Nonnull String testid) throws Exception {
		return notPresent(byId(testid));
	}

	/**
	 * Wait until the element is not present.
	 * @deprecated does not work - use other one notPresent(By)
	 * @return
	 */
	@Deprecated
	@Nonnull
	private WebDriverConnector notPresent_notWorking(@Nonnull By locator) {
		WebDriverWait wait = new WebDriverWait(m_driver, m_waitTimeout, m_waitInterval);
		wait.until(ExpectedConditions.not(ExpectedConditions.presenceOfElementLocated(locator)));
		return this;
	}

	/**
	 * Wait until the element is not present. Uses custom timed Action implementation since it seams that built in
	 * ExpectedConditions.not does not work. Consequence is that now notPresent method has throws Exception decoration.
	 * Implementation that uses is ExpectedConditions renamed to notPresent_notWorking and made private for someone else to check too.
	 *
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public WebDriverConnector notPresent(@Nonnull final By locator) throws Exception {
		timed(getWaitTimeout(), getWaitInterval(), new Action<Boolean>() {
			@Override
			@Nullable
			public Boolean execute() throws Exception {
				if(isPresent(locator)) {
					return null;
				}
				return Boolean.TRUE;
			}
		});
		return this;
	}

	/**
	 * Waits until at least specified number of elements located by locator is present.
	 *
	 * @param locator
	 * @param count
	 * @throws Exception
	 */
	@Nonnull
	public WebDriverConnector waitMultiplePresent(@Nonnull final By locator, final int count) throws Exception {
		timed(getWaitTimeout(), getWaitInterval(), new Action<Boolean>() {
			@Override
			@Nullable
			public Boolean execute() throws Exception {
				List<WebElement> elements = driver().findElements(locator);
				return elements.size() >= count ? Boolean.TRUE : null;
			}
		});
		return this;
	}

	//	/** SEEMS TO BE NONSENSE - REMOVED
	//	 * Waits until at specified number of elements located by locator is not present.
	//	 *
	//	 * @param locator
	//	 * @param count
	//	 * @throws Exception
	//	 */
	//	@Nonnull
	//	public TUtilWebdriver waitMultipleNotPresent(@Nonnull final By locator, final int count) throws Exception {
	//		timed(getWaitTimeout(), getWaitInterval(), new Action<Boolean>() {
	//			@Nullable
	//			public Boolean execute() throws Exception {
	//				List<WebElement> elements = driver().findElements(locator);
	//				return elements.size() < count ? Boolean.TRUE : null;
	//			}
	//		});
	//		return this;
	//	}


	/**
	 * Waits until one of listed locators is found in page.
	 * Returns index of a matched element's locator.
	 *
	 * @param locators
	 * @return
	 * @throws Exception
	 */
	public int waitForOneOfElements(final By... locators) throws Exception {
		if(locators.length == 0)
			throw new IllegalStateException("Missing locators");
		Integer value = timed(new Action<Integer>() {
			@Override
			@Nullable
			public Integer execute() throws Exception {
				int index = 0;
				for(By locator : locators) {
					if(isPresent(locator)) {
						return Integer.valueOf(index);
					}
					index++;
				}
				return null;
			}
		});
		return value.intValue();
	}

	/**
	 * Waits until none of listed locators is found in page.
	 *
	 * @param locators
	 * @return
	 * @throws Exception
	 */
	public void waitForNoneOfElementsPresent(final @Nonnull By... locators) throws Exception {
		if(locators.length == 0)
			throw new IllegalStateException("Missing locators");
		Boolean value = timed(new Action<Boolean>() {
			@Override
			@Nullable
			public Boolean execute() throws Exception {
				for(By locator : locators) {
					if(isPresent(locator)) {
						return null;
					}
				}
				return Boolean.TRUE;
			}
		});
	}


	/**
	 * Waits until one of listed locators is found and visible in page.
	 * Returns index of a matched element's locator.
	 *
	 * @param locators
	 * @return
	 * @throws Exception
	 */
	public int waitForOneOfElementsVisible(final By... locators) throws Exception {
		if(locators.length == 0)
			throw new IllegalStateException("Missing locators");
		Integer value = timed(new Action<Integer>() {
			@Override
			@Nullable
			public Integer execute() throws Exception {
				int index = 0;
				for(By locator : locators) {
					if(isPresent(locator) && isVisible(locator)) {
						return Integer.valueOf(index);
					}
					index++;
				}
				return null;
			}
		});
		return value.intValue();
	}

	@Nonnull
	public String waitValuePresent(@Nonnull final By locator, @Nullable final String text) throws Exception {
		wait(locator);
		return timed(new Action<String>() {
			@Override
			@Nullable
			public String execute() throws Exception {
				String value = getValue(locator);
				if(value == null)
					return null;
				if(text == null)
					return value;
				value = value.trim();
				if(value.equalsIgnoreCase(text.trim()))
					return value;
				return null;
			}
		});
	}

	@Nonnull
	public String waitValueNotEmpty(@Nonnull final By locator) throws Exception {
		wait(locator);
		return timed(new Action<String>() {
			@Override
			@Nullable
			public String execute() throws Exception {
				String value = getValue(locator);
				if(value == null)
					return null;
				value = value.trim();
				if(value.length() > 0)
					return value;
				return null;
			}
		});
	}

	@Nonnull
	public String waitHtmlTextPresent(@Nonnull final By locator, @Nullable final String text) throws Exception {
		wait(locator);
		return timed(new Action<String>() {
			@Override
			@Nullable
			public String execute() throws Exception {
				String value = getHtmlText(locator);
				if(value == null)
					return null;
				if(text == null)
					return value;
				value = value.trim();
				if(value.equalsIgnoreCase(text.trim()))
					return value;
				return null;
			}
		});
	}

	/**
	 * Wait for a link with the specified text, then click it.
	 * @param linkText
	 * @return
	 */
	public void clickLink(@Nonnull String linkText, Keys... optionalWithKeys) {
		internalClick(By.linkText(linkText), optionalWithKeys);
	}

	/**
	 * DO NOT USE - DANGEROUS- Click on the button that has the specified title= attribute.
	 * @param title
	 */
	@Deprecated
	public void clickButtonTitled(@Nonnull String title) {
		cmd().click().on(By.cssSelector("button[title='" + title + "']"));
	}

	/**
	* @see org.openqa.selenium.JavascriptExecutor#executeScript(String, Object...)
	*/
	@Nonnull
	public String executeScript(@Nonnull String javaScriptFunction) {
		if(m_driver instanceof JavascriptExecutor) {
			Object result = ((JavascriptExecutor) m_driver).executeScript(javaScriptFunction);
			if(result == null) {
				return "";
			} else {
				return result.toString();
			}
		} else {
			throw new IllegalStateException("Can't execute the script because the driver is not of type JavascriptExecutor");
		}
	}

	/**
	 * Search for all elements with provided locator
	 * @param locator
	 * @return
	 */
	public List<WebElement> findElements(By locator) {
		return driver().findElements(locator);
	}

	@Nullable
	public WebElement findElement(By locator) {
		return driver().findElement(locator);
	}

	@Nullable
	public WebElement findElement(String testid) {
		return driver().findElement(byId(testid));
	}

	@Nonnull
	public WebElement getElement(String testId) {
		WebElement element = findElement(testId);
		if(null == element)
			throw new ElementNotFoundException("testID " + testId);
		return element;
	}

	@Nonnull
	public WebElement getElement(String testId, String extraCss) {
		WebElement element = findElement(testId, extraCss);
		if(null == element)
			throw new ElementNotFoundException("testID " + testId + " and " + extraCss);
		return element;
	}


	@Nullable
	public WebElement findElement(String testId, String extraCss) {
		return findElement(byId(testId, extraCss));
	}

	@Nonnull
	public WebElement getElement(By by) {
		WebElement element = findElement(by);
		if(null == element)
			throw new ElementNotFoundException("testID " + by.toString());
		return element;
	}

	static public void onTestFailure(@Nonnull WebDriverConnector wd, @Nullable Method failedMethod) throws Exception {
		//-- Make a screenshot
		System.out.println("@onTestFailure: attempting to create a screenshot");
		File tmpf = File.createTempFile("screenshot", ".png");

		/*
		 * Selenium aborts if we try to capture a screenshot with an alert on-screen 8-( So if that
		 * abort happens we try to handle the alert, then try again. Since the alert probably
		 * contains the reason why the test failed we try to get it's message since the screenshot
		 * will not contain it - but the message is at least logged.
		 */
		try {
			wd.screenshot(tmpf);
		} catch(UnhandledAlertException x) {
			System.err.println("@onTestFailure: alert present when taking screenshot - trying to get rid of it");

			//-- Try to handle alert
			try {
				String message = wd.alertGetMessage();
				System.err.println("@onTestFailure: alert message is:\n" + message);
			} catch(Exception xx) {
				System.err.println("@onTestFailure: exception accepting alert");
				xx.printStackTrace();
			}

			//-- Try screenshot a 2nd time, die this time if it fails again
			wd.screenshot(tmpf);
		}
		System.out.println("Made screenshot in " + tmpf);
		if(tmpf.exists()) {
			IPaterContext context = Pater.context();
//			System.out.println("@onTestFailure: context = " + context);
			context.registerResult("Screenshot at the end of the failed test", "image/png", tmpf);
			tmpf.delete();
		}
	}

	/**
	 * Wait for the window handle to disappear.
	 * @throws Exception
	 */
	public void waitForHandleNotPresent() throws Exception {
		try {
			timed(getWaitTimeout(), getWaitInterval(), new Action<Boolean>() {
				@Override
				@Nullable
				public Boolean execute() throws Exception {
					if(!driver().getWindowHandles().contains(driver().getWindowHandle())) {
						return null;
					}
					return Boolean.FALSE;
				}
			});
		} catch(NoSuchWindowException e) {
			//Already gone, fine!
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Repeated stuff with timeouts.						*/
	/*--------------------------------------------------------------*/

	public interface Action<T> {
		@Nullable
		T execute() throws Exception;
	}

	/**
	 * Run the specified action repeatedly, with [interval] millis between tries, until it returns a nonnull value. If
	 * the timeout specified in seconds is exceeded throw an exception.
	 */
	@Nonnull
	<T> T timed(long timeoutSeconds, long intervalMillis, @Nonnull Action<T> action) throws Exception {
		long ts = System.currentTimeMillis() + (timeoutSeconds * 1000);
		int count = 0;
		for(;;) {
			T value = action.execute();
			if(null != value)
				return value;

			if(count++ >= 2) {									// Make sure we execute at least 2x.
				long cts = System.currentTimeMillis();
				if(cts >= ts)
					throw new IllegalStateException("Wait timeout exceeded");
			}
			Thread.sleep(intervalMillis);								// Explicitly allow InterruptedException so that thread can be killed properly.
		}
	}

	@Nonnull
	public <T> T timed(@Nonnull Action<T> action) throws Exception {
		return timed(getWaitTimeout(), getWaitInterval(), action);
	}


	/**
	 * Make sure that passed in testClass has properly annotated @Category({GroupRunsSlow.class}) or ancestor in order to use custom defined timeouts. Use only as last option when test has to wait more for conditions to happen.
	 *
	 * @param timeoutSeconds
	 * @param testClass
	 * @param action
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	public <T> T timed(long timeoutSeconds, @Nonnull Class< ? > testClass, @Nonnull Action<T> action) throws Exception {
		org.junit.experimental.categories.Category category = testClass.getAnnotation(org.junit.experimental.categories.Category.class);
		boolean isAnnotatedAsRunSlow = false;
		if(category != null) {
			for(Class< ? > value : category.value()) {
				if(to.etc.puzzler.GroupRunsSlow.class.isAssignableFrom(value)) {
					isAnnotatedAsRunSlow = true;
				}
			}
		}
		if(!isAnnotatedAsRunSlow) {
			throw new IllegalStateException("Waiting tests must be annotated as slow ones using GroupRunsSlow @Category annotation");
		}
		return timed(timeoutSeconds, getWaitInterval(), action);
	}
	/*--------------------------------------------------------------*/
	/*	CODING:	Keyboard and mouse.									*/
	/*--------------------------------------------------------------*/

	/**
	 * Press keys that are send to the complete browser window; used for pressing acceleration keys on buttons for example.
	 *
	 * @param keys
	 */
	public void pressKeys(@Nonnull CharSequence... keys) throws Exception {
		pressKeys(By.cssSelector("body"), keys);
	}

	/**
	 * Press keys that are send to the specified element.
	 *
	 * @param locator
	 * @param keys
	 */
	public void pressKeys(@Nonnull By locator, @Nonnull CharSequence... keys) throws Exception {
		WebElement elem = driver().findElement(locator);
		elem.sendKeys(Keys.chord(keys));
		handleAfterCommandCallback();
	}

	/**
	 * Focus the specified element.
	 * @param testid
	 * @return
	 */
	public void focus(@Nonnull String testid) {
		focus(byId(testid));
	}

	/**
	 * Focus the specified element.
	 * @param locator
	 * @return
	 */
	public void focus(@Nonnull By locator) {
		new Actions(driver()).moveToElement(driver().findElement(locator)).perform();
	}

	public void keyUp(@Nonnull String testid, @Nonnull Keys... keys) {
		keyUp(byId(testid), keys);
	}

	public void keyUp(@Nonnull By locator, @Nonnull Keys... keys) {
		Actions a = new Actions(driver()).moveToElement(driver().findElement(locator));
		for(Keys k: keys)
			a.keyUp(k);
		a.perform();
	}

	public void keyDown(@Nonnull String testid, @Nonnull Keys... keys) {
		keyDown(byId(testid), keys);
	}

	public void keyDown(@Nonnull By locator, @Nonnull Keys... keys) {
		Actions a = new Actions(driver()).moveToElement(driver().findElement(locator));
		for(Keys k : keys)
			a.keyDown(k);
		a.perform();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Alert handling.										*/
	/*--------------------------------------------------------------*/
	/**
	 * In case that alert is present, accept it, otherwise do nothing
	 */
	public void alertAccept() {
		try {
			Alert alert = driver().switchTo().alert();
			alert.accept();
		} catch(Exception ex) {
			// just ignore
		}
	}

	@Nullable
	public String alertGetMessage() {
		try {
			Alert alert = driver().switchTo().alert();
			String msg = alert.getText();
			alert.accept();
			return msg;
		} catch(Exception ex) {
			return null;
		}
	}

	/**
	 * Used for switching control to the other browser window by UrlPage
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public void switchToWindow(@Nonnull final UrlPage page) throws Exception {
		switchToWindow(page.toString());
	}

	/**
	 * Used for switching control to the other browser window by page name
	 * @param screen
	 * @throws Exception
	 */
	public void switchToWindow(@Nonnull String screen) throws Exception {
		String windowHandler = waitForPopup(screen);
		driver().switchTo().window(windowHandler);
	}

	/**
	 * Used for switching control to the other browser window by its handle.
	 * @param handle
	 */
	public void switchToWindowByHandle(@Nonnull String handle) {
		driver().switchTo().window(handle);
	}

	/**
	 * Used for switching control to a page with string part contained in url.</br>
	 * Used when you are expecting some parameters in your popup
	 * @throws Exception
	 */
	public void switchToByUrlPart(@Nonnull String urlPart) throws Exception {
		switchToWindowByHandle(waitForWithUrlPart(urlPart));
	}

	/**
	 * Get handle from the current window under driver control
	 * @return
	 */
	@Nonnull
	public String getWindowHandle() {
		final String currentWindowHandle = driver().getWindowHandle();
		if(currentWindowHandle == null) {
			throw new NullPointerException("There is no windowHandle for the current page.");
		}
		return currentWindowHandle;
	}

	/**
	 * Expects new popup with specified body #PAGENAME_PARAMETER attribute</br>
	 * Returns handle for expected popup.
	 * @param page
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	private String waitForPopup(@Nonnull final String page) throws Exception {
		alertAccept();
		final String currentWindowHandle = driver().getWindowHandle();
		return timed(getWaitTimeout(), getWaitInterval(), new Action<String>() {
			@Override
			@Nullable
			public String execute() throws Exception {
				Set<String> thisMomentWindows = driver().getWindowHandles();
				String newPopupHandle = null;
				for(String handle : thisMomentWindows) {
					driver().switchTo().window(handle);
					WebElement switchedElement = driver().findElement(locator("body[" + PAGENAME_PARAMETER + "]"));
					String switchedPage = switchedElement.getAttribute(PAGENAME_PARAMETER).toLowerCase();
					if(page.toLowerCase().contains(switchedPage)) {
						newPopupHandle = handle;
						break;
					}
				}
				return newPopupHandle;
			}
		});
	}

	/**
	 * Expects popup page with string part contained in url.</br>
	 * Good for searching by parameters
	 * Return handle for expected page with defined string in url.
	 * @param urlPart
	 * @return
	 * @throws Exception
	 */
	@Nonnull
	private String waitForWithUrlPart(@Nonnull final String urlPart) throws Exception {
		return timed(getWaitTimeout(), getWaitInterval(), new Action<String>() {
			@Override
			@Nullable
			public String execute() throws Exception {
				Set<String> thisMomentWindows = driver().getWindowHandles();
				String newPopupHandle = null;
				for(String handle : thisMomentWindows) {
					driver().switchTo().window(handle);
					if(driver().getCurrentUrl().toLowerCase().contains(urlPart)) {
						newPopupHandle = handle;
						break;
					}
				}
				return newPopupHandle;
			}
		});
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Assertion helpers.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Force the test to fail with the specified message.
	 * @param message
	 */
	final public static void fail(@Nonnull String message) {
		Assert.fail(message);
	}

	final public void assertTrue(@Nonnull String message, boolean condition) {
		Assert.assertTrue(message, condition);
	}

	final public void assertTrue(boolean condition) {
		Assert.assertTrue(condition);
	}

	final public void assertFalse(@Nonnull String message, boolean condition) {
		Assert.assertFalse(message, condition);
	}

	final public void assertFalse(boolean condition) {
		Assert.assertFalse(condition);
	}

	final public void assertEquals(@Nullable Object actual, @Nullable Object expected) {
		Assert.assertEquals(actual, expected);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Assertions.											*/
	/*--------------------------------------------------------------*/
	/** Compares two strings, but handles "regexp:" strings like HTML Selenese
	 *
	 * @param expectedPattern
	 * @param actual
	 * @return true if actual matches the expectedPattern, or false otherwise
	 */
	public static boolean seleniumEquals(String expectedPattern, String actual) {
		if(actual.startsWith("regexp:") || actual.startsWith("regex:") || actual.startsWith("regexpi:") || actual.startsWith("regexi:")) {
			// swap 'em
			String tmp = actual;
			actual = expectedPattern;
			expectedPattern = tmp;
		}
		Boolean b;
		b = handleRegex("regexp:", expectedPattern, actual, 0);
		if(b != null) {
			return b.booleanValue();
		}
		b = handleRegex("regex:", expectedPattern, actual, 0);
		if(b != null) {
			return b.booleanValue();
		}
		b = handleRegex("regexpi:", expectedPattern, actual, Pattern.CASE_INSENSITIVE);
		if(b != null) {
			return b.booleanValue();
		}
		b = handleRegex("regexi:", expectedPattern, actual, Pattern.CASE_INSENSITIVE);
		if(b != null) {
			return b.booleanValue();
		}

		if(expectedPattern.startsWith("exact:")) {
			String expectedExact = expectedPattern.replaceFirst("exact:", "");
			if(!expectedExact.equals(actual)) {
				System.out.println("expected " + actual + " to match " + expectedPattern);
				return false;
			}
			return true;
		}

		String expectedGlob = expectedPattern.replaceFirst("glob:", "");
		expectedGlob = expectedGlob.replaceAll("([\\]\\[\\\\{\\}$\\(\\)\\|\\^\\+.])", "\\\\$1");

		expectedGlob = expectedGlob.replaceAll("\\*", ".*");
		expectedGlob = expectedGlob.replaceAll("\\?", ".");
		if(!Pattern.compile(expectedGlob, Pattern.DOTALL).matcher(actual).matches()) {
			System.out.println("expected \"" + actual + "\" to match glob \"" + expectedPattern + "\" (had transformed the glob into regexp \"" + expectedGlob + "\"");
			return false;
		}
		return true;
	}

	@Nullable
	private static Boolean handleRegex(@Nonnull String prefix, @Nonnull String expectedPattern, @Nonnull String actual, int flags) {
		if(expectedPattern.startsWith(prefix)) {
			String expectedRegEx = expectedPattern.replaceFirst(prefix, ".*") + ".*";
			Pattern p = Pattern.compile(expectedRegEx, flags);
			if(!p.matcher(actual).matches()) {
				System.out.println("expected " + actual + " to match regexp " + expectedPattern);
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		}
		return null;
	}


	public void verifyInvisible(@Nonnull String testid) {
		verifyInvisible(byId(testid));
	}

	public void verifyInvisible(@Nonnull By locator) {
		assertFalse("Locator " + locator + " is not invisible", isVisible(locator));
	}

	public void verifyVisible(@Nonnull String testid) {
		verifyVisible(byId(testid));
	}

	public void verifyVisible(@Nonnull By locator) {
		assertTrue("Locator " + locator + " is not visible", isVisible(locator));
	}

	public void verifyNotEditable(@Nonnull String testid) {
		assertFalse("TestID " + testid + " is editable", isEditable(testid));
	}

	public void verifyEditable(@Nonnull String testid) {
		assertTrue("TestID " + testid + " is not editable", isEditable(testid));
	}

	public void verifyDisabled(@Nonnull String testid) {
		assertFalse("TestID " + testid + " is not disabled", isEnabled(testid));
	}

	public void verifyEnabled(@Nonnull String testid) {
		assertTrue("TestID " + testid + " is not enabled", isEnabled(testid));
	}

	public void verifyPresent(@Nonnull By locator) {
		assertTrue("Locator " + locator + " not present", isPresent(locator));
	}

	public void verifyPresent(@Nonnull String... testIds) {
		for(String testid : testIds)
			verifyPresent(byId(testid));
	}

	public void verifyNotPresent(@Nonnull By locator) {
		assertFalse("Locator " + locator + " is present", isPresent(locator));
	}

	public void verifyNotPresent(@Nonnull String testid) {
		verifyNotPresent(byId(testid));
	}

	public void verifyTextEquals(@Nonnull String testid, @Nonnull String text) {
		verifyTextEquals(byId(testid), text);
	}

	public void verifyTextEquals(@Nonnull By locator, @Nonnull String text) {
		assertEquals(getHtmlText(locator), text);
	}

	public void verifyTextNotEmpty(@Nonnull String testid) {
		assertFalse("TestID " + testid + ": text is empty", StringTool.isBlank(getHtmlText(testid)));
	}

	public void verifyTextContains(@Nonnull String testid, @Nonnull String text) {
		verifyTextContains(byId(testid), text);
	}

	public void verifyTextContains(@Nonnull By locator, @Nonnull String text) {
		String html = getHtmlText(locator);
		assertTrue("Locator " + locator + " does not contain " + text + "(value = " + html + ")", html.toLowerCase().contains(text.toLowerCase()));
	}

	public void verifyTextStartsWith(@Nonnull String testid, @Nonnull String text) {
		verifyTextStartsWith(byId(testid), text);
	}

	public void verifyTextStartsWith(@Nonnull By locator, @Nonnull String text) {
		String html = getHtmlText(locator);
		assertTrue("Locator " + locator + " text does not start with " + text + " (value=" + html + ")", html.toLowerCase().startsWith(text.toLowerCase()));
	}

	public void verifyValue(@Nonnull By locator, @Nullable String value) {
		assertEquals(getValue(locator), value);
	}

	public void verifyValue(@Nonnull String testid, @Nullable String value) {
		verifyValue(byId(testid), value);
	}

	public void verifySelect(@Nonnull String testid, @Nonnull String value) {
		verifySelect(byId(testid), value);
	}

	public void verifySelect(@Nonnull By locator, @Nonnull String value) {
		assertEquals(selectGetSelected(locator, false), value);
	}

	/**
	 * If an alert is present, accept it; if msg is passed it checks if the
	 * alert message text contains that string or it asserts.
	 */
	public void verifyAlert(@Nullable String msg) {
		String amsg = alertGetMessage();
		if(msg != null) {
			if(amsg == null) {
				fail("There is no alert message");
			}
			if(!DomUtil.nullChecked(amsg).toLowerCase().contains(msg)) {
				fail("Alert message '" + amsg + "' does not contain the string '" + msg + "'");
			}
		}
	}

	@Nonnull
	public Map<String, String> getComputedStyles(@Nonnull WebElement element, Predicate<String> filter) {
		JavascriptExecutor executor = (JavascriptExecutor) driver();
		String script = "var s = '';" +
			"var o = getComputedStyle(arguments[0]);" +
			"for(var i = 0; i < o.length; i++){" +
			"s += '~~~~' + o[i] + '``' + o.getPropertyValue(o[i]);}" +
			"return s;";
		String result = (String) executor.executeScript(script, element);

		Map<String, String> res = new TreeMap<>();
		String[] pairs = result.split("~~~~");
		for(String pair : pairs) {
			if(pair.length() != 0) {
				String[] split = pair.split("``");
				if(split.length > 2 || split.length == 0) {
					System.err.println("Failed to split '" + pair + "'");
				} else if(filter.test(split[0])) {
					res.put(split[0], split.length == 1 ? "" : split[1]);
				}
			}
		}
		//System.out.println(res);
		return res;

	}
	@Nonnull
	public Map<String, String> getComputedStyles(@Nonnull WebElement element) {
		return getComputedStyles(element, a -> true);

	}
}
