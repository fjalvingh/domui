package to.etc.domui.webdriver.poproxies;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.webdriver.core.WebDriverConnector;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 09-12-21.
 */
public interface ICpDriverSource {
	@NonNull
	WebDriverConnector wd();
}
