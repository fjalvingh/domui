package to.etc.dbutil.schema;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 27-11-18.
 */
public class DbSequence {
	final private DbSchema m_schema;

	final private String m_name;

	private final ColumnType m_columnType;

	private final String m_type;

	private final long m_startValue;

	private final long m_minValue;

	private final long m_maxValue;

	private final long m_increment;

	private final long m_cacheSize;

	private final long m_lastValue;

	private boolean m_quoteName;

	public DbSequence(DbSchema schema, String name, ColumnType columnType, String type, long startValue, long minValue, long maxValue, long increment, long cacheSize, long lastValue) {

		m_schema = schema;
		m_name = name;
		m_columnType = columnType;
		m_type = type;
		m_startValue = startValue;
		m_minValue = minValue;
		m_maxValue = maxValue;
		m_increment = increment;
		m_cacheSize = cacheSize;
		m_lastValue = lastValue;
	}

	public DbSchema getSchema() {
		return m_schema;
	}

	public String getName() {
		return m_name;
	}

	public ColumnType getColumnType() {
		return m_columnType;
	}

	public String getPlatformTypeName() {
		return m_type;
	}

	public long getStartValue() {
		return m_startValue;
	}

	public long getMinValue() {
		return m_minValue;
	}

	public long getMaxValue() {
		return m_maxValue;
	}

	public long getIncrement() {
		return m_increment;
	}

	public long getCacheSize() {
		return m_cacheSize;
	}

	public long getLastValue() {
		return m_lastValue;
	}

	public boolean isQuoteName() {
		return m_quoteName;
	}

	public void setQuoteName(boolean quoteName) {
		m_quoteName = quoteName;
	}
}
