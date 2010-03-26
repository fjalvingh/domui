package to.etc.webapp.qsql;

import java.sql.*;

import to.etc.webapp.query.*;

class ClassInstanceMaker extends JdbcCompoundType implements IInstanceMaker {
	private int m_startIndex;

	public ClassInstanceMaker(PClassRef root, int startIndex, JdbcClassMeta cm) {
		super(cm);
		m_startIndex = startIndex;
	}

	/**
	 * Traverse all properties and obtain their value from the result set.
	 * @see to.etc.webapp.qsql.IInstanceMaker#make(java.sql.ResultSet)
	 */
	public Object make(QDataContext dc, ResultSet rs) throws Exception {
		Object inst = convertToInstance(rs, m_startIndex);
		if(inst instanceof IInitializable) {
			((IInitializable) inst).initializeInstance(dc);
		}
		return inst;
	}
}
