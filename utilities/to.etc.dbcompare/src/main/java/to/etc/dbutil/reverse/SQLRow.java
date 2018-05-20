package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dbutil.schema.DbColumn;

public class SQLRow {
	private SQLRowSet m_rowSet;

	private Object[] m_values;

	SQLRow(@NonNull SQLRowSet rowSet, @NonNull Object[] values) {
		m_rowSet = rowSet;
		m_values = values;
	}

	public Object getValue(int index) {
		return m_values[index];
	}

	public Object getValue(@NonNull DbColumn col) {
		int index = m_rowSet.getColumnList().indexOf(col);
		if(index < 0)
			throw new IllegalArgumentException(col + ": not in result");
		return getValue(index);
	}


}
