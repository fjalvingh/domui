package to.etc.dbcompare;

import to.etc.dbutil.schema.*;

public class ColumnChange {
	private ChangeType m_changeType;

	private int m_fromIndex;

	private int m_toIndex;

	private IndexColumn m_column;

	public ColumnChange(ChangeType changeType, IndexColumn column, int fromIndex, int toIndex) {
		m_changeType = changeType;
		m_column = column;
		m_fromIndex = fromIndex;
		m_toIndex = toIndex;
	}

	public ChangeType getChangeType() {
		return m_changeType;
	}

	public void setChangeType(ChangeType changeType) {
		m_changeType = changeType;
	}

	public int getFromIndex() {
		return m_fromIndex;
	}

	public void setFromIndex(int fromIndex) {
		m_fromIndex = fromIndex;
	}

	public int getToIndex() {
		return m_toIndex;
	}

	public void setToIndex(int toIndex) {
		m_toIndex = toIndex;
	}

	public IndexColumn getColumn() {
		return m_column;
	}

	public void setColumn(IndexColumn column) {
		m_column = column;
	}
}
