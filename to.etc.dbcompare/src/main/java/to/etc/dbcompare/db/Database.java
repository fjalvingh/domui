package to.etc.dbcompare.db;

import java.sql.*;

import to.etc.dbcompare.reverse.*;
import to.etc.dbpool.*;

public class Database {
	private String		m_poolid;

	private Connection	m_dbc;

	private Schema		m_schema;

	public Database(String poolid) {
		m_poolid = poolid;
	}

	public Database(String poolid, Schema s) {
		m_poolid = poolid;
		m_schema = s;
	}

	@Override
	public String toString() {
		return m_poolid + "." + m_schema;
	}

	public Connection dbc() throws Exception {
		if(m_dbc == null)
			m_dbc = PoolManager.getInstance().definePool(m_poolid).getUnpooledDataSource().getConnection();
		return m_dbc;
	}

	/**
	 * Reverse-engineer a db: read all metadata from the db.
	 * @throws Exception
	 */
	public void reverse(String schemaname) throws Exception {
		//-- Get the correct db reverser depending on db type.
		Reverser r = getReverser(schemaname);
		System.out.println("Database " + m_poolid + "." + schemaname + ": reversed using " + r.getIdent());
		m_schema = r.loadSchema();
	}

	public Schema getSchema() {
		return m_schema;
	}

	public Reverser getReverser(String schemaName) throws Exception {
		return ReverserRegistry.findReverser(dbc(), schemaName);
	}


}
