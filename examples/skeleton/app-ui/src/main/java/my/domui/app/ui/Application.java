package my.domui.app.ui;

import my.domui.app.core.Constants;
import my.domui.app.core.authentication.LoginAuthenticator;
import my.domui.app.core.db.DbUtil;
import my.domui.app.ui.pages.HomePage;
import my.domui.app.ui.pages.login.LoginPage;
import org.slf4j.bridge.SLF4JBridgeHandler;
import to.etc.dbpool.StatisticsRequestListener;
import to.etc.domui.dom.header.FaviconContributor;
import to.etc.domui.dom.header.HeaderContributor;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.login.ILoginDialogFactory;
import to.etc.domui.server.ConfigParameters;
import to.etc.domui.server.DomApplication;
import to.etc.domui.themes.sass.SassThemeFactory;
import to.etc.util.DeveloperOptions;
import to.etc.util.StringTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Application extends DomApplication {

	static private final Locale DUTCH = new Locale("nl", "NL");

	@Nullable @Override public Class<? extends UrlPage> getRootPage() {
		return HomePage.class;
	}

	@Override protected void initialize(@Nonnull ConfigParameters pp) throws Exception {
		setKeepAliveInterval(60);

		//-- Redirect all JUL logging to slf4j
		LogManager.getLogManager().reset();
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		Logger.getLogger("global").setLevel(Level.FINEST);

		fixKeyLength();
		StatisticsRequestListener.setForceEncoding("utf-8");
		StatisticsRequestListener.setSessionStatistics(true);
		setKeepAliveInterval(2 * 60 * 1000);
		setShowProblemTemplate(true);
		setDefaultThemeFactory(SassThemeFactory.INSTANCE);

		addHeaderContributor(HeaderContributor.loadStylesheet("css/appstyle.scss"), 10);

		if(false) {
			// FA 5 alpha lacked a lot of icons
			addHeaderContributor(HeaderContributor.loadStylesheet("css/font-awesome-core.css"), 10);
			addHeaderContributor(HeaderContributor.loadStylesheet("css/font-awesome-regular.css"), 10);
			addHeaderContributor(HeaderContributor.loadStylesheet("css/font-awesome-light.css"), 10);
			//addHeaderContributor(HeaderContributor.loadStylesheet("css/"), 10);
		} else {
			addHeaderContributor(HeaderContributor.loadStylesheet("css/font-awesome.min.css"), 10);
		}
		addHeaderContributor(new FaviconContributor("img/favicon.png"), 100);

		//-- Read properties file, then initialize the database and other stuff
		File propertyFile = getPropertyFile();
		Properties properties = getProperties(propertyFile);

		//-- Some hosting providers to not properly pass the application name when proxying, so allow it to be set manually.
		String appUrl = properties.getProperty("application.url");            // Deep, deep sigh.
		if(null != appUrl)
			setApplicationURL(appUrl);

		initializeDatabase(propertyFile, properties);

		LoginAuthenticator loginAuthenticator = new LoginAuthenticator();
		defineLoginAndLoginPage(loginAuthenticator);
		registerNewPageListener();
	}

	@Nonnull @Override public Locale getRequestLocale(HttpServletRequest request) {
		return DUTCH;
	}

	private Properties getProperties(File propertyFile) throws Exception {

		Properties p = new Properties();
		try(InputStream is = new FileInputStream(propertyFile)) {
			p.load(is);
		}
		return p;
	}

	@Nonnull private File getPropertyFile() {
		String name = System.getProperty("config");
		if(null != name) {
			File f = new File(name);
			if(! f.exists())
				throw new IllegalStateException("The config file " + f.getAbsolutePath() + " cannot be found");
			return f;
		}

		//-- Get the required name of the file.
		name = DeveloperOptions.getString("domui." + Constants.APPCODE + ".config", Constants.APPCODE + ".properties");

		File pf;
		if(name.startsWith(File.separator)) {
			pf = new File(name);
		} else {
			//-- Relative: try several paths
			String home = System.getProperty("user.home");
			pf = new File(home + File.separator + "." + Constants.APPCODE + File.separator + name);
			if(pf.exists())
				return pf;

			pf = new File(home + File.separator + name);
			if(pf.exists())
				return pf;
			pf = getAppFile("WEB-INF/" + name);
		}
		if(!pf.exists())
			throw new IllegalStateException("The config file " + pf + " does not exist");
		return pf;
	}

	private void initializeDatabase(File propFile, Properties properties) throws Exception {
		DbUtil.initialize(propFile, properties, "app");
	}

	private void defineLoginAndLoginPage(LoginAuthenticator loginAuthenticator) {
		setLoginAuthenticator(loginAuthenticator);
		setLoginDialogFactory(new ILoginDialogFactory() {
			@Nonnull
			@Override
			public String getLoginRURL(final String originalTarget) {
				StringBuilder sb = new StringBuilder();
				sb.append(LoginPage.class.getName() + ".ui?target=");
				StringTool.encodeURLEncoded(sb, originalTarget);
				return sb.toString();
			}

			@Nullable
			@Override
			public String getAccessDeniedURL() {
				return null; 								// Just use the built-in DomUI access denied page
			}
		});
	}

	private void registerNewPageListener() {
		//addNewPageInstantiatedListener(new INewPageInstantiated() {
		//	@Override
		//	public void newPageCreated(@Nonnull UrlPage body) throws Exception {
		//	}
		//
		//	@Override
		//	public void newPageBuilt(@Nonnull UrlPage body) throws Exception {
		//		//System.out.println("PAGE BUILT");
		//		if(body instanceof INoTopBar)
		//			return;
		//
		//		if(body.getTitle() == null)
		//			body.setTitle("DomUI Application");
		//
		//		if(body.getDeepChildren(TopBar.class).size() != 0) {
		//			return;
		//		}
		//
		//		//-- Add a topbar in between the page's 1st children.
		//		int top = -1;
		//		int ttl = -1;
		//		int ix = 0;
		//		for(NodeBase b : body) {
		//			if(b.getClass().getName().equals(TopBar.class.getName())) {
		//				top = ix;
		//			} else if(b.getClass().getName().equals(AppPageTitleBar.class.getName())) {
		//				ttl = ix;
		//			}
		//			ix++;
		//		}
		//		if(ttl >= 0 && top >= 0)
		//			return;
		//
		//		if(top < 0) {
		//			//-- Only add the top bar to pages not explicitly forbidding it..
		//			String name = body.getClass().getName();
		//			if(!name.startsWith("to.etc.dbdiscoverer")) {
		//				NodeContainer d = body.getDelegate();
		//				body.delegateTo(null);
		//				body.add(0, new TopBar());
		//				body.delegateTo(d);
		//			}
		//		}
		//	}
		//});
	}


	public static void fixKeyLength() {
		String errorString = "Failed manually overriding key-length permissions.";
		int newMaxKeyLength;
		try {
			if((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
				Class<?> c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
				Constructor<?> con = c.getDeclaredConstructor();
				con.setAccessible(true);
				Object allPermissionCollection = con.newInstance();
				Field f = c.getDeclaredField("all_allowed");
				f.setAccessible(true);
				f.setBoolean(allPermissionCollection, true);

				c = Class.forName("javax.crypto.CryptoPermissions");
				con = c.getDeclaredConstructor();
				con.setAccessible(true);
				Object allPermissions = con.newInstance();
				f = c.getDeclaredField("perms");
				f.setAccessible(true);
				((Map<String, Object>) f.get(allPermissions)).put("*", allPermissionCollection);

				c = Class.forName("javax.crypto.JceSecurityManager");
				f = c.getDeclaredField("defaultPolicy");
				f.setAccessible(true);
				Field mf = Field.class.getDeclaredField("modifiers");
				mf.setAccessible(true);
				mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				f.set(null, allPermissions);

				newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
			}
		} catch(Exception e) {
			throw new RuntimeException(errorString, e);
		}
		if(newMaxKeyLength < 256)
			throw new RuntimeException(errorString); // hack failed
	}

	@Override protected void destroy() {
	}
}
