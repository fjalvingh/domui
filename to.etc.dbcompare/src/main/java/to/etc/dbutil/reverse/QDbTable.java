package to.etc.dbutil.reverse;

import javax.annotation.*;

import to.etc.dbutil.schema.*;
import to.etc.webapp.query.*;

final public class QDbTable implements ICriteriaTableDef<SQLRow> {
	final private DbTable m_table;

	public QDbTable(@Nonnull DbTable table) {
		m_table = table;
	}

	@Override
	@Nonnull
	public Class<SQLRow> getDataClass() {
		return SQLRow.class;
	}

	@Nonnull
	@Override
	public String toString() {
		return "DbTable[" + m_table.getName() + "]";
	}

	@Nonnull
	public DbTable getTable() {
		return m_table;
	}
}
