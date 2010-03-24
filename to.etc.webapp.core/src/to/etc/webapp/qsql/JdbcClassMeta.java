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

	private JdbcPropertyMeta m_primaryKey;

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
				if(null != colmap.put(pm.getColumnName(), pm))
					throw new IllegalStateException(m_dataClass + ": duplicate column name " + pm.getColumnName());
			}
		}
		m_propertyMap = map;
		m_columnMap = colmap;
		List<JdbcPropertyMeta>	res = new ArrayList<JdbcPropertyMeta>(m_propertyMap.values());
		Collections.sort(res, C_PROP);
		m_propertyList = Collections.unmodifiableList(new ArrayList<JdbcPropertyMeta>(res));
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

	private JdbcPropertyMeta evaluateProperty(PropertyInfo pi) throws Exception {
		if(pi.getGetter() == null) // Writeonly not accepted
			return null;
		if(pi.getSetter() == null) // Readonly not accepted
			return null;
		JdbcPropertyMeta pm = new JdbcPropertyMeta(this, pi);

		QJdbcColumn col = pi.getGetter().getAnnotation(QJdbcColumn.class);
		if(col != null) {
			pm.setColumnName(col.name());
			pm.setNullable(col.nullable());
			pm.setTransient(col.istransient());
			pm.setLength(col.length());
			pm.setActualClass(pi.getGetter().getReturnType());
			pm.setScale(col.scale());
			if(col.columnConverter() != ITypeConverter.class)
				pm.setTypeConverter(col.columnConverter().newInstance());
		}
		if(null != pi.getGetter().getAnnotation(QJdbcId.class)) {
			//-- This is the PK.
			if(m_primaryKey != null)
				throw new IllegalStateException("Duplicate PK: " + pi.getName() + " and " + m_primaryKey.getName());
			m_primaryKey = pm;
		}

		if(pm.getColumnName() == null && !pm.isTransient())
			throw new IllegalStateException(m_dataClass + ": property " + pi.getName() + " has no name for it's JDBC column name");

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

	public void setDataClass(Class< ? > dataClass) {
		m_dataClass = dataClass;
	}

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
}
