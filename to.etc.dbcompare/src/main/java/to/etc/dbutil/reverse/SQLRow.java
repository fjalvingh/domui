package to.etc.dbutil.reverse;

import javax.annotation.*;

import to.etc.dbutil.schema.*;

public class SQLRow {
	private SQLRowSet m_rowSet;

	private Object[] m_values;

	SQLRow(@Nonnull SQLRowSet rowSet, @Nonnull Object[] values) {
		m_rowSet = rowSet;
		m_values = values;
	}

	public Object getValue(int index) {
		return m_values[index];
	}

	public Object getValue(@Nonnull DbColumn col) {
		int index = m_rowSet.getColumnList().indexOf(col);
		if(index < 0)
			throw new IllegalArgumentException(col + ": not in result");
		return getValue(index);
	}


}
