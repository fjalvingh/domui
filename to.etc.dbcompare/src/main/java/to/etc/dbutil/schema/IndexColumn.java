package to.etc.dbutil.schema;

import java.io.*;

public class IndexColumn implements Serializable {
	private DbColumn m_column;

	private boolean m_descending;

	public IndexColumn(DbColumn column, boolean descending) {
		m_column = column;
		m_descending = descending;
	}

	public DbColumn getColumn() {
		return m_column;
	}

	public void setColumn(DbColumn column) {
		m_column = column;
	}

	public boolean isDescending() {
		return m_descending;
	}

	public void setDescending(boolean descending) {
		m_descending = descending;
	}
}
