package to.etc.domui.injector;

import java.lang.reflect.*;

import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;

/**
 * This property injector contains the name of an URL parameter plus the property to set from it. At
 * injection time it uses the name to get the string value of the URL parameter. This parameter is
 * then converted using the URL converters registered in the ConverterRegistry to the proper value
 * type of the setter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2008
 */
public class UrlParameterInjector extends PropertyInjector {
	final private String m_name;

	final private boolean m_mandatory;

	public UrlParameterInjector(final Method propertySetter, final String name, final boolean mandatory) {
		super(propertySetter);
		m_name = name;
		m_mandatory = mandatory;
	}

	/**
	 * Effects the actual injection of an URL parameter to a value.
	 * @see to.etc.domui.state.PageMaker.PropertyInjector#inject(to.etc.domui.server.RequestContextImpl, to.etc.domui.state.PageParameters)
	 */
	@Override
	public void inject(final UrlPage page, final RequestContextImpl ctx, final PageParameters papa) throws Exception {
		//-- 1. Get the URL parameter's value.
		String pv = papa.getString(m_name, null);
		if(pv == null) {
			if(m_mandatory)
				throw new IllegalArgumentException("The page " + page.getClass() + " REQUIRES the URL parameter " + m_name);
			return;
		}

		//-- 2. Convert the thing to the appropriate type.
		Class< ? > type = getPropertySetter().getParameterTypes()[0];
		Object value;
		try {
			value = ConverterRegistry.convertURLStringToValue(type, pv);
		} catch(Exception x) {
			throw new RuntimeException("Cannot convert the string '" + pv + "' to type=" + type + ", for URL parameter=" + m_name + " of page=" + page.getClass() + ": " + x, x);
		}

		//-- 3. Insert the value.
		try {
			getPropertySetter().invoke(page, value);
		} catch(Exception x) {
			throw new RuntimeException("Cannot SET the value '" + value + "' converted from the string '" + pv + "' to type=" + type + ", for URL parameter=" + m_name + " of page="
				+ page.getClass() + ": " + x, x);
		}
	}
}