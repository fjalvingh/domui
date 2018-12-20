package to.etc.domui.injector;

import to.etc.util.PropertyInfo;

/**
 * Checks whether the injected value is allowed on the page.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public interface IInjectedPropertyAccessChecker<T> {
	boolean isAccessAllowed(PropertyInfo info, T value) throws Exception;
}
