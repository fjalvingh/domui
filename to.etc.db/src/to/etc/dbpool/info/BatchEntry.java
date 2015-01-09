package to.etc.dbpool.info;

/**
* @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
* Created on 1/9/15.
*/
final public class BatchEntry {
	private final String m_statement;

	private int m_rowCount;

	private int m_execCount;

	public BatchEntry(String statement) {
		m_statement = statement;
	}

	public void add(int count) {
		m_rowCount+= count;
		m_execCount++;
	}

	public String getStatement() {
		return m_statement;
	}

	public int getRowCount() {
		return m_rowCount;
	}

	public int getExecCount() {
		return m_execCount;
	}
}
