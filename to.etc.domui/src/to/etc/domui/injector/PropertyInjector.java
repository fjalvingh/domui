package to.etc.domui.injector;

import java.lang.reflect.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 *
 */
abstract class PropertyInjector {
	final private Method m_propertySetter;

	public PropertyInjector(final Method propertySetter) {
		m_propertySetter = propertySetter;
	}

	protected Method getPropertySetter() {
		return m_propertySetter;
	}

	public abstract void inject(UrlPage page, RequestContextImpl ctx, PageParameters pp) throws Exception;
}