package to.etc.domui.injector;

import to.etc.domui.annotations.*;
import to.etc.domui.util.*;
import to.etc.util.*;

import javax.annotation.*;
import java.lang.reflect.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 12-2-17.
 */
@DefaultNonNull
final public class EntityPropertyInjectorFactory implements IPagePropertyFactory {
	@Nullable @Override public PropertyInjector calculateInjector(PropertyInfo propertyInfo) {
		Method getter = propertyInfo.getGetter();
		if(null == getter)
			return null;

		//-- Check annotation, including super classes.
		UIUrlParameter upp = ClassUtil.findAnnotationIncludingSuperClasses(getter, UIUrlParameter.class);
		if(null == upp)
			return null;

		String name = upp.name() == Constants.NONE ? propertyInfo.getName() : upp.name();
		Class< ? > ent = upp.entity();
		if(ent == Object.class) {
			//-- Use getter's type.
			ent = getter.getReturnType();
		}

		//-- Entity lookup.
		return createEntityInjector(propertyInfo, name, upp.mandatory(), ent);
	}

	protected PropertyInjector createEntityInjector(PropertyInfo pi, String name, boolean mandatory, Class< ? > entityType) {
		return new UrlEntityInjector(pi.getSetter(), name, mandatory, entityType);
	}
}
