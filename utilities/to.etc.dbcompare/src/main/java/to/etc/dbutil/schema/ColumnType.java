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

	static private final List<ColumnType> m_typeList = new ArrayList<>();

	static public final ColumnType FLOAT = new ColumnType("float", Types.FLOAT, false, false, "float", "real");

	static public final ColumnType DOUBLE = new ColumnType("double", Types.DOUBLE, false, false, "double", "double precision");

	static public final ColumnType NUMBER = new ColumnType("number", Types.NUMERIC, true, true, "numeric", "number");

	static public final ColumnType VARCHAR = new ColumnType("varchar", Types.VARCHAR, true, false, "varchar2", "varchar", "character varying");

	static public final ColumnType CHAR = new ColumnType("char", Types.CHAR, true, false, "char", "character");

	static public final ColumnType BOOLEAN = new ColumnType("boolean", Types.BOOLEAN, false, false, "bool", "boolean");

	static public final ColumnType CLOB = new ColumnType("text", Types.CLOB, false, false, "clob", "text");

	static public final ColumnType BLOB = new ColumnType("blob", Types.BLOB, false, false, "blob");

	static public final ColumnType TIMESTAMP = new ColumnType("timestamp", Types.TIMESTAMP, false, false, "timestamp");

	static public final ColumnType TIME = new ColumnType("time", Types.TIME, false, false, "time");

	static public final ColumnType DATE = new ColumnType("date", Types.DATE, false, false, "date");

	static public final ColumnType BIGINT = new ColumnType("int64", Types.BIGINT, false, false, "bigint");

	static public final ColumnType INTEGER = new ColumnType("int32", Types.INTEGER, false, false, "integer", "int");

	static public final ColumnType INTEGER2 = new ColumnType("int16", Types.SMALLINT, false, false, "smallint", "int2");

	static public final ColumnType BINARY = new ColumnType("binary", Types.BINARY, false, false, "binary");

	static public final ColumnType XML = new ColumnType("SQLXML", Types.SQLXML, false, false, "xml");

	private int m_sqlType;

	private String m_name;

	private String[] m_platformNames;

	private boolean m_precision;

	private boolean m_scale;

	protected ColumnType(String name, int sqlType, boolean precision, boolean scale, String... platformNames) {
		m_sqlType = sqlType;
		m_name = name;
		m_platformNames = platformNames;
		m_precision = precision;
		m_scale = scale;
		m_typeList.add(this);

		for(String pn : platformNames) {
			m_typeMap.put(pn, this);
		}
	}

	//static public ColumnType findByCode(String code) {
	//	return m_typeMap.get(code);
	//}

	static public Collection<ColumnType> getTypes() {
		return Collections.unmodifiableCollection(m_typeList);
	}

	public String[] getPlatformNames() {
		return m_platformNames;
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
		result = PRIME * result + ((m_platformNames == null) ? 0 : m_platformNames.hashCode());
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
		if(m_platformNames == null) {
			if(other.m_platformNames != null)
				return false;
		} else if(!m_platformNames.equals(other.m_platformNames))
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
