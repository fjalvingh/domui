package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import to.etc.dbutil.schema.DbTable;
import to.etc.webapp.query.ICriteriaTableDef;

final public class QDbTable implements ICriteriaTableDef<SQLRow> {
	final private DbTable m_table;

	public QDbTable(@NonNull DbTable table) {
		m_table = table;
	}

	@Override
	@NonNull
	public Class<SQLRow> getDataClass() {
		return SQLRow.class;
	}

	@NonNull
	@Override
	public String toString() {
		return "DbTable[" + m_table.getName() + "]";
	}

	@NonNull
	public DbTable getTable() {
		return m_table;
	}
}
