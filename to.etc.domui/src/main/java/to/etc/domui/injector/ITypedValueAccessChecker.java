package to.etc.domui.injector;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.AbstractPage;
import to.etc.util.PropertyInfo;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-12-18.
 */
public interface ITypedValueAccessChecker<T> {
	boolean isAccessAllowed(PropertyInfo info, AbstractPage page, @Nullable T value) throws Exception;
}
