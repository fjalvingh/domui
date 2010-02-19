package to.etc.domui.injector;

import java.lang.reflect.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * Base for injecting something into a property.
 */
public abstract class PropertyInjector {
	final private Method m_propertySetter;

	public PropertyInjector(final Method propertySetter) {
		m_propertySetter = propertySetter;
	}

	protected Method getPropertySetter() {
		return m_propertySetter;
	}

	protected void setValue(Object instance, Object value) {
		try {
			getPropertySetter().invoke(instance, value);
		} catch(Exception x) {
			throw new RuntimeException("Cannot SET the entity '" + value + "' for property=" + m_propertySetter.getName() + " of page=" + instance.getClass() + ": " + x, x);
		}
	}

	public abstract void inject(UrlPage page, RequestContextImpl ctx, PageParameters pp) throws Exception;
}