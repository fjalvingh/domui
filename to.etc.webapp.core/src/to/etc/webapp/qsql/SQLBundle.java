package to.etc.webapp.qsql;

import java.io.*;
import java.lang.ref.*;
import java.util.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * Resource bundle of SQL statements that can be easily executed. These must be encoded as utf-8 class resources.
 * The bundle has the following content format:
 * <pre>
 * -- Statements for MJB bulk operations
 * --!update_elements
 * insert into bae_element_instanties(bot_id, bet_id, bei_aanbrengdatum, bei_einde_levensduur, bei_waarde_dec, bei_omschrijving)
 *  select t.bot_id, t.bet_id, t.bew_datum_ingang, add_months(t.bew_datum_ingang, 12 * nvl(nec_levensduur_cyclus,50)), nvl(t.bew_waarde_dec,t.bew_waarde_num), t2.bet_element
 *     from bae_element_waarden t, bae_elementen t2, npo_elementcodes t3
 *     where t.bet_id = t2.bet_id
 *     and t2.nec_id = t3.nec_id
 *     and t2.bet_mjb = 'Y'
 *     and not exists (select 1 from bae_element_instanties ii where ii.bew_id = t.bew_id)
 * /
 * --!select_item
 * select * from crm_subjects where cst_id=$0
 * </pre>
 * Each statement must be preceded by --![key], the key name there is used to retrieve the statement from the
 * bundle. The statement itself is then a string that is either terminated by the next --![name] or by a / at
 * the 1st position on the line.
 * <p>Statements can be parameterized using $[integer]. The integer index is 0-based and the same number can
 * be repeated throughout the statement. Parameters for the actual query are passed as Object[], and the $integer
 * represents the index into that array. It means that you can easily reuse the same value multiple times in
 * the same statement.</p>
 *
 * <p>SQLBundle's are cached in the system and a single bundle only occurs once in memory. They are weakly linked
 * so they can be garbage-collected when needed.</p>
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 16, 2011
 */
final public class SQLBundle {
	/** The resource name. */
	@Nonnull
	final private String m_name;

	@Nonnull
	final private Map<String, String> m_map;

	@Nonnull
	final private Map<String, BundleStatement> m_stmtMap;

	@Nonnull
	static private final Map<String, Reference<SQLBundle>> m_bundleMap = new HashMap<String, Reference<SQLBundle>>();

	private SQLBundle(@Nonnull String name, @Nonnull Map<String, String> map) {
		m_name = name;
		m_map = map;
		m_stmtMap = new HashMap<String, BundleStatement>(map.size());
	}

	/**
	 * Create a SQL Statement bundle with the default name "sqlbundle.sql" in the package where
	 * the specified class is residing.
	 *
	 * @param base
	 * @return
	 */
	@Nonnull
	static public SQLBundle create(@Nonnull Class< ? > base) {
		return create(base, "sqlbundle.sql");
	}

	/**
	 * Create a SQL statement resource bundle from the specified class resource (name and class).
	 * @param base
	 * @param name
	 * @return
	 */
	@Nonnull
	static synchronized public SQLBundle create(@Nonnull Class< ? > base, @Nonnull String name) {
		//-- Calculate bundle name
		String bn = (base.getPackage().getName() + "." + name);
		Reference<SQLBundle>	ref = m_bundleMap.get(bn);			// Do we have it mapped?
		if(ref != null) {
			SQLBundle b = ref.get();								// Still mapped?
			if(null != b)
				return b;
		}

		//-- Not cached (anymore)- load it
		InputStream is = base.getResourceAsStream(name);
		if(null == is)
			throw new IllegalArgumentException("No class resource for class=" + base + " and name=" + name);
		try {
			String data = FileTool.readStreamAsString(is, "utf-8");
			Map<String, String> map = split(data); // Split into separate statements
			SQLBundle b = new SQLBundle(bn, map);
			m_bundleMap.put(bn, new WeakReference<SQLBundle>(b));
			return b;
		} catch(RuntimeException rx) {
			throw rx;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}

	/**
	 * Split into separate statements. Each statement starts with --! name to name it and ends in /. Comment lines start
	 * with -- and are not copied, nor are empty lines.
	 * @param data
	 * @return
	 */
	@Nonnull
	private static Map<String, String> split(@Nonnull String data) throws IOException {
		LineNumberReader lnr = new LineNumberReader(new StringReader(data));
		String line;

		StringBuilder sb = new StringBuilder();
		String currname = null;

		Map<String, String> res = new HashMap<String, String>();
		while(null != (line = lnr.readLine())) {
			String tl = line.trim();
			if(tl.startsWith("--!")) {
				//-- New statement. End the previous one.
				addStmt(sb, currname, res);

				//-- Start a new name
				currname = tl.substring(3).trim();
				if(!StringTool.isValidDottedName(currname))
					throw new IllegalStateException("Invalid name for SQL statement: '" + currname + "'");
			} else if(tl.startsWith("--")) {
				//-- Ignore comment
			} else if(tl.startsWith("/")) {
				addStmt(sb, currname, res);
				currname = null;
			} else {
				sb.append(line).append('\n');
			}
		}

		//-- If we have residue add it
		addStmt(sb, currname, res);
		return res;
	}

	private static void addStmt(@Nonnull StringBuilder sb, @Nullable String currname, @Nonnull Map<String, String> res) {
		if(null == currname)
			return;
		String sql = sb.toString().trim();
		if(null != res.put(currname, sql))
			throw new IllegalStateException("Duplicate key=" + currname + " in bundle");
		sb.setLength(0);
	}

	/**
	 * Get the specified statement string. Throws exception if unknown.
	 * @param name
	 * @return
	 */
	@Nonnull
	public String getString(@Nonnull String name) {
		String sql = m_map.get(name);
		if(null == sql)
			throw new MissingResourceException("Missing SQL resource '" + name + "' in SQL bundle " + this, m_name, name);
		return sql;
	}

	@Nonnull
	public synchronized BundleStatement getStatement(@Nonnull String key) {
		BundleStatement bs = m_stmtMap.get(key);
		if(null == bs) {
			String sql = getString(key);
			bs = BundleStatement.create(sql);
			m_stmtMap.put(key, bs);
		}
		return bs;
	}
}
