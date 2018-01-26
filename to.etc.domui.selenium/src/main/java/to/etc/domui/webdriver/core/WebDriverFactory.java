package to.etc.domui.webdriver.core;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeDriverService.Builder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static to.etc.domui.util.DomUtil.nullChecked;

/**
 * Factory to create raw WebDriver instances.
 */
@DefaultNonNull final class WebDriverFactory {
	private static Logger LOG = LoggerFactory.getLogger(WebDriverFactory.class);

	/**
	 * Allocate a WebDriver instance with the specified characteristics.
	 */
	public static WebDriver allocateInstance(WebDriverType type, BrowserModel browser, @Nullable String hubUrl, @Nullable Locale lang) throws Exception {
		if(lang == null) {
			lang = nullChecked(Locale.US);
		}

		if(browser == BrowserModel.PHANTOMJS) {
			return allocatePhantomjsInstance(lang);
		}

		switch(type){
			default:
				throw new IllegalStateException("? unhandled driver type");

			case HTMLUNIT:
				return allocateHtmlUnitInstance(browser, lang);

			case LOCAL:
				return allocateLocalInstance(browser, lang);

			case REMOTE:
				return allocateRemoteInstance(browser, nullChecked(hubUrl), lang);

			case BROWSERSTACK:
				return allocateBrowserStack(browser, Objects.requireNonNull(hubUrl), lang);
		}
	}

	private static WebDriver allocateBrowserStack(BrowserModel browser, String huburl, Locale lang) throws MalformedURLException {
		if(huburl.startsWith(WebDriverConnector.BROWSERSTACK)) {
			huburl = huburl.substring(WebDriverConnector.BROWSERSTACK.length());
		}

		//-- Format is browserstack://username:password
		String[] spl = huburl.split(":");
		if(spl.length != 2) {
			throw new IllegalStateException("Invalid browserstack:// url: use browserstack://username:key");
		}
		String userName = spl[0];
		String key = spl[1];
		if(key.endsWith("/"))
			key = key.substring(0, key.length() - 1);

		DesiredCapabilities caps = new DesiredCapabilities();
		switch(browser) {
			default:
				throw new IllegalStateException("browserstack: unsupported browser type " + browser);

			case CHROME:
				caps.setCapability("browser", "chrome");
				break;

			case EDGE14:
				caps.setCapability("browser", "Edge");
				caps.setCapability("browser_version", "14");
				caps.setCapability("os_version", "10");
				break;

			case EDGE15:
				caps.setCapability("browser", "Edge");
				caps.setCapability("browser_version", "15");
				caps.setCapability("os_version", "10");
				break;

			case FIREFOX:
				caps.setCapability("browser", "Firefox");
				caps.setCapability("os_version", "8");
				break;

			case IE:
				caps.setCapability("browser", "IE");
				caps.setCapability("browser_version", "11.0");
				caps.setCapability("os_version", "8");
				break;

			case IE10:
				caps.setCapability("browser", "IE");
				caps.setCapability("browser_version", "10.0");
				caps.setCapability("os_version", "8");
				break;

			case IE11:
				caps.setCapability("browser", "IE");
				caps.setCapability("browser_version", "11.0");
				caps.setCapability("os_version", "10");
				break;
		}

		caps.setCapability("os", "Windows");
		caps.setCapability("browserstack.debug", "true");

		//-- Local passthrough support
		caps.setCapability("browserstack.local", "true");
		caps.setCapability("browserstack.localIdentifier", "btest");

		String url = "https://" + userName + ":" + key + "@hub-cloud.browserstack.com/wd/hub";
		return new RemoteWebDriver(new URL(url), caps);
	}

	private static WebDriver allocatePhantomjsInstance(Locale lang) throws Exception {
		DesiredCapabilities capabilities = calculateCapabilities(BrowserModel.PHANTOMJS, lang);
		capabilities.setCapability(CapabilityType.TAKES_SCREENSHOT, "true");

		PhantomJSDriver wd;
		if(false) {
			wd = new PhantomJSDriver(capabilities);
		} else {
			/*
			 * We must have anti-aliasing off for better testing. This should work for unices where Phantomjs has been
			 * compiled with FontConfig support..
			 */

			//-- Set the XDG_CONFIG_HOME envvar; this is used by fontconfig as one of its locations
			File dir = createFontConfigFile();
			Map<String, String> env = new HashMap<>();
			env.put("XDG_CONFIG_HOME", dir.getParentFile().getAbsolutePath());

			PhantomJSDriverService service = MyPhantomDriverService.createDefaultService(capabilities, env);
			wd = new PhantomJSDriver(service, capabilities);
		}

		wd.manage().window().setSize(new Dimension(1280, 1024));
		String browserName = wd.getCapabilities().getBrowserName();
		String version = wd.getCapabilities().getVersion();
		System.out.println("wd: allocated " + browserName + " " + version);
		return wd;
	}

