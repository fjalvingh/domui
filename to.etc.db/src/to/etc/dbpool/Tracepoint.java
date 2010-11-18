package to.etc.dbpool;

/**
 * This contains a stack trace location. It is not yet pruned.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 2, 2010
 */
final public class Tracepoint {
	final private long m_timestamp;

	final private StackTraceElement[] m_elements;

	final private String m_sql;

	public Tracepoint(long ts, StackTraceElement[] elements, String sql) {
		m_elements = elements;
		m_timestamp = ts;
		m_sql = sql;
	}

	public String getSql() {
		return m_sql;
	}

	public StackTraceElement[] getElements() {
		return m_elements;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	/**
	 * Create a tracepoint for the current stack location and timestamp.
	 * @return
	 */
	static Tracepoint create(String sql) {
		try {
			throw new RuntimeException();
		} catch(Exception x) {
			StackTraceElement[] se = x.getStackTrace();
			return new Tracepoint(System.currentTimeMillis(), se, sql);
		}
	}
}
