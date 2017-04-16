package to.etc.dbcompare.db;

import java.io.*;

public class IndexColumn implements Serializable {
	private Column	m_column;

	private boolean	m_descending;

	public IndexColumn(Column column, boolean descending) {
		m_column = column;
		m_descending = descending;
	}

	public Column getColumn() {
		return m_column;
	}

	public void setColumn(Column column) {
		m_column = column;
	}

	public boolean isDescending() {
		return m_descending;
	}

	public void setDescending(boolean descending) {
		m_descending = descending;
	}
}
