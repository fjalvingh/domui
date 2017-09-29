package to.etc.domui.webdriver.core;

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.remote.CommandInfo;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.service.DriverCommandExecutor;
import org.openqa.selenium.remote.service.DriverService;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 3-9-17.
 */
public class MyChromeDriverCommandExecutor extends DriverCommandExecutor {
	private static final ImmutableMap<String, CommandInfo> CHROME_COMMAND_NAME_TO_URL;

	public MyChromeDriverCommandExecutor(DriverService service) {
		super(service, CHROME_COMMAND_NAME_TO_URL);
	}

	static {
		CHROME_COMMAND_NAME_TO_URL = ImmutableMap.of("launchApp", new CommandInfo("/session/:sessionId/chromium/launch_app", HttpMethod.POST)
		, "sendCommandWithResult", new CommandInfo("/session/:sessionId/chromium/send_command_and_get_result", HttpMethod.POST)
		);
	}

}
