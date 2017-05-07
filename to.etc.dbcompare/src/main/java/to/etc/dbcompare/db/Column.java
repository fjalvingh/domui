package to.etc.dbcompare.db;

import java.io.*;

public class Column implements Serializable {
	private Table		m_table;

	private String		m_name;

	private ColumnType	m_type;

	private int			m_precision;

	private int			m_scale;

	private boolean		m_nullable;

	private String		m_comment;

	private int			m_sqlType;

	private String		m_platformTypeName;

	public Column(Table table, String name, ColumnType type, int precision, int scale, boolean nullable) {
		m_table = table;
		m_name = name;
		m_type = type;
		m_precision = precision;
		m_scale = scale;
		m_nullable = nullable;
	}

	public int getPrecision() {
		return m_precision;
	}

	public void setPrecision(int precision) {
		m_precision = precision;
	}

	public int getScale() {
		return m_scale;
	}

	public void setScale(int scale) {
		m_scale = scale;
	}

	public ColumnType getType() {
		return m_type;
	}

	public void setType(ColumnType type) {
		m_type = type;
	}

	public String getName() {
		return m_name;
	}

	public Table getTable() {
		return m_table;
	}

	public String getComment() {
		return m_comment;
	}

	public void setComment(String comment) {
		m_comment = comment;
	}

	public boolean isNullable() {
		return m_nullable;
	}

	public void setNullable(boolean nullable) {
		m_nullable = nullable;
	}

	public String getPlatformTypeName() {
		return m_platformTypeName;
	}

	public void setPlatformTypeName(String platformTypeName) {
		m_platformTypeName = platformTypeName;
	}

	public int getSqlType() {
		return m_sqlType;
	}

	public void setSqlType(int sqlType) {
		m_sqlType = sqlType;
	}


}
