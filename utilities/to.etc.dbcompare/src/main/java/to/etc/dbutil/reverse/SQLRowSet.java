package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbTable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SQLRowSet implements Iterable<SQLRow> {
	private List<DbColumn> m_columnList;

	private DbTable m_table;

	private List<SQLRow> m_rowSet;

	public SQLRowSet(@NonNull List<DbColumn> columnList, @NonNull DbTable table) {
		m_columnList = columnList;
		m_table = table;
	}

	void init(@NonNull List<SQLRow> res) {
		m_rowSet = Collections.unmodifiableList(res);
	}

	@NonNull
	public List<DbColumn> getColumnList() {
		return m_columnList;
	}

	@NonNull
	public DbColumn getColumn(int ix) {
		return getColumnList().get(ix);
	}

	public int size() {
		return getColumnList().size();
	}

	@NonNull
	@Override
	public Iterator<SQLRow> iterator() {
		return m_rowSet.iterator();
	}
}
