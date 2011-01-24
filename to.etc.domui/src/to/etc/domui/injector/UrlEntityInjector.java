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

import to.etc.domui.component.meta.*;
import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.webapp.query.*;

/**
 * This property injector takes the named URL parameter as a string. It does a lookup of the entity specified
 * in the MetaData and locates it's ID property. The URL parameter string is then converted to the type of that
 * primary key using the ConverterRegistry's URL converters. Finally it issues a LOOKUP of the entity using that
 * PK. This converter accepts the special value "NEW"; when that is present it constructs a new instance of the
 * entity.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2008
 */
public class UrlEntityInjector extends PropertyInjector {
	final private String m_name;

	final private boolean m_mandatory;

	final private Class< ? > m_entityClass;

	public UrlEntityInjector(final Method propertySetter, final String name, final boolean mandatory, final Class< ? > enityClass) {
		super(propertySetter);
		m_name = name;
		m_mandatory = mandatory;
		m_entityClass = enityClass;
	}

	protected String getParameterName() {
		return m_name;
	}

	protected boolean isMandatory() {
		return m_mandatory;
	}

	protected String getParameterValue(UrlPage page, PageParameters papa) throws Exception {
		//-- 1. Get the URL parameter's value.
		String pv = papa.getString(m_name, null);
		if(pv == null) {
			if(m_mandatory)
				throw new IllegalArgumentException("The page " + page.getClass() + " REQUIRES the URL parameter " + m_name);
			return null;
		}
		return pv;
	}

	/**
	 * Create a new instance.
	 * @return
	 */
	protected Object createNew(final UrlPage page, final RequestContextImpl ctx) {
		try {
			return m_entityClass.newInstance();
		} catch(Exception x) {
			throw new RuntimeException("Cannot create an instance of entity class '" + m_entityClass + "' for URL parameter=" + m_name + " of page=" + page.getClass() + ": " + x, x);
		}
	}

	/**
	 * Returns T if the request is to create a new instance.
	 * @param page
	 * @param ctx
	 * @param papa
	 * @param value
	 * @return
	 */
	protected boolean isNew(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa, String value) throws Exception {
		return "NEW".equals(value);
	}

	protected Object getKeyInstance(QDataContext dc, final UrlPage page, String pv) throws Exception {
		//-- Try to find the PK for this entity
		ClassMetaModel cmm = MetaManager.findClassMeta(m_entityClass); // Locatish
		PropertyMetaModel< ? > pmm = cmm.getPrimaryKey(); // Find it's PK;
		if(pmm == null)
			throw new RuntimeException("Cannot find the primary key property for entity class '" + m_entityClass + "' for URL parameter=" + m_name + " of page=" + page.getClass() + ": ");

		//-- Convert the URL's value to the TYPE of the primary key, using URL converters.
		Object pk = CompoundKeyConverter.INSTANCE.unmarshal(dc, pmm.getActualType(), pv);
		//		Object pk = ConverterRegistry.convertURLStringToValue(pmm.getActualType(), pv);
		if(pk == null)
			throw new RuntimeException("URL parameter value='" + pv + "' converted to Null primary key value for entity class '" + m_entityClass + "' for URL parameter=" + m_name + " of page="
				+ page.getClass() + ": ");
		return pk;
	}

	//	final protected Object loadInstance(final UrlPage page, String pv) throws Exception {
	//		QDataContext dc = QContextManager.getContext(page.getPage());
	//		Object pk = getKeyInstance(dc, page, pv);
	//		return dc.find(m_entityClass, pk);
	//	}

	@Override
	public void inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
		//-- 1. Get the URL parameter's value.
		String pv = getParameterValue(page, papa);
		if(pv == null)
			return;

		//-- 2. Handle the constant 'NEW'.
		Object value;
		if(isNew(page, ctx, papa, pv)) {
			value = createNew(page, ctx);
		} else {
			QDataContext dc = QContextManager.getContext(page.getPage());
			Object pk = getKeyInstance(dc, page, pv);
			value = dc.find(m_entityClass, pk);
			//			value = loadInstance(page, pv);
			if(value == null && m_mandatory)
				throw new QNotFoundException(m_entityClass, pk);
		}
		setValue(page, value);
	}
}
