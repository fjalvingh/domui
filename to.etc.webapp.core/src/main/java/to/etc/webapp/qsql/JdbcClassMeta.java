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

import java.util.*;

import to.etc.util.*;

/**
 * Metadata for a JDBC accessible class.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Aug 25, 2009
 */
public class JdbcClassMeta {
	private Class< ? > m_dataClass;

	private String m_tableName;

	private Map<String, JdbcPropertyMeta> m_propertyMap;

	private Map<String, JdbcPropertyMeta> m_columnMap;

	/** Immutable list of properties. */
	private List<JdbcPropertyMeta> m_propertyList;

	private List<String> m_columnNames;

	private JdbcPropertyMeta m_primaryKey;

	/** T if this is a COMPOUND class, meaning it is not a table but a fragment of one. */
	private boolean m_compound;

	public JdbcClassMeta() {}

	public JdbcClassMeta(Class< ? > cm) {
		m_dataClass = cm;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Configuring from annotations.						*/
	/*--------------------------------------------------------------*/
	/**
	 * Locked initialization of this jdbc accessible POJO
	 */
	protected synchronized void initialize() throws Exception {
		if(m_columnMap != null)
			return;
		if(m_dataClass == null)
			throw new IllegalStateException("The POJO class cannot be null");
		QJdbcTable tbl = m_dataClass.getAnnotation(QJdbcTable.class);
		if(tbl != null) {
			m_tableName = tbl.table();
		}
		QJdbcCompound co = m_dataClass.getAnnotation(QJdbcCompound.class);
		if(co != null) {
			m_compound = true;
		}

		//-- Do not allow both table and compound
		if(m_compound && m_tableName != null)
			throw new IllegalStateException(m_dataClass + ": A table cannot also be a compound");

		//-- Get properties from the class
		List<PropertyInfo> pilist = ClassUtil.getProperties(m_dataClass);
		if(pilist.size() == 0)
			throw new IllegalStateException("No properties on data class!?");
		Map<String, JdbcPropertyMeta> map = new HashMap<String, JdbcPropertyMeta>();
		Map<String, JdbcPropertyMeta> colmap = new HashMap<String, JdbcPropertyMeta>();
		for(PropertyInfo pi : pilist) {
			JdbcPropertyMeta pm = evaluateProperty(pi);
			if(pm != null) {
				if(null != map.put(pm.getName(), pm))
					throw new IllegalStateException(m_dataClass + ": duplicate property name " + pm.getName());
			}
		}
		m_propertyMap = map;
		m_columnMap = colmap;
		List<JdbcPropertyMeta>	res = new ArrayList<JdbcPropertyMeta>(m_propertyMap.values());
		Collections.sort(res, C_PROP);
		m_propertyList = Collections.unmodifiableList(new ArrayList<JdbcPropertyMeta>(res));

		//-- Create column names in EXACT SAME order.
		List<String> colnames = new ArrayList<String>();
		for(JdbcPropertyMeta pm: res) {
			if(!pm.isTransient()) {
				for(String cn : pm.getColumnNames()) {
					if(null != colmap.put(cn, pm))
						throw new IllegalStateException(m_dataClass + ": duplicate column name " + cn);
					colnames.add(cn);
				}
			}
		}
		m_columnNames = Collections.unmodifiableList(colnames);
	}

	static private final Comparator<JdbcPropertyMeta> C_PROP = new Comparator<JdbcPropertyMeta>() {
		@Override
		public int compare(JdbcPropertyMeta a, JdbcPropertyMeta b) {
			if(a.isPrimaryKey() != b.isPrimaryKey()) {
				return a.isPrimaryKey() ? -1 : 1;
			}
			return a.getName().compareTo(b.getName());
		}
	};

	/**
	 * Decode all metadata for this property.
	 * @param pi
	 * @return
	 * @throws Exception
	 */
	private JdbcPropertyMeta evaluateProperty(PropertyInfo pi) throws Exception {
		if(pi.getGetter() == null) // Writeonly not accepted
			return null;
		if(pi.getSetter() == null) // Readonly not accepted
			return null;
		JdbcPropertyMeta pm = new JdbcPropertyMeta(this, pi);
		pm.setActualClass(pi.getGetter().getReturnType());

		QJdbcColumn col = pi.getGetter().getAnnotation(QJdbcColumn.class);
		if(col != null) {
			pm.setColumnName(col.name());
			pm.setNullable(col.nullable());
			pm.setTransient(col.istransient());
			pm.setLength(col.length());
			pm.setScale(col.scale());
			if(col.columnConverter() != IJdbcType.class)
				pm.setTypeConverter(col.columnConverter().newInstance());
		}

		//-- If this is a non-simple type AND not transient it must be a compound..
		if(!JdbcMetaManager.isSimpleType(pm.getActualClass()) && !pm.isTransient()) {
			//-- Must be compound. Decode compound *before* this to get column mappings proper.
			Class< ? > clz = pm.getActualClass();
			if(clz.getAnnotation(QJdbcCompound.class) == null)
				throw new IllegalStateException(m_dataClass + ": property " + pi.getName() + " has complex type " + pm.getActualClass()
					+ ", but it is not marked as a compound type with @QJdbcCompound");

			JdbcClassMeta pcm = JdbcMetaManager.getMeta(clz);
			if(! pcm.isCompound())
				throw new IllegalStateException(m_dataClass + ": property " + pi.getName() + " has complex type " + pm.getActualClass()
					+ ", but it is not marked as a compound type with @QJdbcCompound");
			pm.setCompound(true);

			//-- Create the full column list.
			List<String>	cols = new ArrayList<String>();
			for(JdbcPropertyMeta cpm: pcm.getPropertyList()) {
				for(String s: cpm.getColumnNames())
					cols.add(s);
			}
			pm.setColumnNames(cols.toArray(new String[cols.size()]));

		}

		if(null != pi.getGetter().getAnnotation(QJdbcId.class)) {
			//-- This is the PK.
			if(m_primaryKey != null)
				throw new IllegalStateException("Duplicate PK: " + pi.getName() + " and " + m_primaryKey.getName());
			m_primaryKey = pm;
		}

		if(!pm.isTransient() && !pm.isCompound() && pm.getColumnName() == null)
			throw new IllegalStateException(m_dataClass + ": property " + pi.getName() + " has no name for it's JDBC column name");

		if (null == pm.getTypeConverter()) {
			pm.setTypeConverter(JdbcMetaManager.createConverter(pm));
		}
		return pm;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Getters and setters.								*/
	/*--------------------------------------------------------------*/
	/**
	 * The class itself.
	 * @return
	 */
	public Class< ? > getDataClass() {
		return m_dataClass;
	}

//	public void setDataClass(Class< ? > dataClass) {
//		m_dataClass = dataClass;
//	}

	public String getTableName() {
		return m_tableName;
	}

	public void setTableName(String tableName) {
		m_tableName = tableName;
	}

	public List<JdbcPropertyMeta> getPropertyList() {
		return m_propertyList;
	}

	public JdbcPropertyMeta findProperty(String pname) {
		return m_propertyMap.get(pname);
	}

	public JdbcPropertyMeta getPrimaryKey() {
		return m_primaryKey;
	}

	public void setPrimaryKey(JdbcPropertyMeta primaryKey) {
		m_primaryKey = primaryKey;
	}

	public boolean isCompound() {
		return m_compound;
	}

	public int getColumnCount() {
		return m_columnNames.size();
	}

	public List<String> getColumnNames() {
		return m_columnNames;
	}
}
