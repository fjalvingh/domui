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

import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import javax.annotation.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

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
public class DefaultPageInjector implements IPageInjector {
	private Set<String> m_ucs = new HashSet<String>();

	/**
	 * Maps UrlPage classnames to their PageInjectors. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	private Map<String, PageInjector> m_injectorMap = new HashMap<String, PageInjector>();

	public DefaultPageInjector() {
		m_ucs.add(String.class.getName());
		m_ucs.add(Byte.class.getName());
		m_ucs.add(Byte.TYPE.getName());
		m_ucs.add(Character.class.getName());
		m_ucs.add(Character.TYPE.getName());
		m_ucs.add(Short.class.getName());
		m_ucs.add(Short.TYPE.getName());
		m_ucs.add(Integer.class.getName());
		m_ucs.add(Integer.TYPE.getName());
		m_ucs.add(Long.class.getName());
		m_ucs.add(Long.TYPE.getName());
		m_ucs.add(Float.class.getName());
		m_ucs.add(Float.TYPE.getName());
		m_ucs.add(Double.class.getName());
		m_ucs.add(Double.TYPE.getName());
		m_ucs.add(Date.class.getName());
		m_ucs.add(BigDecimal.class.getName());
		m_ucs.add(BigInteger.class.getName());
		m_ucs.add(Boolean.class.getName());
		m_ucs.add(Boolean.TYPE.getName());
		//		UCS.add(Byte.class.toString());
		//		UCS.add(Byte.class.toString());
		//		UCS.add(Byte.class.toString());
		//		UCS.add(Byte.class.toString());
		//		UCS.add(Byte.class.toString());
		//		UCS.add(Byte.class.toString());
		//
	}

	/**
	 * Checks all properties of a page and returns a list of Injectors to use to inject values into
	 * those properties, if needed.
	 *
	 * @param page
	 * @return
	 */
	private List<PropertyInjector> calculateInjectorList(final Class< ? extends UrlPage> page) {
		List<PropertyInfo> pilist = ClassUtil.getProperties(page);
		List<PropertyInjector> ilist = Collections.EMPTY_LIST;
		for(PropertyInfo pi : pilist) {
			PropertyInjector pij = calculateInjector(pi);
			if(pij != null) {
				if(ilist.size() == 0)
					ilist = new ArrayList<PropertyInjector>();
				ilist.add(pij);
			}
		}
		return ilist;
	}


	/**
	 * Tries to find an injector to inject a value for the specified property.
	 *
	 * @param pi
	 * @return
	 */
	@Nullable
	protected PropertyInjector calculateInjector(final PropertyInfo pi) {
		if(pi.getSetter() == null) // Read-only property?
			return null; // Be gone;
		Method m = pi.getGetter();
//		if(m == null)
//			m = pi.getSetter();

		return calculatePropertyInjector(pi, m);
	}

	protected PropertyInjector calculatePropertyInjector(PropertyInfo pi, Method annotatedMethod) {
		//-- Check annotation.
		UIUrlParameter upp = annotatedMethod.getAnnotation(UIUrlParameter.class);

		if(upp != null)
			return createUrlAnnotationConnector(pi, upp);
		return null;
	}

	protected PropertyInjector createUrlAnnotationConnector(final PropertyInfo pi, UIUrlParameter upp) {
		String name = upp.name() == Constants.NONE ? pi.getName() : upp.name();
		Class< ? > ent = upp.entity();
		if(ent == Object.class) {
			//-- Use getter's type.
			ent = pi.getGetter().getReturnType();
		}

		/*
		 * Entity auto-discovery: if entity is specified we're always certain we have an entity. If not,
		 * we check the property type; if that is in a supported conversion class we assume a normal value.
		 */
		if(upp.entity() == Object.class) {
			//-- Can be entity or literal.
			if(upp.name() == Constants.NONE || m_ucs.contains(ent.getName()) || RuntimeConversions.isSimpleType(pi.getActualType())) // If no name is set this is NEVER an entity,
				return createParameterInjector(pi, name, upp.mandatory());
		}

		//-- Entity lookup.
		return createEntityInjector(pi, name, upp.mandatory(), ent);
	}

	protected PropertyInjector createParameterInjector(PropertyInfo pi, String name, boolean mandatory) {
		return new UrlParameterInjector(pi.getSetter(), name, mandatory);
	}

	protected PropertyInjector createEntityInjector(PropertyInfo pi, String name, boolean mandatory, Class< ? > entityType) {
		return new UrlEntityInjector(pi.getSetter(), name, mandatory, entityType);
	}

	/**
	 * Fully recalculates the page injectors to use for the specified page. This explicitly does not
	 * use the injector cache.
	 * @param page
	 * @return
	 */
	protected PageInjector calculatePageInjector(final Class< ? extends UrlPage> page) {
		List<PropertyInjector> pil = calculateInjectorList(page);
		return new PageInjector(page, pil);
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

		pij = calculatePageInjector(page);
		m_injectorMap.put(cn, pij);
		return pij;
	}

	/**
	 * This scans the page for properties that are to be injected. It scans for properties on the Page's UrlPage class
	 * and injects any stuff it finds. This version only handles the @UIUrlParameter annotation.
	 *
	 * @param page
	 * @param papa
	 * @see to.etc.domui.state.IPageInjector#injectPageValues(to.etc.domui.dom.html.UrlPage, to.etc.domui.state.PageParameters)
	 */
	@Override
	public void injectPageValues(final UrlPage page, final IPageParameters papa) throws Exception {
		PageInjector pij = findPageInjector(page.getClass());
		pij.inject(page, papa);
	}
}
