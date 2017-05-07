package to.etc.dbcompare.db;

import java.io.*;

public class UniqueConstraint implements Serializable {
	private String	m_name;

	private Index	m_backingIndex;

	public UniqueConstraint() {
	}

	public UniqueConstraint(String name, Index backingIndex) {
		m_name = name;
		m_backingIndex = backingIndex;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public Index getBackingIndex() {
		return m_backingIndex;
	}

	public void setBackingIndex(Index backingIndex) {
		m_backingIndex = backingIndex;
	}
}
