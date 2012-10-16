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

import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.util.*;

/**
 * Base for injecting something into a property.
 */
public abstract class PropertyInjector {
	final private Method m_propertySetter;

	public PropertyInjector(final Method propertySetter) {
		m_propertySetter = propertySetter;
	}

	protected Method getPropertySetter() {
		return m_propertySetter;
	}

	protected void setValue(Object instance, Object value) {
		try {
			getPropertySetter().invoke(instance, value);
		} catch(Exception x) {
			throw new WrappedException("Cannot SET the entity '" + value + "' for property=" + m_propertySetter.getName() + " of page=" + instance.getClass() + ": " + x, x);
		}
	}

	public abstract void inject(UrlPage page, RequestContextImpl ctx, IPageParameters pp) throws Exception;
}