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
package to.etc.webapp.qsql;

import java.sql.*;

import javax.annotation.*;

/**
 * Holds a value and a location (result set) to set a value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
class ValSetter implements IQValueSetter {
	private int m_index;

	private Object m_value;

	private IJdbcType m_converter;

	private JdbcPropertyMeta m_property;

	public ValSetter(int index, Object value, IJdbcType converter, JdbcPropertyMeta pm) {
		m_index = index;
		m_value = value;
		m_converter = converter;
		m_property = pm;
	}

	public JdbcPropertyMeta getProperty() {
		return m_property;
	}
	public int getIndex() {
		return m_index;
	}

	public Object getValue() {
		return m_value;
	}

//	public IJdbcType getConverter() {
//		return m_converter;
//	}

	/**
	 * @see to.etc.webapp.qsql.IQValueSetter#assign(java.sql.PreparedStatement)
	 */
	@Override
	public void assign(@Nonnull PreparedStatement ps) throws Exception {
		m_converter.assignParameter(ps, getIndex(), getProperty(), getValue());
	}
}
