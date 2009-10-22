package to.etc.domui.injector;

import java.util.*;

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

final class PageInjector {
	final private List<PropertyInjector> m_propInjectorList;

	final private Class< ? extends UrlPage> m_pageClass;

	public PageInjector(final Class< ? extends UrlPage> pageClass, final List<PropertyInjector> propInjectorList) {
		m_pageClass = pageClass;
		m_propInjectorList = propInjectorList;
	}

	public Class< ? extends UrlPage> getPageClass() {
		return m_pageClass;
	}

	/**
	 * Inject into all page properties.
	 * @param page
	 * @param ctx
	 * @param pp
	 * @throws Exception
	 */
	public void inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters pp) throws Exception {
		for(PropertyInjector pi : m_propInjectorList)
			pi.inject(page, ctx, pp);
	}
}