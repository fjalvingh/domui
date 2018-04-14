package to.etc.domui.injector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.util.PropertyInfo;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
@NonNullByDefault
public interface IPagePropertyFactory {
	@Nullable
	PropertyInjector	calculateInjector(PropertyInfo propertyInfo);
}
