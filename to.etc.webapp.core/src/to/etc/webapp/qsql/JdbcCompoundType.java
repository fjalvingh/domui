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
import java.sql.*;

import to.etc.util.*;

class JdbcCompoundType implements IJdbcType, IJdbcTypeFactory {
	private JdbcClassMeta m_compoundMeta;

	public JdbcCompoundType(JdbcClassMeta compoundcm) {
		m_compoundMeta = compoundcm;
	}

	JdbcCompoundType() {}

	@Override
	public int accept(JdbcPropertyMeta pm) {
		if(pm.isCompound())
			return 10;
		return -1;
	}

	/**
	 * Create a specific converter for this meta property.
	 * @see to.etc.webapp.qsql.IJdbcTypeFactory#createType(to.etc.webapp.qsql.JdbcPropertyMeta)
	 */
	@Override
	public IJdbcType createType(JdbcPropertyMeta pm) throws Exception {
		JdbcClassMeta cm = JdbcMetaManager.getMeta(pm.getActualClass());
		if(!cm.isCompound())
			throw new IllegalStateException("Property " + pm + " has complex type " + pm.getActualClass() + ", but it is not marked as a compound type with @QJdbcCompound");
		return new JdbcCompoundType(cm);
	}

	@Override
	public int columnCount() {
		return m_compoundMeta.getColumnCount();
	}

	/**
	 * Assign values to the columns as rendered in a parameter set.
	 * @see to.etc.webapp.qsql.IJdbcType#assignParameter(java.sql.PreparedStatement, int, to.etc.webapp.qsql.JdbcPropertyMeta, java.lang.Object)
	 */
	@Override
	public void assignParameter(PreparedStatement ps, int index, JdbcPropertyMeta srcpm, Object inst) throws Exception {
		int rix = index;
		for(JdbcPropertyMeta pm : m_compoundMeta.getPropertyList()) {
			if(pm.isTransient())
				continue;
			IJdbcType type = pm.getTypeConverter();

			//-- Get the value for this property
			Object pvalue = inst == null ? null : pm.getPropertyValue(inst);
			type.assignParameter(ps, rix, pm, pvalue);
			rix += type.columnCount();
		}
	}

	@Override
	public Object convertToInstance(ResultSet rs, int index, JdbcPropertyMeta pmsource) throws Exception {
		Object inst = m_compoundMeta.getDataClass().newInstance(); // Create empty instance;

		boolean nonnull = false;
		int rix = index;
		for(JdbcPropertyMeta pm : m_compoundMeta.getPropertyList()) {
			if(pm.isTransient())
				continue;
			IJdbcType type = pm.getTypeConverter();
			Object pvalue;
			try {
				pvalue = type.convertToInstance(rs, rix, pm);
			} catch(JdbcConversionException x) {
				throw x;
			} catch(Exception x) {
				throw JdbcConversionException.create(x, rs, pm, rix);
			}

			if(pvalue != null) {
				nonnull = true;
			} else {
				//-- If this is primitive convert if possible.
				if(pm.getActualClass().isPrimitive()) {
					String s = pm.getNullValue();
					if(s == null)
						s = "0";
					pvalue = RuntimeConversions.convertTo(s, pm.getActualClass());
				}
			}
			if(pvalue != null) {
				Method setter = pm.getPi().getSetter();
				if(null == setter)
					throw new IllegalArgumentException("Property " + pm + " is read-only");
				setter.invoke(inst, pvalue);
			}
			rix += pm.getColumnNames().length;
		}
		return nonnull ? inst : null;
	}
}
