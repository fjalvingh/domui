package to.etc.webapp.qsql;

import java.sql.*;

class ClassInstanceMaker implements IInstanceMaker {
	private JdbcClassMeta m_meta;

	//	private PClassRef m_root;

	private int m_startIndex;

	public ClassInstanceMaker(PClassRef root, int startIndex, JdbcClassMeta cm) {
		m_meta = cm;
		//		m_root = root;
		m_startIndex = startIndex;
	}

	/**
	 * Traverse all properties and obtain their value from the result set.
	 * @see to.etc.webapp.qsql.IInstanceMaker#make(java.sql.ResultSet)
	 */
	public Object make(ResultSet rs) throws Exception {
		//-- 1. Create an object instance.
		Object inst = m_meta.getDataClass().newInstance(); // This will abort when constructor is bad

		//-- 2. Walk the result set and fill in zhe blanks.
		int index = m_startIndex;
		boolean gotsome = false;
		for(JdbcPropertyMeta pm : m_meta.getPropertyList()) {
			if(moveRsToProperty(inst, rs, index, pm))
				gotsome = true;
			index++;
		}

		return gotsome ? inst : null; // No data -> no object (for later join impl)
	}

	private boolean moveRsToProperty(Object inst, ResultSet rs, int index, JdbcPropertyMeta pm) throws Exception {
		ITypeConverter tc = pm.getTypeConverter();
		if(tc == null)
			tc = JdbcMetaManager.getConverter(pm);
		try {
			Object value = tc.convertToInstance(rs, index, pm);
			if(rs.wasNull())
				return false;
			pm.getPi().getSetter().invoke(inst, value);
			return true;
		} catch(Exception x) {
			String lv = "(-)";
			try {
				lv = rs.getString(index);
			} catch(Exception xx) {}

			throw new RuntimeException("Failed to convert column " + pm.getColumnName() + " of class " + pm.getActualClass() + "\nUsing ITypeConverter " + tc + "\nResultSet value: " + lv
				+ "\nException: " + x);
		}
	}
}
