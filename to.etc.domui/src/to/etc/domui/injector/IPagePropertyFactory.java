package to.etc.domui.injector;

import to.etc.util.*;

import javax.annotation.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
@DefaultNonNull
public interface IPagePropertyFactory {
	@Nullable
	PropertyInjector	calculateInjector(PropertyInfo propertyInfo);
}
