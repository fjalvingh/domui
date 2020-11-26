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

import org.eclipse.jdt.annotation.NonNullByDefault;
import to.etc.domui.dom.html.AbstractPage;
import to.etc.domui.state.IPageParameters;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@NonNullByDefault
final class PageInjectionList {
	final private List<PropertyInjector> m_propInjectorList;

	final private Class<?> m_pageClass;

	public PageInjectionList(Class<?> pageClass, List<PropertyInjector> propInjectorList) {
		m_pageClass = Objects.requireNonNull(pageClass);
		m_propInjectorList = Objects.requireNonNull(propInjectorList);
	}

	public Class<?> getPageClass() {
		return m_pageClass;
	}

	/**
	 * Inject into all page properties.
	 * @param attributeMap A map that can be used to store things while injecting.
	 */
	public void inject(AbstractPage page, IPageParameters pp, Map<String, Object> attributeMap) throws Exception {
		for(PropertyInjector pi : m_propInjectorList)
			pi.inject(page, pp, attributeMap);
	}
}
