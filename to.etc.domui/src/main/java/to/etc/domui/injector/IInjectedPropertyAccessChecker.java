package to.etc.domui.injector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.AbstractPage;
import to.etc.util.PropertyInfo;

/**
 * Checks whether the injected value is allowed on the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
@NonNullByDefault
public interface IInjectedPropertyAccessChecker {
	void checkAccessAllowed(PropertyInfo info, AbstractPage page, @Nullable Object value) throws Exception;
}
