package to.etc.domui.webdriver.core;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.Location;
import org.openqa.selenium.html5.LocationContext;
import org.openqa.selenium.html5.SessionStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.interactions.HasTouchScreen;
import org.openqa.selenium.interactions.TouchScreen;
import org.openqa.selenium.mobile.NetworkConnection;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteTouchScreen;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteLocationContext;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.openqa.selenium.remote.mobile.RemoteNetworkConnection;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-9-17.
 */
public class MyChromeDriver  extends RemoteWebDriver implements LocationContext, WebStorage, HasTouchScreen, NetworkConnection {
	private RemoteLocationContext locationContext;
	private RemoteWebStorage webStorage;
	private TouchScreen touchScreen;
	private RemoteNetworkConnection networkConnection;

	//public MyChromeDriver() {
	//	this(ChromeDriverService.createDefaultService(), new ChromeOptions());
	//}
	//
	//public MyChromeDriver(ChromeDriverService service) {
	//	this(service, new ChromeOptions());
	//}

	public MyChromeDriver(Capabilities capabilities) {
		this(ChromeDriverService.createDefaultService(), capabilities);
	}

	//public MyChromeDriver(ChromeOptions options) {
	//	this(ChromeDriverService.createDefaultService(), options);
	//}

	public MyChromeDriver(ChromeDriverService service, Capabilities capabilities) {
		super(new MyChromeDriverCommandExecutor(service), capabilities);
		this.locationContext = new RemoteLocationContext(this.getExecuteMethod());
		this.webStorage = new RemoteWebStorage(this.getExecuteMethod());
		this.touchScreen = new RemoteTouchScreen(this.getExecuteMethod());
		this.networkConnection = new RemoteNetworkConnection(this.getExecuteMethod());
	}

	@Override
	public void setFileDetector(FileDetector detector) {
		throw new WebDriverException("Setting the file detector only works on remote webdriver instances obtained via RemoteWebDriver");
	}

	@Override
	public LocalStorage getLocalStorage() {
		return this.webStorage.getLocalStorage();
	}

	@Override
	public SessionStorage getSessionStorage() {
		return this.webStorage.getSessionStorage();
	}

	@Override
	public Location location() {
		return this.locationContext.location();
	}

	@Override
	public void setLocation(Location location) {
		this.locationContext.setLocation(location);
	}

	@Override
	public TouchScreen getTouch() {
		return this.touchScreen;
	}

	@Override
	public ConnectionType getNetworkConnection() {
		return this.networkConnection.getNetworkConnection();
	}

	@Override
	public ConnectionType setNetworkConnection(ConnectionType type) {
		return this.networkConnection.setNetworkConnection(type);
	}

	public void launchApp(String id) {
		this.execute("launchApp", ImmutableMap.of("id", id));
	}
}
