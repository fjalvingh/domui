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

import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.util.*;

public class JdbcPropertyMeta {
	private JdbcClassMeta m_classMeta;

	private String m_columnName;

	/** For a compound property this contains all column names, in order. */
	private String[] m_columnNames;

	private PropertyInfo m_pi;

	private Class< ? > m_actualClass;

	private int m_length = -1;

	private int m_scale = -1;

	private boolean m_nullable;

	private boolean m_transient;

	private String m_nullValue;

	private IJdbcType m_typeConverter;

	private boolean m_compound;

	public JdbcPropertyMeta() {}

	public JdbcPropertyMeta(JdbcClassMeta jdbcClassMeta, PropertyInfo pi) {
		m_classMeta = jdbcClassMeta;
		m_pi = pi;
	}

	public JdbcClassMeta getClassMeta() {
		return m_classMeta;
	}

	public void setClassMeta(JdbcClassMeta classMeta) {
		m_classMeta = classMeta;
	}

	public String getColumnName() {
		if(isCompound())
			throw new IllegalStateException("Illegal reference to getColumnName for compound property " + m_classMeta.getDataClass().getName() + "." + getName());
		return m_columnName;
	}

	public String getName() {
		return m_pi.getName();
	}

	public void setColumnName(String columnName) {
		m_columnName = columnName;
		m_columnNames = new String[]{columnName};
	}

	public PropertyInfo getPi() {
		return m_pi;
	}

	public void setPi(PropertyInfo pi) {
		m_pi = pi;
	}

	public Class< ? > getActualClass() {
		return m_actualClass;
	}

	public void setActualClass(Class< ? > actualClass) {
		m_actualClass = actualClass;
	}

	public int getLength() {
		return m_length;
	}

	public void setLength(int length) {
		m_length = length;
	}

	public int getScale() {
		return m_scale;
	}

	public void setScale(int scale) {
		m_scale = scale;
	}

	public boolean isNullable() {
		return m_nullable;
	}

	public void setNullable(boolean nullable) {
		m_nullable = nullable;
	}

	public boolean isTransient() {
		return m_transient;
	}

	public void setTransient(boolean calculated) {
		m_transient = calculated;
	}

	public String getNullValue() {
		return m_nullValue;
	}

	public void setNullValue(String nullValue) {
		m_nullValue = nullValue;
	}

	public IJdbcType getTypeConverter() {
		return m_typeConverter;
	}

	public void setTypeConverter(IJdbcType typeConverter) {
		m_typeConverter = typeConverter;
	}

	public boolean isPrimaryKey() {
		return this == m_classMeta.getPrimaryKey();
	}

	@Override
	public String toString() {
		//		StringBuilder sb = new StringBuilder(32);
		return m_classMeta.getDataClass().getName() + "." + getName() + " (row " + m_classMeta.getTableName() + "." + m_columnName + "): " + getActualClass();
		//		return sb.toString();
	}

	/**
	 * T if this is a COMPOUND JDBC class.
	 * @return
	 */
	public boolean isCompound() {
		return m_compound;
	}

	public void setCompound(boolean compound) {
		m_compound = compound;
	}

	public String[] getColumnNames() {
		return m_columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		m_columnNames = columnNames;
	}

	/**
	 * Return the value of this property on the specified class instance. This throws an exception if inst is null!!!
	 * @param inst
	 * @return
	 */
	@Nullable
	public Object getPropertyValue(@Nonnull Object inst) throws Exception {
		if(inst == null)
			throw new IllegalArgumentException("Null instance not allowed");
		try {
			return m_pi.getGetter().invoke(inst);
		} catch(InvocationTargetException itx) {
			throw WrappedException.unwrap(itx);
		}
	}

	public void setPropertyValue(@Nonnull Object inst, @Nullable Object value) throws Exception {
		if(inst == null)
			throw new IllegalArgumentException("Null instance not allowed");
		try {
			Method setter = m_pi.getSetter();
			if(null == setter)
				throw new IllegalArgumentException("Property " + m_pi + " is read-only");

			setter.invoke(inst, value);
		} catch(InvocationTargetException itx) {
			throw WrappedException.unwrap(itx);
		}
	}
}
