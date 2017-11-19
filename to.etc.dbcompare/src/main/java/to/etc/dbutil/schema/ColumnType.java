package to.etc.dbutil.schema;

import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Generalized column type.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class ColumnType implements Serializable {
	static private Map<String, ColumnType> m_typeMap = new HashMap<String, ColumnType>();

	static public final ColumnType FLOAT = new ColumnType("float", "fl", Types.FLOAT, false, false);

	static public final ColumnType DOUBLE = new ColumnType("double", "dbl", Types.DOUBLE, false, false);

	static public final ColumnType NUMBER = new ColumnType("number", "n", Types.NUMERIC, true, true);

	static public final ColumnType VARCHAR = new ColumnType("varchar", "v", Types.VARCHAR, true, false);

	static public final ColumnType CHAR = new ColumnType("char", "c", Types.CHAR, true, false);

	static public final ColumnType BOOLEAN = new ColumnType("boolean", "bl", Types.BOOLEAN, false, false);

	static public final ColumnType CLOB = new ColumnType("text", "txt", Types.CLOB, false, false);

	static public final ColumnType BLOB = new ColumnType("blob", "blob", Types.BLOB, false, false);

	static public final ColumnType TIMESTAMP = new ColumnType("timestamp", "ts", Types.TIMESTAMP, false, false);

	static public final ColumnType TIME = new ColumnType("time", "time", Types.TIME, false, false);

	static public final ColumnType DATE = new ColumnType("d", "date", Types.DATE, false, false);

	static public final ColumnType BIGINT = new ColumnType("bi", "bigint", Types.BIGINT, false, false);

	static public final ColumnType INTEGER = new ColumnType("int", "integer", Types.INTEGER, false, false);

	static public final ColumnType BINARY = new ColumnType("binary", "binary", Types.BINARY, false, false);

	private int m_sqlType;

	private String m_name;

	private String m_code;

	private boolean m_precision;

	private boolean m_scale;

	protected ColumnType(String name, String code, int sqlType, boolean precision, boolean scale) {
		m_sqlType = sqlType;
		m_name = name;
		m_code = code;
		m_precision = precision;
		m_scale = scale;
		if(null != m_typeMap.put(code, this))
			throw new IllegalStateException("Dup code in type map: " + code);
	}

	static public ColumnType findByCode(String code) {
		return m_typeMap.get(code);
	}

	static public Collection<ColumnType> getTypes() {
		return m_typeMap.values();
	}

	public String getCode() {
		return m_code;
	}

	public String getName() {
		return m_name;
	}

	public boolean isPrecision() {
		return m_precision;
	}

	public boolean isScale() {
		return m_scale;
	}

	public int getSqlType() {
		return m_sqlType;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((m_code == null) ? 0 : m_code.hashCode());
		result = PRIME * result + ((m_name == null) ? 0 : m_name.hashCode());
		result = PRIME * result + (m_precision ? 1231 : 1237);
		result = PRIME * result + (m_scale ? 1231 : 1237);
		result = PRIME * result + m_sqlType;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		final ColumnType other = (ColumnType) obj;
		if(m_code == null) {
			if(other.m_code != null)
				return false;
		} else if(!m_code.equals(other.m_code))
			return false;
		if(m_name == null) {
			if(other.m_name != null)
				return false;
		} else if(!m_name.equals(other.m_name))
			return false;
		if(m_precision != other.m_precision)
			return false;
		if(m_scale != other.m_scale)
			return false;
		return m_sqlType == other.m_sqlType;
	}

}
