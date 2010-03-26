package to.etc.webapp.qsql;

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
}
