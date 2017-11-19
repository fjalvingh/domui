package to.etc.domui.webdriver.core;

import com.google.common.base.Preconditions;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.os.ExecutableFinder;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.phantomjs.PhantomJSDriverService.Builder;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2-9-17.
 */
public class MyPhantomDriverService {
	public static PhantomJSDriverService createDefaultService(Capabilities desiredCapabilities, Map<String, String> env) {
		Proxy proxy = null;
		if (desiredCapabilities != null) {
			proxy = Proxy.extractFrom(desiredCapabilities);
		}

		File phantomjsfile = findPhantomJS(desiredCapabilities, "https://github.com/ariya/phantomjs/wiki", "http://phantomjs.org/download.html");
		File ghostDriverfile = findGhostDriver(desiredCapabilities, "https://github.com/detro/ghostdriver/blob/master/README.md", "https://github.com/detro/ghostdriver/downloads");
		Builder builder = new Builder();
		builder.usingPhantomJSExecutable(phantomjsfile)
			.usingGhostDriver(ghostDriverfile)
			.usingAnyFreePort()
			.withProxy(proxy)
			.withLogFile(new File("phantomjsdriver.log"))
			.usingCommandLineArguments(findCLIArgumentsFromCaps(desiredCapabilities, "phantomjs.cli.args"))
			.usingGhostDriverCommandLineArguments(findCLIArgumentsFromCaps(desiredCapabilities, "phantomjs.ghostdriver.cli.args"));
		if(null != env)
			builder.withEnvironment(env);
		return builder.build();
	}

	public static File findPhantomJS(Capabilities desiredCapabilities, String docsLink, String downloadLink) {
		String phantomjspath;
		if (desiredCapabilities != null && desiredCapabilities.getCapability("phantomjs.binary.path") != null) {
			phantomjspath = (String)desiredCapabilities.getCapability("phantomjs.binary.path");
		} else {
			phantomjspath = (new ExecutableFinder()).find("phantomjs");
			phantomjspath = System.getProperty("phantomjs.binary.path", phantomjspath);
		}

		Preconditions.checkState(phantomjspath != null, "The path to the driver executable must be set by the %s capability/system property/PATH variable; for more information, see %s. The latest version can be downloaded from %s", "phantomjs.binary.path", docsLink, downloadLink);
		File phantomjs = new File(phantomjspath);
		checkExecutable(phantomjs);
		return phantomjs;
	}

	protected static File findGhostDriver(Capabilities desiredCapabilities, String docsLink, String downloadLink) {
		String ghostdriverpath;
		if (desiredCapabilities != null && desiredCapabilities.getCapability("phantomjs.ghostdriver.path") != null) {
			ghostdriverpath = (String)desiredCapabilities.getCapability("phantomjs.ghostdriver.path");
		} else {
			ghostdriverpath = System.getProperty("phantomjs.ghostdriver.path");
		}

		if (ghostdriverpath != null) {
			File ghostdriver = new File(ghostdriverpath);
			Preconditions.checkState(ghostdriver.exists(), "The GhostDriver does not exist: %s", ghostdriver.getAbsolutePath());
			Preconditions.checkState(ghostdriver.isFile(), "The GhostDriver is a directory: %s", ghostdriver.getAbsolutePath());
			Preconditions.checkState(ghostdriver.canRead(), "The GhostDriver is not a readable file: %s", ghostdriver.getAbsolutePath());
			return ghostdriver;
		} else {
			return null;
		}
	}

	protected static void checkExecutable(File exe) {
		Preconditions.checkState(exe.exists(), "The driver executable does not exist: %s", exe.getAbsolutePath());
		Preconditions.checkState(!exe.isDirectory(), "The driver executable is a directory: %s", exe.getAbsolutePath());
		Preconditions.checkState(exe.canExecute(), "The driver is not executable: %s", exe.getAbsolutePath());
	}

	private static String[] findCLIArgumentsFromCaps(Capabilities desiredCapabilities, String capabilityName) {
		if (desiredCapabilities != null) {
			Object cap = desiredCapabilities.getCapability(capabilityName);
			if (cap != null) {
				if (cap instanceof String[]) {
					return ((String[])cap);
				}

				if (cap instanceof Collection) {
					try {
						Collection<String> capCollection = (Collection<String>)cap;
						return capCollection.toArray(new String[capCollection.size()]);
					} catch (Exception var4) {
						System.err.println(String.format("Unable to set Capability '%s' as it was neither a String[] or a Collection<String>", capabilityName));
					}
				}
			}
		}

		return new String[0];
	}
}
