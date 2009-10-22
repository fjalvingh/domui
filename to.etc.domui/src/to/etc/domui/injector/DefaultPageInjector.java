package to.etc.domui.injector;

import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import to.etc.domui.annotations.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;
import to.etc.util.*;

public class DefaultPageInjector implements IPageInjector {
	private Set<String> m_ucs = new HashSet<String>();

	/**
	 * Maps UrlPage classnames to their PageInjectors. We use names instead of the Class instances
	 * to allow for class reloading.
	 */
	private Map<String, PageInjector> m_injectorMap = new HashMap<String, PageInjector>();

	public DefaultPageInjector() {
		m_ucs.add(String.class.toString());
		m_ucs.add(Byte.class.toString());
		m_ucs.add(Byte.TYPE.getName());
		m_ucs.add(Character.class.toString());
		m_ucs.add(Character.TYPE.getName());
		m_ucs.add(Short.class.toString());
		m_ucs.add(Short.TYPE.getName());
		m_ucs.add(Integer.class.toString());
		m_ucs.add(Integer.TYPE.getName());
		m_ucs.add(Long.class.toString());
		m_ucs.add(Long.TYPE.getName());
		m_ucs.add(Float.class.toString());
		m_ucs.add(Float.TYPE.getName());
		m_ucs.add(Double.class.toString());
		m_ucs.add(Double.TYPE.getName());
		m_ucs.add(Date.class.toString());
		m_ucs.add(BigDecimal.class.toString());
		m_ucs.add(BigInteger.class.toString());
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
	protected PropertyInjector calculateInjector(final PropertyInfo pi) {
		if(pi.getSetter() == null) // Read-only property?
			return null; // Be gone;
		Method m = pi.getGetter();
		if(m == null)
			m = pi.getSetter();

		//-- Check annotation.
		UIUrlParameter upp = m.getAnnotation(UIUrlParameter.class);

		if(upp != null) {
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
				if(upp.name() == Constants.NONE || m_ucs.contains(ent.getName())) // If no name is set this is NEVER an entity,
					return new UrlParameterInjector(pi.getSetter(), name, upp.mandatory());
			}

			//-- Entity lookup.
			return new UrlEntityInjector(pi.getSetter(), name, upp.mandatory(), ent);
		}
		return null;
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
	 * and injects any stuff it finds.
	 *
	 * @param page
	 * @param ctx
	 * @param papa
	 * @throws Exception
	 */
	public void injectPageValues(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
		PageInjector pij = findPageInjector(page.getClass());
		pij.inject(page, ctx, papa);
	}
}
