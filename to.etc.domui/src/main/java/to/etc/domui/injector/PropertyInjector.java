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
import to.etc.domui.dom.html.AbstractPage;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.IPageParameters;
import to.etc.util.PropertyInfo;
import to.etc.util.WrappedException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * Base for injecting something into a property.
 */
@NonNullByDefault
public abstract class PropertyInjector {
	private final PropertyInfo m_propertyInfo;

	public abstract void inject(@NonNull UrlPage page, @NonNull IPageParameters pp, @NonNull Map<String, Object> attributeMap) throws Exception;

	public PropertyInjector(@NonNull PropertyInfo info) {
		m_propertyInfo = info;
	}

	protected PropertyInfo getPropertyInfo() {
		return m_propertyInfo;
	}

	protected Method getPropertySetter() {
		return Objects.requireNonNull(m_propertyInfo.getSetter());
	}

	/**
	 * Once the value is determined this injects it, after a check whether the value is allowed
	 * according to the rights checkers registered.
	 *
	 * If rights check is refused we return refused details, and we not set the property value.
	 */
	protected void setValue(@NonNull AbstractPage instance, @Nullable Object value) throws Exception {
		try {
			isValueAllowed(instance, value);
			getPropertySetter().invoke(instance, value);
		} catch(AccessCheckException acex) {
			throw acex;
		} catch(Exception x) {
			throw new WrappedException("Cannot SET the entity '" + value + "' for property=" + m_propertyInfo.getName() + " of page=" + instance.getClass() + ": " + x, x);
		}
	}

	private void isValueAllowed(AbstractPage instance, @Nullable Object value) throws Exception {
		for(IInjectedPropertyAccessChecker checker : DomApplication.get().getInjectedPropertyAccessCheckerList()) {
			checker.checkAccessAllowed(m_propertyInfo, instance, value);
		}
	}
}
