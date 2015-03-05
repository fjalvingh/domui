package to.etc.dbpool.info;

/**
* @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
* Created on 1/9/15.
*/
final public class BatchEntry {
	private final String m_statement;

	private int m_rowCount;

	private int m_execCount;

	private boolean m_noRowCount;

	public BatchEntry(String statement) {
		m_statement = statement;
	}

	public synchronized void add(int count) {
		m_rowCount+= count;
		m_execCount++;
	}

	public synchronized String getStatement() {
		return m_statement;
	}

	public synchronized int getRowCount() {
		return m_rowCount;
	}

	public synchronized int getExecCount() {
		return m_execCount;
	}

	public synchronized void setNoRowCount() {
		m_noRowCount = true;
	}

	public boolean isNoRowCount() {
		return m_noRowCount;
	}
}
