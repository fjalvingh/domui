package to.etc.dbutil.schema;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 20-5-18.
 */
public class DbDomain {
	private String m_name;

	private ColumnType m_type;

	private int m_precision;

	private int m_scale;

	private boolean m_nullable;

	private int m_sqlType;

	private String m_platformTypeName;

	public DbDomain(String name, ColumnType type, int precision, int scale, boolean nullable, int sqlType, String platformTypeName) {
		m_name = name;
		m_type = type;
		m_precision = precision;
		m_scale = scale;
		m_nullable = nullable;
		m_sqlType = sqlType;
		m_platformTypeName = platformTypeName;
	}

	public String getName() {
		return m_name;
	}

	public ColumnType getType() {
		return m_type;
	}

	public int getPrecision() {
		return m_precision;
	}

	public int getScale() {
		return m_scale;
	}

	public boolean isNullable() {
		return m_nullable;
	}

	public int getSqlType() {
		return m_sqlType;
	}

	public String getPlatformTypeName() {
		return m_platformTypeName;
	}
}
