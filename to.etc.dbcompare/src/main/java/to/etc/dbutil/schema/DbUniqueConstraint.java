package to.etc.dbutil.schema;

import java.io.*;

public class DbUniqueConstraint implements Serializable {
	private String m_name;

	private DbIndex m_backingIndex;

	public DbUniqueConstraint() {}

	public DbUniqueConstraint(String name, DbIndex backingIndex) {
		m_name = name;
		m_backingIndex = backingIndex;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public DbIndex getBackingIndex() {
		return m_backingIndex;
	}

	public void setBackingIndex(DbIndex backingIndex) {
		m_backingIndex = backingIndex;
	}
}
