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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.IPageParameters;
import to.etc.util.WrappedException;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Base for injecting something into a property.
 */
@NonNullByDefault
public abstract class PropertyInjector {
	@NonNull
	final private Method m_propertySetter;

	public abstract void inject(@NonNull UrlPage page, @NonNull IPageParameters pp, @NonNull Map<String, Object> attributeMap) throws Exception;

	public PropertyInjector(@NonNull Method propertySetter) {
		m_propertySetter = propertySetter;
	}

	@NonNull
	protected Method getPropertySetter() {
		return m_propertySetter;
	}

	/**
	 * Once the value is determined this injects it, after a check whether the value is allowed
	 * according to the rights checkers registered.
	 */
	protected void setValue(@NonNull Object instance, @Nullable Object value) {
		try {
			getPropertySetter().invoke(instance, value);
		} catch(Exception x) {
			throw new WrappedException("Cannot SET the entity '" + value + "' for property=" + m_propertySetter.getName() + " of page=" + instance.getClass() + ": " + x, x);
		}
	}
}
