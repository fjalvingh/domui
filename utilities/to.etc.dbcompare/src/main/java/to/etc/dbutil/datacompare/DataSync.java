package to.etc.dbutil.datacompare;

import java.io.*;
import java.util.*;

import to.etc.dbutil.schema.*;

/**
 * Sync database contents between two databases, as optimally as possible.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 22, 2012
 */
public class DataSync {
	private String m_srcPool;

	private String m_destPool;

	private String m_srcSchema;

	private String m_destSchema;

	private long m_srcRecordCount;

	private long m_recordsDone;

	private int m_tablesDone;

	private int m_tablesTotal;

	private DbTable m_currentTable;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new DataSync().run(args);
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
		Database src = initDb(args[0], args[1]);
		Database dest = initDb(args[2], args[3]);

		//-- Make sure the schema's are equal.
		EqualSchemaComparator dp = new EqualSchemaComparator(src.getSchema(), dest.getSchema());
		dp.run();
		String del = dp.getChanges();
		if(del.length() != 0) {
			System.err.println("The database schema's are not equal:\n");
			System.err.println(del);
			System.exit(10);
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

		m_tablesDone = 0;
		m_tablesTotal = orderedList.size();

		for(DbTable t : orderedList) {
			syncTable(src, dest, t);
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
				System.err.println("Unknown option " + s);
				System.exit(10);
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

	/**
	 *
	 * @param src
	 * @param dest
	 * @param t
	 */
	private void syncTable(Database src, Database dest, DbTable t) {
		m_currentTable = t;
		where("Starting sync for table");


	}

}