	@Nonnull private static File createFontConfigFile() throws IOException {
		//-- 1. Make a temp directory which will contain our fonts.conf
		String tmp = System.getProperty("java.io.tmpdir");
		if(tmp == null) {
			tmp = "/tmp";
		}
		File dir = new File(tmp + File.separator + "/_phantomjs-config/fontconfig");
		dir.mkdirs();
		if(!dir.exists()) {
			throw new IOException("Can't create fontconfig directory to override phantomjs font settings at " + dir);
		}

		File conf = new File(dir, "fonts.conf");
		String text = "<match target=\"font\">\n"
			+ "<edit mode=\"assign\" name=\"antialias\">\n"
			+ "<bool>false</bool>\n"
			+ "</edit>\n"
			+ "</match>";
		try(FileOutputStream fos = new FileOutputStream(conf)) {
			fos.write(text.getBytes("UTF-8"));
		}

		return dir;
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

			case CHROME_HEADLESS:
				return getChromeHeadlessCapabilities(lang);


			case IE:
			case IE9:
			case IE10:
			case IE11:
			case EDGE14:
			case EDGE15:
				return getIECapabilities(browser, lang);
		}
	}

	private static WebDriver allocateLocalInstance(BrowserModel browser, Locale lang) throws IOException {
		switch(browser){
			default:
				throw new IllegalStateException("Unsupported browser type " + browser.getCode() + " for HUB test execution");

			case FIREFOX:
				return allocateFirefoxDriver(lang);

			case CHROME:
			case CHROME_HEADLESS:
				return allocateChromeInstance(browser, lang);

			case IE:
			case IE9:
			case IE10:
			case IE11:
			case EDGE14:
			case EDGE15:
				return allocateIEDriver(browser, lang);
		}
	}

	@NotNull private static WebDriver allocateIEDriver(BrowserModel browser, Locale lang) {
		InternetExplorerDriver wd = new InternetExplorerDriver(getIECapabilities(browser, lang));
		String browserName = wd.getCapabilities().getBrowserName();
		String version = wd.getCapabilities().getVersion();
		System.out.println("wd: allocated " + browserName + " " + version);
		return wd;
	}

	@NotNull private static WebDriver allocateFirefoxDriver(Locale lang) throws IOException {
		FirefoxOptions fo = new FirefoxOptions();

		////-- Set the XDG_CONFIG_HOME envvar; this is used by fontconfig as one of its locations
		//File dir = createFontConfigFile();
		//FirefoxBinary ffb = new FirefoxBinary();
		//ffb.setEnvironmentProperty("XDG_CONFIG_HOME", dir.getParentFile().getAbsolutePath());
		//FirefoxOptions ffo = new FirefoxOptions(getFirefoxCapabilities(lang));
		//ffo.setBinary(ffb);
		//FirefoxDriver wd = new FirefoxDriver(ffo);

		replaceDotFonts();

		FirefoxDriver wd = new FirefoxDriver(getFirefoxCapabilities(lang));
		String browserName = wd.getCapabilities().getBrowserName();
		String version = wd.getCapabilities().getVersion();
		System.out.println("wd: allocated " + browserName + " " + version);
		return wd;
	}

	static private boolean m_dotFontChecked;

	private static synchronized void replaceDotFonts() throws IOException {
		if(m_dotFontChecked)
			return;

		File home = new File(System.getProperty("user.home"));
		final File dotfont = new File(home, ".fonts.conf");
		final File dotbackup = new File(home, "fonts.conf.backup");
		if(! dotbackup.exists()) {
			if(dotfont.exists()) {
				if(! dotfont.renameTo(dotbackup)) {
					throw new IOException("Cannot rename " + dotfont + " to " + dotbackup);
				}
			}
		}

		//-- Now write a new .fonts.conf
		String text = "<match target=\"font\">\n"
			+ "<edit mode=\"assign\" name=\"antialias\">\n"
			+ "<bool>false</bool>\n"
			+ "</edit>\n"
			+ "</match>";
		try(FileOutputStream fos = new FileOutputStream(dotfont)) {
			fos.write(text.getBytes("UTF-8"));
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					if(!dotfont.delete()) {
						System.err.println("FAILED TO DELETE " + dotfont);
					}
					if(dotbackup.exists()) {
						if(! dotbackup.renameTo(dotfont)) {
							System.err.println("FAILED TO RENAME " + dotbackup + " back to " + dotfont);
						}
					}

				} catch(Exception x) {
					x.printStackTrace();
				}
			}
		});
		m_dotFontChecked = true;
	}

	private static WebDriver allocateChromeInstance(BrowserModel model, Locale lang) throws IOException {
		DesiredCapabilities dc;
		switch(model) {
			default:
				throw new IllegalStateException("Unsupported browser type " + model.getCode() + " for local execution");

			case CHROME:
				dc = getChromeCapabilities(lang);
				break;

			case CHROME_HEADLESS:
				dc = getChromeHeadlessCapabilities(lang);
				break;
		}

		TestProperties tp = TUtilTestProperties.getTestProperties();
		String chromeBinariesLocation = tp.getProperty("webdriver.chrome.driver", "/usr/bin/google-chrome");
		System.setProperty("webdriver.chromedriver", chromeBinariesLocation);
		dc.setCapability("chrome.binary", chromeBinariesLocation);

		//-- Set the XDG_CONFIG_HOME envvar; this is used by fontconfig as one of its locations
		File dir = createFontConfigFile();
		Map<String, String> env = new HashMap<>();
		env.put("XDG_CONFIG_HOME", dir.getParentFile().getAbsolutePath());

		Builder builder = new Builder();
		builder.usingAnyFreePort();
		builder.withEnvironment(env);
		ChromeDriverService service = builder.build();
		MyChromeDriver chromeDriver = new MyChromeDriver(service, dc);

		chromeDriver.manage().window().setSize(new Dimension(1280, 1024));

		String browserName = chromeDriver.getCapabilities().getBrowserName();
		String version = chromeDriver.getCapabilities().getVersion();
		System.out.println("wd: allocated " + browserName + " " + version);

		return chromeDriver;
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
			case EDGE14:
				dc.setVersion("10");
				break;
			case EDGE15:
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

		//-- Not supported anymore, see https://github.com/mozilla/geckodriver/issues/617
		//capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		return capabilities;
	}

	private static DesiredCapabilities getChromeCapabilities(Locale lang) {
		ChromeOptions options = getCommonChromeOptions(lang);
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return capabilities;
	}

	private static DesiredCapabilities getChromeHeadlessCapabilities(Locale lang) {
		ChromeOptions options = getCommonChromeOptions(lang);
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		return capabilities;
	}

	@NotNull private static ChromeOptions getCommonChromeOptions(Locale lang) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments(
			"test-type");                    // This gets rid of the message "You are using an unsupported command-line flag: --ignore-certificate-errors. Stability and security will suffer."
		options.addArguments("lang=" + lang.getLanguage().toLowerCase());
		options.addArguments("intl.accept_languages=" + lang.getLanguage().toLowerCase());

		return options;
	}


	private static DesiredCapabilities getPhantomCapabilities(Locale lang) {
		DesiredCapabilities capabilities = DesiredCapabilities.phantomjs();
		String value = lang.getLanguage().toLowerCase();
		capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX + "Accept-Language", value);
		capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);
		return capabilities;
	}


	@Nullable
	public static IWebdriverScreenshotHelper getScreenshotHelper(WebDriverType webDriverType, BrowserModel browserModel) {
		switch(webDriverType) {
			default:
				break;

			case HTMLUNIT:
				//-- HTMLUNIT does not render, so it cannot create screenshots.
				return null;
		}

		switch(browserModel) {
			default:
				return new DefaultScreenshotHelper();

			case CHROME:
			case CHROME_HEADLESS:
				return new ChromeScreenshotHelper();
		}
	}
}
