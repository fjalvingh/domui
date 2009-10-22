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
final class UrlEntityInjector extends PropertyInjector {
	final private String m_name;

	final private boolean m_mandatory;

	final private Class< ? > m_entityClass;

	public UrlEntityInjector(final Method propertySetter, final String name, final boolean mandatory, final Class< ? > enityClass) {
		super(propertySetter);
		m_name = name;
		m_mandatory = mandatory;
		m_entityClass = enityClass;
	}

	@Override
	public void inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
		//-- 1. Get the URL parameter's value.
		String pv = papa.getString(m_name);
		if(pv == null) {
			if(m_mandatory)
				throw new IllegalArgumentException("The page " + page.getClass() + " REQUIRES the URL parameter " + m_name);
			return;
		}

		//-- 2. Handle the constant 'NEW'.
		Object value;
		if("NEW".equals(pv)) {
			//-- Construct a new instance
			try {
				value = m_entityClass.newInstance();
			} catch(Exception x) {
				throw new RuntimeException("Cannot create an instance of entity class '" + m_entityClass + "' for URL parameter=" + m_name + " of page=" + page.getClass() + ": " + x, x);
			}
		} else {
			//-- Try to find the PK for this entity
			ClassMetaModel cmm = MetaManager.findClassMeta(m_entityClass); // Locatish
			PropertyMetaModel pmm = cmm.getPrimaryKey(); // Find it's PK;
			if(pmm == null)
				throw new RuntimeException("Cannot find the primary key property for entity class '" + m_entityClass + "' for URL parameter=" + m_name + " of page=" + page.getClass() + ": ");

			//-- Convert the URL's value to the TYPE of the primary key, using URL converters.
			Object pk = ConverterRegistry.convertURLStringToValue(pmm.getActualType(), pv);
			if(pk == null)
				throw new RuntimeException("URL parameter value='" + pv + "' converted to Null primary key value for entity class '" + m_entityClass + "' for URL parameter=" + m_name
					+ " of page=" + page.getClass() + ": ");

			//-- Load the entity using the page's context
			value = QContextManager.getContext(page.getPage()).find(m_entityClass, pk);
		}

		//-- 3. Insert the value.
		try {
			getPropertySetter().invoke(page, value);
		} catch(Exception x) {
			throw new RuntimeException("Cannot SET the entity '" + value + "' for URL parameter=" + m_name + " of page=" + page.getClass() + ": " + x, x);
		}
	}
}