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
package to.etc.iocular.def;

import to.etc.util.*;

/**
 * A configuration-time definition for setting a specific property to some specific value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 18, 2009
 */
public class ComponentPropertyDef {
	private final ComponentBuilder m_builder;

	private final String m_propertyName;

	private String m_sourceName;

	private Class< ? > m_sourceClass;

	private boolean m_required;

	private PropertyInfo m_info;

	ComponentPropertyDef(final ComponentBuilder builder, final String propertyName) {
		m_builder = builder;
		m_propertyName = propertyName;
	}

	public String getSourceName() {
		return m_sourceName;
	}

	public void setSourceName(final String sourceName) {
		m_sourceName = sourceName;
	}

	public Class< ? > getSourceClass() {
		return m_sourceClass;
	}

	public void setSourceClass(final Class< ? > sourceClass) {
		m_sourceClass = sourceClass;
	}

	public ComponentBuilder getBuilder() {
		return m_builder;
	}

	public String getPropertyName() {
		return m_propertyName;
	}

	/**
	 * When T this property MUST be settable. It is set for all explicitly defined properties and for
	 * the properties added when the property mode is 'allProperties'. It is unset for automatically
	 * added properties in 'knownProperties' mode.
	 * @return
	 */
	public boolean isRequired() {
		return m_required;
	}

	public void setRequired(final boolean required) {
		m_required = required;
	}

	PropertyInfo getInfo() {
		return m_info;
	}

	void setInfo(final PropertyInfo info) {
		m_info = info;
	}
}
