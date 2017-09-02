package to.etc.domui.webdriver.core;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import to.etc.webapp.testsupport.TUtilTestProperties;
import to.etc.webapp.testsupport.TestProperties;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.util.Locale;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * Factory to create raw WebDriver instances.
 */
@DefaultNonNull
final class WebDriverFactory {
	private static Logger LOG = LoggerFactory.getLogger(WebDriverFactory.class);

	/**
	 * Allocate a WebDriver instance with the specified characteristics.
	 */
	public static WebDriver allocateInstance(WebDriverType type, BrowserModel browser, @Nullable String hubUrl, @Nullable Locale lang) throws Exception {
		if(lang == null) {
			lang = nullChecked(Locale.ENGLISH);
		}

		if(browser == BrowserModel.PHANTOMJS) {
			return allocatePhantomjsInstance(lang);
		}

		switch(type) {
			default:
				throw new IllegalStateException("? unhandled driver type");

			case HTMLUNIT:
				return allocateHtmlUnitInstance(browser, lang);

			case LOCAL:
				return allocateLocalInstance(browser, lang);

			case REMOTE:
				return allocateRemoteInstance(browser, nullChecked(hubUrl), lang);
		}
	}

	private static WebDriver allocatePhantomjsInstance(Locale lang) throws Exception {
		DesiredCapabilities capabilities = calculateCapabilities(BrowserModel.PHANTOMJS, lang);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, "true");
		PhantomJSDriver wd = new PhantomJSDriver(capabilities);

		wd.manage().window().setSize(new Dimension(1280, 1024));
		return wd;
	}

	private static WebDriver allocateHtmlUnitInstance(BrowserModel browser, Locale lang) throws Exception {
		Capabilities capabilities = calculateCapabilities(browser, lang);
		return new HtmlUnitDriver(capabilities);
	}

	private static WebDriver allocateRemoteInstance(BrowserModel browser, @Nonnull String hubUrl, Locale lang) throws Exception {
		return new RemoteWebDriver(new URL(hubUrl), calculateCapabilities(browser, lang));
	}

	private static DesiredCapabilities calculateCapabilities(BrowserModel browser, Locale lang) throws Exception {
		switch(browser){
			default:
				throw new IllegalStateException("Unsupported browser type " + browser.getCode());

			case FIREFOX:
				return getFirefoxCapabilities(lang);

			case PHANTOMJS:
				return getPhantomCapabilities(lang);

			case CHROME:
				return getChromeCapabilities(lang);

			case IE:
			case IE9:
			case IE10:
			case IE11:
			case EDGE:
				return getIECapabilities(browser, lang);
		}
	}

	private static WebDriver allocateLocalInstance(BrowserModel browser, Locale lang) {
		switch(browser){
			default:
				throw new IllegalStateException("Unsupported browser type " + browser.getCode() + " for HUB test execution");

			case FIREFOX:
				return new FirefoxDriver(getFirefoxCapabilities(lang));

			case CHROME:
				DesiredCapabilities dc = getChromeCapabilities(lang);
				TestProperties tp = TUtilTestProperties.getTestProperties();
				String chromeBinariesLocation = tp.getProperty("webdriver.chrome.driver", "/usr/bin/google-chrome");
				System.setProperty("webdriver.chromedriver", chromeBinariesLocation);
				dc.setCapability("chrome.binary", chromeBinariesLocation);
				return new ChromeDriver(dc);

			case IE:
			case IE9:
			case IE10:
			case IE11:
			case EDGE:
				return new InternetExplorerDriver(getIECapabilities(browser, lang));
		}
	}

	private static DesiredCapabilities getIECapabilities(BrowserModel browser, Locale lang) {
		LOG.warn("Language for IE is still not supported! Language found: [" + lang.getLanguage() + "]");
		DesiredCapabilities dc = DesiredCapabilities.internetExplorer();
		dc.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		switch(browser){
			default:
				throw new IllegalStateException("Unsupported IE browser version " + browser.getCode());
			case IE:
				//no specific version required
				LOG.warn("Unspecified IE browser version");
				break;
			case IE9:
				dc.setVersion("9");
				break;
			case IE10:
				dc.setVersion("10");
				break;
			case IE11:
				dc.setVersion("11");
				break;
			case EDGE:
				dc.setVersion("11");
				break;
		}
		return dc;
	}

	private static DesiredCapabilities getFirefoxCapabilities(Locale lang) {
		FirefoxProfile profile = new FirefoxProfile();
		profile.setPreference("intl.accept_languages", lang.getLanguage().toLowerCase());
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();
		capabilities.setCapability(FirefoxDriver.PROFILE, profile);
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		return capabilities;
	}

	private static DesiredCapabilities getChromeCapabilities(Locale lang) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("test-type"); 					// This gets rid of the message "You are using an unsupported command-line flag: --ignore-certificate-errors. Stability and security will suffer."
		options.addArguments("lang=" + lang.getLanguage().toLowerCase());
		options.addArguments("intl.accept_languages=" + lang.getLanguage().toLowerCase());
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return capabilities;
	}

	private static DesiredCapabilities getPhantomCapabilities(Locale lang) {
		//
		//ChromeOptions options = new ChromeOptions();
		//options.addArguments("test-type"); 					// This gets rid of the message "You are using an unsupported command-line flag: --ignore-certificate-errors. Stability and security will suffer."
		//options.addArguments("lang=" + lang.getLanguage().toLowerCase());
		//options.addArguments("intl.accept_languages=" + lang.getLanguage().toLowerCase());

		DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
		capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language", lang.toString());
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		return capabilities;
	}


}
