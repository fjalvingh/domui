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

import to.etc.domui.converter.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;

import javax.annotation.*;
import java.lang.reflect.*;

/**
 * This property injector contains the name of an URL parameter plus the property to set from it. At
 * injection time it uses the name to get the string value of the URL parameter. This parameter is
 * then converted using the URL converters registered in the ConverterRegistry to the proper value
 * type of the setter.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 19, 2008
 */
final public class UrlParameterInjector extends PropertyInjector {
	final private String m_name;

	final private boolean m_mandatory;

	public UrlParameterInjector(final Method propertySetter, final String name, final boolean mandatory) {
		super(propertySetter);
		m_name = name;
		m_mandatory = mandatory;
	}

	/**
	 * Effects the actual injection of an URL parameter to a value.
	 */
	@Override
	public void inject(@Nonnull final UrlPage page, final @Nonnull IPageParameters papa) throws Exception {
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