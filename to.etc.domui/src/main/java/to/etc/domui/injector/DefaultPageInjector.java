/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.injector;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;
import java.util.*;

/**
 * This is the default DomUI page injector. It is responsible for providing (injecting) values into
 * page setters where required. This default version only accepts the @{@link UIUrlParameter} annotation
 * and uses it to inject either URL parameter values or entities loaded from an URL parameter into
 * the page. This can be extended to add extra methods to inject values into a page, for instance using
 * Spring (NO! NO! Use something good instead of this piece of shit!), Guice, Pico/Nanocontainer or
 * whatever.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 23, 2009
 */
@DefaultNonNull
final public class DefaultPageInjector implements IPageInjector {
	/**
	 * Maps UrlPage class names to their PageInjectors. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	private Map<String, PageInjector> m_injectorMap = new HashMap<String, PageInjector>();

	private final DefaultPageInjectorFactory m_defaultPageInjectorFactory;

	public DefaultPageInjector() {
		m_defaultPageInjectorFactory = new DefaultPageInjectorFactory();
		registerPageInjector(0, m_defaultPageInjectorFactory);
	}

	@Override
	public  void registerFactory(int urgency, IPagePropertyFactory injector) {
		m_defaultPageInjectorFactory.registerFactory(urgency, injector);
	}

	final public PageInjector calculateInjectors(Class<? extends UrlPage> page) {
		Map<String, PropertyInjector> propInjectorMap = new HashMap<>();
		for(IPageInjectorCalculator injector : getPageInjectorList()) {
			injector.calculatePageInjectors(propInjectorMap, page);
		}
		return new PageInjector(page, new ArrayList<>(propInjectorMap.values()));
	}

	@DefaultNonNull
	static final private class InjectorReference {
		final private int m_priority;

		final private IPageInjectorCalculator m_pageInjector;

		public InjectorReference(int priority, IPageInjectorCalculator pageInjector) {
			m_priority = priority;
			m_pageInjector = pageInjector;
		}

		public int getPriority() {
			return m_priority;
		}

		public IPageInjectorCalculator getPageInjector() {
			return m_pageInjector;
		}
	}

	@GuardedBy("this")
	private List<InjectorReference> m_pageInjectorOrderList = Collections.emptyList();

	@GuardedBy("this")
	private List<IPageInjectorCalculator> m_pageInjectorList = Collections.emptyList();

	public synchronized void registerPageInjector(int urgency, IPageInjectorCalculator injector) {
		ArrayList<InjectorReference> list = new ArrayList<>(m_pageInjectorOrderList);
		list.add(new InjectorReference(urgency, injector));
		Collections.sort(list, (a, b) -> b.getPriority() - a.getPriority());
		m_pageInjectorOrderList = list;

		List<IPageInjectorCalculator> res = new ArrayList<>(list.size());
		list.forEach(item -> res.add(item.getPageInjector()));
		m_pageInjectorList = Collections.unmodifiableList(res);
	}

	@Nonnull
	private synchronized List<IPageInjectorCalculator> getPageInjectorList() {
		return m_pageInjectorList;
	}

	/**
	 * Find the page injectors to use for the page. This uses the cache.
	 * @param page
	 * @return
	 */
	private synchronized PageInjector findPageInjector(final Class< ? extends UrlPage> page) {
		String cn = page.getClass().getCanonicalName();
		PageInjector pij = m_injectorMap.get(cn);
		if(pij != null) {
			//-- Hit on name; is the class instance the same? If not this is a reload.
			if((Class< ? >) pij.getPageClass() == page.getClass()) // Idiotic generics. If the class changed we have a reload of the class and need to recalculate.
				return pij;
		}

		pij = calculateInjectors(page);
		m_injectorMap.put(cn, pij);
		return pij;
	}

	/**
	 * This scans the page for properties that are to be injected. It scans for properties on the Page's UrlPage class
	 * and injects any stuff it finds. This version only handles the @UIUrlParameter annotation.
	 */
	@Override
	public void injectPageValues(final UrlPage page, final IPageParameters papa) throws Exception {
		PageInjector pij = findPageInjector(page.getClass());
		pij.inject(page, papa);
	}
}
