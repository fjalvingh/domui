package to.etc.domui.injector;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.dom.html.AbstractPage;
import to.etc.domui.login.AccessCheckResult;
import to.etc.util.PropertyInfo;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public interface ITypedValueAccessChecker<T> {
	AccessCheckResult isAccessAllowed(PropertyInfo info, AbstractPage page, @NonNull T value) throws Exception;
}
