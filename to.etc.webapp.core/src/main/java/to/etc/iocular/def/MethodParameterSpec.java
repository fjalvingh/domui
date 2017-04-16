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

/**
 * Specification of a method parameter by the builder.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 22, 2009
 */
final class MethodParameterSpec {
	/** When set this is defined as "get the parameter by looking up this type in the container" */
	private Class< ? > m_sourceType;

	/** When set this is defined as "lookup the defined component with this name in the container" */
	private String m_sourceName;

	/** When defined this parameter refers to the component that is being defined. */
	private boolean m_self;

	/** For numbered parameters this defines the formal parameter that must be set by this parameter */
	private int m_parameterNumber;

	public Class< ? > getSourceType() {
		return m_sourceType;
	}

	public void setSourceType(final Class< ? > sourceType) {
		m_sourceType = sourceType;
	}

	public String getSourceName() {
		return m_sourceName;
	}

	public void setSourceName(final String sourceName) {
		m_sourceName = sourceName;
	}

	public boolean isSelf() {
		return m_self;
	}

	public void setSelf(final boolean self) {
		m_self = self;
	}

	public int getParameterNumber() {
		return m_parameterNumber;
	}

	public void setParameterNumber(final int parameterNumber) {
		m_parameterNumber = parameterNumber;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if(m_sourceName != null) {
			sb.append("name=");
			sb.append(m_sourceName);
		}
		if(m_sourceType != null) {
			sb.append("type=");
			sb.append(m_sourceType.getName());
		}
		if(m_self) {
			sb.append("source=self");
		}
		sb.append(']');
		return sb.toString();
	}
}
