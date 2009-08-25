package to.etc.webapp.qsql;

import to.etc.util.*;

public class JdbcPropertyMeta {
	private JdbcClassMeta m_classMeta;

	private String m_columnName;

	private PropertyInfo m_pi;

	private Class< ? > m_actualClass;

	private int m_length = -1;

	private int m_scale = -1;

	private boolean m_nullable;

	private String m_nullValue;

	private ITypeConverter m_typeConverter;

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
		return m_columnName;
	}

	public String getName() {
		return m_pi.getName();
	}

	public void setColumnName(String columnName) {
		m_columnName = columnName;
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

	public String getNullValue() {
		return m_nullValue;
	}

	public void setNullValue(String nullValue) {
		m_nullValue = nullValue;
	}

	public ITypeConverter getTypeConverter() {
		return m_typeConverter;
	}

	public void setTypeConverter(ITypeConverter typeConverter) {
		m_typeConverter = typeConverter;
	}

	@Override
	public String toString() {
		//		StringBuilder sb = new StringBuilder(32);
		return m_classMeta.getDataClass().getName() + "." + getName() + " (row " + m_classMeta.getTableName() + "." + getColumnName() + "): " + getActualClass();
		//		return sb.toString();
	}
}
