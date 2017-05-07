package to.etc.dbutil.schema;

import java.io.*;
import java.sql.*;

import javax.annotation.*;
import javax.sql.*;

import to.etc.dbpool.*;
import to.etc.dbutil.reverse.*;

public class Database {
	private String m_poolid;

	private DataSource m_ds;

	private Connection m_dbc;

	private DbSchema m_schema;

	private Database(String poolid) {
		m_poolid = poolid;
	}

	private Database(String poolid, DbSchema s) {
		m_poolid = poolid;
		m_schema = s;
	}

	/**
	 * tries to load a serialized planset. Returns null if load fails.
	 * @param src
	 * @return
	 */
	@Nonnull
	static private DbSchema loadSchema(File src) throws Exception {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(src));
			DbSchema s = (DbSchema) ois.readObject();
			System.out.println("Schema loaded from " + src);
			return s;
		} finally {
			try {
				if(ois != null)
					ois.close();
			} catch(Exception x) {}
		}
	}

	static private void saveSchema(File dst, DbSchema ps) throws Exception {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(dst));
			oos.writeObject(ps);
		} catch(Exception x) {
			System.out.println("Saving schema failed: " + x);
		} finally {
			try {
				if(oos != null)
					oos.close();
			} catch(Exception x) {}
		}
	}

	static public Database loadSchema(String poolid, String schema, File schemaFile) throws Exception {
		if(null != schemaFile) {
			try {
				DbSchema s = loadSchema(schemaFile);
				return new Database(poolid, s);
			} catch(Exception x) {}
		}

		Database d = new Database(poolid);
		d.reverse(schema);
		if(null != schemaFile)
			saveSchema(schemaFile, d.getSchema());
		return d;
	}

	@Override
	public String toString() {
		return m_poolid + "." + m_schema;
	}

	public DataSource ds() throws Exception {
		if(null == m_ds) {
			m_ds = PoolManager.getInstance().definePool(m_poolid).getUnpooledDataSource();
		}
		return m_ds;
	}

	public Connection dbc() throws Exception {
		if(m_dbc == null) {
			m_dbc = ds().getConnection();
			m_dbc.setAutoCommit(false);
		}
		return m_dbc;
	}

	/**
	 * Reverse-engineer a db: read all metadata from the db.
	 * @throws Exception
	 */
	public void reverse(String schemaname) throws Exception {
		//-- Get the correct db reverser depending on db type.
		Reverser r = getReverser();
		System.out.println("Database " + m_poolid + "." + schemaname + ": reversed using " + r.getIdent());
		m_schema = r.loadSchema(schemaname, false);
	}

	public DbSchema getSchema() {
		return m_schema;
	}

	public Reverser getReverser() throws Exception {
		return ReverserRegistry.findReverser(ds());
	}


}
