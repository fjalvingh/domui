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
package to.etc.domui.hibernate.types;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.annotation.*;

import org.hibernate.*;
import org.hibernate.usertype.*;

/**
 * Java 5 Hibernate enum type. Coded because the XML variant of Hibernate does not
 * know about Java enum's. Good work, guys 8-( - it's 2009 now.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 1, 2009
 */
@SuppressWarnings("unchecked")
// Prevent nonsense bounds errors
public class Enum5Type implements EnhancedUserType, ParameterizedType {
	private Class< ? extends Enum< ? >> m_enumClass;

	private boolean m_ordinal;

	public void setParameterValues(@Nonnull final Properties parameters) {
		String enumClassName = parameters.getProperty("enumClass");
		try {
			m_enumClass = (Class<Enum< ? >>) Class.forName(enumClassName);
		} catch(ClassNotFoundException cnfe) {
			throw new HibernateException("Enum class not found", cnfe);
		}
		String ord = parameters.getProperty("enumerated");
		if(ord != null && (ord.startsWith("o") || ord.startsWith("O")))
			m_ordinal = true;
	}

	public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
		return cached;
	}

	public Object deepCopy(final Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(final Object value) throws HibernateException {
		return (Enum< ? >) value;
	}

	public boolean equals(final Object x, final Object y) throws HibernateException {
		return x == y;
	}

	public int hashCode(final Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException, SQLException {
		if(m_ordinal) {
			int ord = rs.getInt(names[0]);
			if(rs.wasNull())
				return null;
			else
				return m_enumClass.getEnumConstants()[ord];
		} else {
			String name = rs.getString(names[0]);
			return rs.wasNull() ? null : Enum.valueOf((Class) m_enumClass, name);
		}
	}

	public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException, SQLException {
		if(value == null) {
			st.setNull(index, Types.VARCHAR);
		} else {
			//-- Locate ordinal index,
			for(int i = m_enumClass.getEnumConstants().length; --i >= 0;) {
				if(m_enumClass.getEnumConstants()[i] == value) {
					st.setString(index, ((Enum< ? >) value).name());
					return;
				}
			}
			throw new IllegalStateException("Cannot convert enum value " + value + " to a valid label for enum=" + m_enumClass);
		}
	}

	public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
		return original;
	}

	public Class< ? extends Enum< ? >> returnedClass() {
		return m_enumClass;
	}

	static private final int[] T_ORD = {Types.NUMERIC};

	static private final int[] T_NAME = {Types.VARCHAR};

	public int[] sqlTypes() {
		return m_ordinal ? T_ORD : T_NAME;
	}

	@SuppressWarnings("rawtypes")
	public Object fromXMLString(final String xmlValue) {
		return Enum.valueOf((Class) m_enumClass, xmlValue);
	}

	public String objectToSQLString(final Object value) {
		return '\'' + ((Enum< ? >) value).name() + '\'';
	}

	public String toXMLString(final Object value) {
		return ((Enum< ? >) value).name();
	}
}
