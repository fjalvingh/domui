package to.etc.dbutil.reverse;

import java.util.*;

import javax.annotation.*;

import to.etc.dbutil.schema.*;

public class SQLRowSet implements Iterable<SQLRow> {
	private List<DbColumn> m_columnList;

	private DbTable m_table;

	private List<SQLRow> m_rowSet;

	public SQLRowSet(@Nonnull List<DbColumn> columnList, @Nonnull DbTable table) {
		m_columnList = columnList;
		m_table = table;
	}

	void init(@Nonnull List<SQLRow> res) {
		m_rowSet = Collections.unmodifiableList(res);
	}

	@Nonnull
	public List<DbColumn> getColumnList() {
		return m_columnList;
	}

	@Nonnull
	public DbColumn getColumn(int ix) {
		return getColumnList().get(ix);
	}

	public int size() {
		return getColumnList().size();
	}

	@Nonnull
	@Override
	public Iterator<SQLRow> iterator() {
		return m_rowSet.iterator();
	}
}
