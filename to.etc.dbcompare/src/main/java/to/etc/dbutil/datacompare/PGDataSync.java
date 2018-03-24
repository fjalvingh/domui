package to.etc.dbutil.datacompare;

import to.etc.dbpool.BetterSQLException;
import to.etc.dbutil.schema.Database;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbTable;
import to.etc.util.FileTool;
import to.etc.util.StringTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * QD Sync between Posgresql databases.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 22, 2012
 */
public class PGDataSync {
	private String m_srcPool;

	private String m_destPool;

	private String m_srcSchema;

	private String m_destSchema;

	private long m_srcRecordCount;

	private long m_recordsDone;

	private int m_tablesDone;

	private int m_tablesTotal;

	private DbTable m_currentTable;

	private List<Constraint> m_constraintList;

	private Map<DbColumn, PreparedStatement> m_lobReaderMap = new HashMap<DbColumn, PreparedStatement>();

	private Map<String, PreparedStatement> m_updateStmtMap = new HashMap<String, PreparedStatement>();

	private Set<String> m_ignoreTableSet = new HashSet<String>();

	private Set<String> m_ignoreColumnSet = new HashSet<String>();

	private Set<String> m_onlyTableSet = new HashSet<String>();

	private long m_lobsz;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new PGDataSync().run(args);
		} catch(BetterSQLException sx) {
			sx.printStackTrace();
			SQLException ax = (SQLException) sx.getCause();

			for(;;) {
				SQLException nx = ax.getNextException();
				if(nx == null || nx == ax)
					break;
				nx.printStackTrace();
				ax = nx;
			}
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private void usage() {
		System.out.println("Usage: DataSync [options] srcpool srcschema destpool destschema");
		System.exit(10);
	}

	private Database initDb(String poolid, String schema) throws Exception {
		//-- 1. Try to load an earlier schema,
		File sf = new File(poolid + "-" + schema + ".ser");
		return Database.loadSchema(poolid, schema, sf);
	}

	private void run(String[] args) throws Exception {
		decodeArgs(args);

		//-- Allocate connections to compare,
		Database src = initDb(m_srcPool, m_srcSchema);
		Database dest = initDb(m_destPool, m_destSchema);

		//-- Make sure the schema's are equal.
		EqualSchemaComparator dp = new EqualSchemaComparator(src.getSchema(), dest.getSchema());
		dp.run();
		String del = dp.getChanges();
		if(del.length() != 0) {
			System.err.println("The database schema's are not equal:\n");
			System.err.println(del);
			//			System.exit(10);
		}

		//-- 1. Get a total source db record count.
		m_srcRecordCount = 0;
		for(DbTable st : src.getSchema().getTables()) {
			m_srcRecordCount += st.getRecordCount(src);
		}

		System.out.println("Database schema's equal- start comparison of " + m_srcRecordCount + " records");
		List<DbTable> orderedList = calculateBestOrder(src);
		for(DbTable t : orderedList)
			System.out.println("load " + t.getName());

		registerMd5Function(src);
		registerMd5Function(dest);

		loadConstraints(dest);
		try {
			deleteConstraints(dest);
			updateSequences(src, dest);

			m_tablesDone = 0;
			m_tablesTotal = orderedList.size();

			for(DbTable t : orderedList) {
				syncTable(src, dest, t);
			}
		} finally {
			createConstraints(dest);
		}
	}


	private void updateSequences(Database src, Database dest) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			//-- Get all sequence names.
			List<String> res = new ArrayList<String>();
			ps = src.dbc().prepareStatement("select relname from pg_class where relkind='S'");
			rs = ps.executeQuery();
			while(rs.next())
				res.add(rs.getString(1));
			rs.close();
			ps.close();

			//-- Get/set all sequence values.
			for(String name : res) {
				ps = src.dbc().prepareStatement("select nextval('" + name + "')");
				rs = ps.executeQuery();
				if(!rs.next())
					throw new SQLException("No value for sequence " + name);
				long value = rs.getLong(1);
				rs.close();
				ps.close();

				try {
					ps = dest.dbc().prepareStatement("alter sequence " + name + " restart with " + value);
					ps.executeUpdate();
					dest.dbc().commit();
					System.out.println("Sequence " + name + " set to " + value);
				} catch(Exception x) {
					System.err.println("Failed to update sequence " + name + ": " + x);
				} finally {
					dest.dbc().rollback();
					try {
						ps.close();
					} catch(Exception x) {}
				}
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}

		// TODO Auto-generated method stub

	}

	private void registerMd5Function(Database d) throws Exception {
		String fn = FileTool.readResourceAsString(getClass(), "md5fn.sql", "utf-8");
		PreparedStatement ps = null;
		try {
			ps = d.dbc().prepareStatement(fn);
			ps.executeUpdate();
			d.dbc().commit();
		} catch(Exception x) {
			System.out.println("Create md5 function: " + x);
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			try {
				d.dbc().rollback();
			} catch(Exception x) {}
		}
	}

	private void deleteConstraints(Database dest) throws Exception {
		for(Constraint c : m_constraintList) {
			PreparedStatement ps = null;
			try {
				String sql = "alter table " + c.getSchema() + "." + c.getRelname() + " drop constraint " + c.getConname();
				ps = dest.dbc().prepareStatement(sql);
				ps.executeUpdate();
				//				System.out.println("Drop: " + sql);
			} finally {
				try {
					if(ps != null)
						ps.close();
				} catch(Exception x) {}
			}
		}
	}

	private void createConstraints(Database dest) {
		System.out.println("Re-adding all constraints");
		for(Constraint c : m_constraintList) {
			PreparedStatement ps = null;
			String sql = "alter table " + c.getSchema() + "." + c.getRelname() + " add constraint " + c.getConname() + " " + c.getDef();
			try {
				dest.dbc().rollback();
				ps = dest.dbc().prepareStatement(sql);
				ps.executeUpdate();
			} catch(Exception x) {
				System.err.println("ERROR creating constraint: " + x + "\nSQL: " + sql);
			} finally {
				try {
					if(ps != null)
						ps.close();
				} catch(Exception x) {}
			}
		}
	}

	static private class Constraint {
		private String m_schema;

		private String m_relname;

		private String m_conname;

		private String m_def;

		public Constraint(String schema, String relname, String conname, String def) {
			m_schema = schema;
			m_relname = relname;
			m_conname = conname;
			m_def = def;
		}

		public String getSchema() {
			return m_schema;
		}

		public String getRelname() {
			return m_relname;
		}

		public String getConname() {
			return m_conname;
		}

		public String getDef() {
			return m_def;
		}

	}


	/**
	 * Load all postgresql constraints.
	 * @param dest
	 */
	private void loadConstraints(Database dest) throws Exception {
		m_constraintList = new ArrayList<Constraint>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = dest.dbc().prepareStatement("SELECT n.nspname AS schemaname, c.relname, conname, pg_get_constraintdef(r.oid, false) as condef " //
				+ " FROM  pg_constraint r, pg_class c" //
				+ " LEFT JOIN pg_namespace n ON n.oid = c.relnamespace" //
				+ " WHERE r.contype = 'f'" //
				+ "and r.conrelid=c.oid" //
			); //
			rs = ps.executeQuery();
			while(rs.next()) {
				Constraint c = new Constraint(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
				m_constraintList.add(c);
				//				System.out.println(c.getSchema() + "." + c.getConname() + ", " + c.getDef());
			}
			System.out.println("Loaded " + m_constraintList.size() + " constraints");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}


	}

	/**
	 * Try to calculate the best table order.
	 * @return
	 */
	private List<DbTable> calculateBestOrder(Database src) {
		HashSet<DbTable> doneset = new HashSet();
		List<DbTable> res = new ArrayList<DbTable>();
		for(DbTable t : src.getSchema().getTables()) {
			calculateBestOrder(res, doneset, t);
		}
		return res;
	}

	private void calculateBestOrder(List<DbTable> res, HashSet<DbTable> doneset, DbTable t) {
		if(doneset.contains(t))
			return;
		doneset.add(t);

		if(res.contains(t))
			return;
		for(DbRelation r : t.getChildRelationList()) {			// All relations I am a child in
			calculateBestOrder(res, doneset, r.getParent());	// First load/handle these,
		}
		if(res.contains(t))
			return;
		res.add(t);
	}

	/**
	 * @param args
	 */
	private void decodeArgs(String[] args) {
		int i = 0;
		int anr = 0;
		while(i < args.length) {
			String s = args[i++];
			if(s.startsWith("-")) {
				if("-skip".equals(s)) {
					//-- Skip table/column list.
					if(i >= args.length)
						throw new RuntimeException("Missing table name after -skip");
					String ts = args[i++].toLowerCase();
					int col = ts.indexOf(":");
					if(col == -1) {
						m_ignoreTableSet.add(ts);
					} else {
						m_ignoreColumnSet.add(ts);
					}
				} else if("-only".equals(s)) {
					if(i >= args.length)
						throw new RuntimeException("Missing table name after -skip");
					String ts = args[i++].toLowerCase();
					m_onlyTableSet.add(ts);
				} else if("-puzzler".equals(s)) {
					//					m_ignoreTableSet.add("ab_logline");
					m_ignoreTableSet.add("sys_mail_recipients");
					m_ignoreTableSet.add("sys_mail_messages");
					m_ignoreTableSet.add("qu_file");
					m_ignoreTableSet.add("qu_issue");
					m_ignoreTableSet.add("qu_module");
					m_ignoreTableSet.add("qu_package");
					//					m_ignoreColumnSet.add("cfs_data:cfd_data");
				} else {
					System.err.println("Unknown option " + s);
					System.exit(10);
				}
			} else {
				switch(anr){
					default:
						System.err.println("Too many arguments: " + s);
						System.exit(10);
						break;
					case 0:
						m_srcPool = s;
						break;
					case 1:
						m_srcSchema = s;
						break;
					case 2:
						m_destPool = s;
						break;
					case 3:
						m_destSchema = s;
						break;
				}
				anr++;
			}
		}

		if(m_srcPool == null || m_srcSchema == null || m_destPool == null || m_destSchema == null) {
			usage();
		}
	}

	private void where(String s) {
		StringBuilder sb = new StringBuilder();
		double pct = ((double) m_recordsDone / m_srcRecordCount * 100.0);
		sb.append("Table ").append(m_tablesDone).append("/").append(m_tablesTotal).append(" ").append(String.format("%.1f", pct)).append("% ").append(m_currentTable.getName()).append("> ").append(s)
			.append("\n");
		System.out.print(sb.toString());
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Table sync code.									*/
	/*--------------------------------------------------------------*/
	/*
	 * Core mechanism: we have constraints disabled in target. For every table we do the following:
	 *
	 * 1. Find the PK def.
	 * 2. Create a select for all fields in the table, as follows:
	 *    a. For every normal field just select the value.
	 *    b. For every lob field select the md5sum of the value, using a user-specified function.
	 *    c. Order by PK field.
	 * 3.
	 *
	 */

	static private final String UNCHANGED_LOB = "%%";

	static private class Upd {
		final private Map<DbColumn, Object> m_values;

		private final String m_lobkey;

		public Upd(Map<DbColumn, Object> values, String lobkey) {
			m_values = values;
			m_lobkey = lobkey;
		}

		public Map<DbColumn, Object> getValues() {
			return m_values;
		}

		public String getLobkey() {
			return m_lobkey;
		}
	}

	/**
	 *
	 * @param src
	 * @param dest
	 * @param t
	 */
	private void syncTable(Database src, Database dest, DbTable t) throws Exception {
		m_currentTable = t;
		if(m_ignoreTableSet.contains(t.getName().toLowerCase())) {
			where("Table ignored, skipping.");
			m_tablesDone++;
			return;
		}
		if(m_onlyTableSet.size() > 0) {
			if(!m_onlyTableSet.contains(t.getName().toLowerCase())) {
				where("Table ignored, skipping.");
				m_tablesDone++;
				return;
			}
		}

		where("Starting sync for table: " + t.getRecordCount(src) + " records to-do");
		//		if(!t.getName().equals("sys_mail_messages"))
		//			return;

		String sql = createSelect(t);
		System.out.println("Sel: " + sql);

		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		PreparedStatement ps2 = null;
		ResultSet rs2 = null;

		List<Map<DbColumn, Object>> insertList = new ArrayList<Map<DbColumn, Object>>();
		List<Upd> updateList = new ArrayList<Upd>();
		List<Object[]> deleteList = new ArrayList<Object[]>();
		int unchanged = 0;

		try {
			ps1 = src.dbc().prepareStatement(sql);
			ps2 = dest.dbc().prepareStatement(sql);

			rs1 = ps1.executeQuery();
			rs2 = ps2.executeQuery();

			int recct = 0;
			Map<DbColumn, Object> srcr = new HashMap<DbColumn, Object>();
			Map<DbColumn, Object> dstr = new HashMap<DbColumn, Object>();
			boolean srceof = false;
			boolean dsteof = false;
			Object[] srcpk = null;
			Object[] dstpk = null;
			while(true) {
				if(srcr.size() == 0) {
					//-- Need a src record.
					if(!srceof) {
						srceof = !rs1.next();
						if(!srceof) {
							readRow(srcr, rs1, t);
							srcpk = readPK(srcr, t);
							m_recordsDone++;
							recct++;
							if(m_recordsDone % 1000 == 0)
								where("Syncing " + recct + " of " + t.getName());
						}
					}
				}

				if(dstr.size() == 0) {
					if(!dsteof) {
						dsteof = !rs2.next();
						if(!dsteof) {
							readRow(dstr, rs2, t);
							dstpk = readPK(dstr, t);
						}
					}
				}

				if(dsteof && srceof)
					break;

				int res;
				if(dsteof)
					res = -1; // Have source but no dest -> insert
				else if(srceof)
					res = 1;
				else
					res = compareKeys(srcpk, dstpk);
				//				System.out.println("@ " + res + ": " + Arrays.toString(srcpk) + " / " + Arrays.toString(dstpk));

				if(res < 0) {
					//-- Source pk < dest pk: a dest record is missing -> INSERT
					insertList.add(new HashMap<DbColumn, Object>(srcr));

					//					System.out.println("insert " + Arrays.toString(srcpk));

					//-- Source record done, dest remains
					srcr.clear();
				} else if(res > 0) {
					//-- source pk > dest pk: dest is deleted in src
					//					System.out.println("delete " + Arrays.toString(dstpk));
					deleteList.add(dstpk);

					dstr.clear();
				} else if(res == 0) {
					/*
					 * Same record. Compare all key values, and when a change is found add it to a new map of fields
					 * to change.
					 */
					boolean eq = true;
					String lobkey = "";
					for(DbColumn c : t.getColumnList()) {
						Object sv = srcr.get(c);
						Object dv = dstr.get(c);
						if(!StringTool.isEqual(sv, dv)) {
							//							if(eq) {
							//								System.out.println("HIT: changed col = " + c.getName() + ", o=" + sv + ", n=" + dv + ", pk=" + Arrays.toString(dstpk));
							//							}
							eq = false;
						} else if(c.isLob()) {
							//-- Unchanged LOB value -> set special marker in value set, to prevent this lob from being updated.
							srcr.put(c, UNCHANGED_LOB);
							lobkey += "/" + c.getName();
						}
					}
					if(eq) {
						unchanged++;
					} else {
						updateList.add(new Upd(new HashMap<DbColumn, Object>(srcr), lobkey + "/"));
					}
					//					System.out.println("update " + Arrays.toString(dstpk));
					dstr.clear();
					srcr.clear();
				}
			}
			System.out.println(t.getName() + ": " + insertList.size() + " inserts, " + deleteList.size() + " deletes, " + updateList.size() + " updates, " + unchanged + " unchanged");

			handleDeletes(dest, deleteList, t);
			handleInserts(src, dest, insertList, t);
			handleUpdates(src, dest, updateList, t);

			m_tablesDone++;
		} finally {
			try {
				if(rs1 != null)
					rs1.close();
			} catch(Exception x) {}
			try {
				if(ps1 != null)
					ps1.close();
			} catch(Exception x) {}
			try {
				if(rs2 != null)
					rs2.close();
			} catch(Exception x) {}
			try {
				if(ps2 != null)
					ps2.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Handle all table updates.
	 *
	 * @param src
	 * @param dest
	 * @param updateList
	 * @param t
	 */
	private void handleUpdates(Database src, Database dest, List<Upd> updateList, DbTable t) throws Exception {
		try {
			int updates = 0;

			long ts = System.currentTimeMillis();
			for(Upd u : updateList) {
				handleSingleUpdate(src, dest, t, u);
				updates++;

				long cts = System.currentTimeMillis();
				if(updates % 100 == 0 || (cts - ts) > 60000) {
					ts = cts;
					where("Updating " + updates + " of " + updateList.size());
					dest.dbc().commit();
				}
			}
			dest.dbc().commit();
		} finally {
			clearLobs();
		}
	}

	private void handleSingleUpdate(Database src, Database dest, DbTable t, Upd u) throws Exception {
		PreparedStatement ps = getUpdateStatement(dest, t, u.getLobkey()); // Get this specified update statement

		//-- Assign all values to update.
		int ix = 1;
		Object[] pk = readPK(u.getValues(), t);
		for(DbColumn col : t.getColumnList()) {
			if(isIgnoredLob(u.getLobkey(), col))
				continue;
			String tsl = t.getName() + ":" + col.getName();
			if(m_ignoreColumnSet.contains(tsl.toLowerCase()))
				continue;
			if(isPkField(t, col))
				continue;

			assignDestValue(src, t, col, ps, ix, u.getValues());
			ix++;
		}

		//-- Assign all PK values
		assignPK(ps, pk, ix, t);
		ps.executeUpdate();
	}

	/**
	 * Find or create an update statement that updates this set of fields
	 * @param dest
	 * @param t
	 * @param lobkey
	 * @return
	 * @throws Exception
	 * @throws SQLException
	 */
	private PreparedStatement getUpdateStatement(Database dest, DbTable t, String lobkey) throws Exception {
		PreparedStatement ps = m_updateStmtMap.get(lobkey);
		if(ps == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("update ").append(t.getName()).append(" set ");

			//-- Add all fields that are *not* part of the PK and that are *not* in the lobkey (lobs that do not need an update).
			int ix = 0;
			for(DbColumn col : t.getColumnList()) {
				if(isIgnoredLob(lobkey, col))
					continue;
				String tsl = t.getName() + ":" + col.getName();
				if(m_ignoreColumnSet.contains(tsl.toLowerCase()))
					continue;
				if(isPkField(t, col))
					continue;
				if(ix++ > 0)
					sb.append(',');
				sb.append(col.getName()).append("=?");
			}

			//-- Add the PK where clause
			sb.append(" where ");
			appendPkSelect(sb, t);

			ps = dest.dbc().prepareStatement(sb.toString());
			m_updateStmtMap.put(lobkey, ps);
		}
		return ps;
	}

	private boolean isPkField(DbTable t, DbColumn col) {
		for(DbColumn c : t.getPrimaryKey().getColumnList()) {
			if(c == col)
				return true;
		}
		return false;
	}

	private boolean isIgnoredLob(String lobkey, DbColumn col) {
		return lobkey.contains("/" + col.getName() + "/");
	}

	/**
	 * Handle all inserts.
	 * @param dest
	 * @param insertList
	 * @param t
	 */
	private void handleInserts(Database src, Database dest, List<Map<DbColumn, Object>> insertList, DbTable t) throws Exception {

		PreparedStatement ps = null;
		try {
			StringBuilder sb = new StringBuilder();
			StringBuilder vsb = new StringBuilder();
			sb.append("insert into ").append(t.getName()).append("(");
			int ix = 0;
			for(DbColumn c : t.getColumnList()) {
				String tsl = t.getName() + ":" + c.getName();
				if(m_ignoreColumnSet.contains(tsl.toLowerCase()))
					continue;

				if(ix++ > 0) {
					sb.append(",");
					vsb.append(",");
				}
				sb.append(c.getName());
				vsb.append('?');
				createLobReader(src, c, t);
			}
			sb.append(") values(").append(vsb).append(")");
			ps = dest.dbc().prepareStatement(sb.toString());

			int dels = 0;
			m_lobsz = 0;
			long ts = System.currentTimeMillis();
			for(Map<DbColumn, Object> record : insertList) {
				ix = 1;
				for(DbColumn col : t.getColumnList()) {
					String tsl = t.getName() + ":" + col.getName();
					if(m_ignoreColumnSet.contains(tsl.toLowerCase()))
						continue;

					assignDestValue(src, t, col, ps, ix, record);

					ix++;
				}

				//				ps.executeUpdate();
				ps.addBatch();

				dels++;
				long cts = System.currentTimeMillis();
				if(dels % 100 == 0 || (cts - ts) > 60000) {
					ts = cts;
					ps.executeBatch();
					System.out.println("Inserted: " + dels);
					dest.dbc().commit();
				}
			}
			ps.executeBatch();
			dest.dbc().commit();
			System.out.println("Inserted " + insertList.size() + " records");
		} catch(Exception x) {
			for(DbColumn col : t.getColumnList()) {
				System.out.println(" ..... " + col.getName() + " : " + col.getType().getSqlType() + " / " + col.getType());
			}
			throw x;
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
			clearLobs();
		}
	}

	private void assignDestValue(Database src, DbTable t, DbColumn col, PreparedStatement ps, int ix, Map<DbColumn, Object> record) throws Exception {
		//-- Is this a LOB?
		PreparedStatement lobps = createLobReader(src, col, t);
		if(null == lobps) {
			Object value = record.get(col);
			col.setValue(ps, ix, value);
		} else {
			Object[] pk = readPK(record, t);
			byte[] value = loadLob(lobps, pk, t, col);
			if(value != null) {
				System.out.println("PK=" + Arrays.toString(pk) + ", lobsz=" + value.length);
				m_lobsz += value.length;
			}
			col.setValue(ps, ix, value);
		}
	}

	private byte[] loadLob(PreparedStatement lobps, Object[] pk, DbTable t, DbColumn col) throws Exception {
		assignPK(lobps, pk, 1, t);
		ResultSet rs = null;
		try {
			rs = lobps.executeQuery();
			if(!rs.next())
				return null;

			int st = col.getType().getSqlType();
			switch(st){
				default:
					throw new IllegalStateException("Don't know how to read LOB for type " + st);

				case Types.BINARY:
					return rs.getBytes(1);

				case Types.BLOB:
					Blob b = rs.getBlob(1);
					if(null == b)
						return null;

					//-- Read the blob fully into a byte array
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream is = null;
					try {
						is = b.getBinaryStream();
						if(null == is)
							return null;
						FileTool.copyFile(baos, is);
						baos.close();
						return baos.toByteArray();
					} finally {
						try {
							if(is != null)
								is.close();
						} catch(Exception x) {}
					}
			}


			//
			//			InputStream is = rs.getBinaryStream(1);
			//			if(is == null)
			//				return null;
			//			return FileTool.loadByteBuffers(is);
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {}
		}
	}

	private void clearLobs() {
		for(PreparedStatement ps : m_lobReaderMap.values()) {
			try {
				ps.close();
			} catch(Exception x) {}
		}
		m_lobReaderMap.clear();
		for(PreparedStatement ps : m_updateStmtMap.values()) {
			try {
				ps.close();
			} catch(Exception x) {}
		}
		m_updateStmtMap.clear();
	}

	private PreparedStatement createLobReader(Database src, DbColumn c, DbTable t) throws Exception {
		PreparedStatement ps = m_lobReaderMap.get(c);
		if(null == ps) {
			StringBuilder sb = new StringBuilder();
			switch(c.getType().getSqlType()){
				default:
					return null;

				case Types.BLOB:
				case Types.BINARY:
				case Types.LONGVARBINARY:
					sb.append("select " + c.getName() + " from " + t.getName());
					break;
			}
			sb.append(" where ");
			appendPkSelect(sb, t);

			ps = src.dbc().prepareStatement(sb.toString());
			m_lobReaderMap.put(c, ps);
		}
		return ps;
	}

	/**
	 * Delete all records by PK.
	 * @param dest
	 * @param deleteList
	 * @param t
	 * @throws Exception
	 */
	private void handleDeletes(Database dest, List<Object[]> deleteList, DbTable t) throws Exception {
		PreparedStatement ps = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from ").append(t.getName()).append(" where ");
			int ix;
			appendPkSelect(sb, t);
			ps = dest.dbc().prepareStatement(sb.toString());
			int dels = 0;
			for(Object[] key : deleteList) {
				ix = 1;
				assignPK(ps, key, ix, t);
				ps.addBatch();
				dels++;
				if(dels % 100 == 0) {
					ps.executeBatch();
					System.out.println("Deleted: " + dels);
					dest.dbc().commit();
				}
			}
			ps.executeBatch();
			dest.dbc().commit();
			System.out.println("Deleted " + deleteList.size() + " records");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch(Exception x) {}
		}
	}

	private void assignPK(PreparedStatement ps, Object[] key, int ix, DbTable t) throws Exception {
		int lix = 0;
		for(DbColumn c : t.getPrimaryKey().getColumnList()) {
			c.setValue(ps, ix++, key[lix++]);
		}
	}

	private void appendPkSelect(StringBuilder sb, DbTable t) {
		int ix = 0;
		for(DbColumn c : t.getPrimaryKey().getColumnList()) {
			if(ix++ > 0)
				sb.append(" and ");
			sb.append(c.getName()).append("=?");
		}
	}

	private Object[] readPK(Map<DbColumn, Object> srcr, DbTable t) {
		Object[] res = new Object[t.getPrimaryKey().getColumnList().size()];
		int ix = 0;
		for(DbColumn c : t.getPrimaryKey().getColumnList()) {
			res[ix++] = srcr.get(c);
		}
		return res;
	}

	private int compareKeys(Object[] aa, Object[] ba) {
		if(aa.length != ba.length)
			throw new IllegalStateException();
		for(int i = 0; i < aa.length; i++) {
			Object a = aa[i];
			Object b = ba[i];
			if(a != null || b != null) {
				if(a == null && b != null) {
					return -1;
				} else if(a != null && b == null) {
					return 1;
				} else {
					Comparable ca = (Comparable) a;
					Comparable cb = (Comparable) b;
					int res = ca.compareTo(cb);
					if(res != 0)
						return res;
				}
			}
		}
		return 0;
	}


	private void readRow(Map<DbColumn, Object> row, ResultSet rs, DbTable t) throws Exception {
		row.clear();
		int ix = 1;
		for(DbColumn col : t.getColumnList()) {
			row.put(col, col.getValue(rs, ix));
			ix++;
		}
	}

	private String createSelect(DbTable t) {
		/*
		 * Prepare the select for all columns.
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		int ix = 0;
		for(DbColumn c : t.getColumnList()) {
			if(ix++ > 0)
				sb.append(',');

			switch(c.getType().getSqlType()){
				default:
					sb.append(c.getName());
					break;

				case Types.BINARY:
					sb.append("md5(").append(c.getName()).append(")");
					break;

				case Types.BLOB:
					sb.append("lo_md5(").append(c.getName()).append(")");
					break;
			}
		}
		sb.append(" from ").append(t.getName()).append(" order by ");

		DbPrimaryKey pk = t.getPrimaryKey();
		ix = 0;
		for(DbColumn c : pk.getColumnList()) {
			if(ix++ > 0)
				sb.append(',');
			sb.append(c.getName());
		}
		return sb.toString();
	}

}
