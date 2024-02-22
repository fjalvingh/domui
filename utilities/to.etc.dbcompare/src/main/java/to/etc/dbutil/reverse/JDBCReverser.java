package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbIndex;
import to.etc.dbutil.schema.DbPrimaryKey;
import to.etc.dbutil.schema.DbRelation;
import to.etc.dbutil.schema.DbRelation.RelationUpdateAction;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;
import to.etc.util.FileTool;
import to.etc.util.WrappedException;
import to.etc.webapp.query.QCriteria;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Generic impl of a jdbc-based reverser.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Dec 22, 2006
 */
public class JDBCReverser implements Reverser {
	private DataSource m_ds;

	private final Set<ReverserOption> m_optionSet;

//	private DatabaseMetaData m_dmd;

	private final boolean m_keepConnectionsOpen;

	private Set<DbSchema> m_schemaSet = new HashSet<>();

	public JDBCReverser(DataSource dbc, Set<ReverserOption> optionSet) {
		m_ds = dbc;
		m_keepConnectionsOpen = false;
		m_optionSet = optionSet;
	}

	public JDBCReverser(Connection conn, Set<ReverserOption> optionSet) {
		m_ds = from(conn);
		m_keepConnectionsOpen = true;
		m_optionSet = optionSet;
	}

	public boolean hasOptionRaw(ReverserOption... options) {
		//System.out.println("dbg: " + m_optionSet);
		if(m_optionSet.isEmpty())
			return true;
		for(ReverserOption option : options) {
			if(m_optionSet.contains(option)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasOption(ReverserOption... options) {
		boolean v = hasOptionRaw(options);
		//System.out.println("debug: hasOption " + Arrays.toString(options) + " is " + v);
		return v;
	}

	@Override
	public synchronized void lazy(@NonNull IExec what) {
		Connection dbc = null;
		try {
			dbc = m_ds.getConnection();
			what.exec(dbc);
		} catch(RuntimeException x) {
			throw x;
		} catch(Exception x) {
			throw WrappedException.wrap(x);
		} finally {
			if(!m_keepConnectionsOpen) {
				FileTool.closeAll(dbc);
			}
		}
	}

	@Override
	public String getDefaultSchemaName() throws Exception {
		return "PUBLIC";
	}

	@Override
	public DbSchema loadSchema(@Nullable String name, boolean lazily) throws Exception {
		Connection dbc = m_ds.getConnection();
		try {
			name = translateSchemaName(dbc, name);
			if(name == null)
				throw new IllegalStateException("Schema name not known");

			Set<DbSchema> schemasOnly = m_schemaSet = getSchemasOnly(dbc);

			String javaSucks = name;
			DbSchema schema = schemasOnly.stream().filter(a -> a.getName().equalsIgnoreCase(javaSucks)).findFirst().orElseThrow(() -> new IllegalStateException("Schema name '" + javaSucks + "' not known"));
			Set<DbSchema> schemaSet = m_schemaSet;
			schemaSet.clear();
			schemaSet.add(schema);
			initialize(dbc, schemaSet);
			System.out.println("Reversing tables");
			reverseTables(dbc, schemaSet);

			if(!lazily) {
				if(hasOption(ReverserOption.ReverseSequences)) {
					System.out.println("Reversing sequences");
					reverseSequences(dbc, schemaSet);
				}
				if(hasOption(ReverserOption.ReverseColumns, ReverserOption.ReverseIndexes, ReverserOption.ReverseRelations, ReverserOption.ReverseConstraints)) {
					System.out.println("Reversing columns");
					reverseColumns(dbc, schemaSet);
					//int ncols = 0;
					//for(DbTable t : schema.getTables()) {
					//	ncols += t.getColumnList().size();
					//}
//					msg("Loaded " + ncols + " columns");
				}
				if(hasOption(ReverserOption.ReverseIndexes)) {
					System.out.println("Reversing indices");
					reverseIndexes(dbc, schemaSet);
				}
				if(hasOption(ReverserOption.ReverseColumns)) {
					System.out.println("Reversing primary keys");
					reversePrimaryKeys(dbc, schemaSet);
				}
				if(hasOption(ReverserOption.ReverseRelations)) {
					System.out.println("Reversing relations");
					reverseRelations(dbc, schemaSet);
				}
				if(hasOption(ReverserOption.ReverseViews)) {
					System.out.println("Reversing views");
					reverseViews(dbc, schema);
				}
				if(hasOption(ReverserOption.ReverseProcedures)) {
					System.out.println("Reversing procedures");
					reverseProcedures(dbc, schema);
					System.out.println("Reversing packages");
					reversePackages(dbc, schema);
					System.out.println("Reversing triggers");
					reverseTriggers(dbc, schema);
				}
				if(hasOption(ReverserOption.ReverseConstraints)) {
					System.out.println("Reversing constraints");
					reverseConstraints(dbc, schemaSet);
				}

				afterLoad(dbc, schema);
			}
			return schema;
		} finally {
			if(!m_keepConnectionsOpen) {
				FileTool.closeAll(dbc);
			}
		}
	}

	protected void initialize(Connection dbc, Set<DbSchema> schema) throws Exception {

	}

	@Override
	public Set<DbSchema> getSchemasExcept(boolean lazily, Set<String> except) throws Exception {
		Set<DbSchema> schemaSet = m_schemaSet = getSchemasOnly(lazily);
		if(except != null) {
			Set<String> icSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
			icSet.addAll(except);
			schemaSet.removeIf(schema -> icSet.contains(schema.getName().toLowerCase()));
		}
		Connection dbc = m_ds.getConnection();
		try {
			reverseSchemaSet(dbc, schemaSet, lazily);
			return schemaSet;
		} finally {
			if(!m_keepConnectionsOpen) {
				FileTool.closeAll(dbc);
			}
		}
	}

	/**
	 * Load all schema's, but not their content.
	 */
	protected Set<DbSchema> getSchemasOnly(boolean lazily) throws Exception {
		Connection dbc = m_ds.getConnection();
		try {
			return getSchemasOnly(dbc);
		} finally {
			if(!m_keepConnectionsOpen) {
				FileTool.closeAll(dbc);
			}
		}
	}

	@NonNull
	private Set<DbSchema> getSchemasOnly(Connection dbc) throws Exception {
		Set<DbSchema> schemaSet = new HashSet<>();
		DatabaseMetaData dmd = dbc.getMetaData();
		try(ResultSet rs = dmd.getSchemas()) {
			while(rs.next()) {
				String name = rs.getString("TABLE_SCHEM");
				name = translateSchemaName(dbc, name);
				if(null != name) {
					DbSchema schema = new DbSchema(this, name, null);		// Actual schema
					schemaSet.add(schema);
				}
			}
		}
		return schemaSet;
	}

	@Override
	public Set<DbSchema> getSchemasByName(boolean lazily, @NonNull Collection<String> schemaNames) throws Exception {
		Connection dbc = m_ds.getConnection();
		try {
			Set<DbSchema> schemaSet = m_schemaSet = getSchemasOnly(lazily);		// Load schema's
			if(!schemaNames.isEmpty()) {
				List<String> lcSchemaNames = schemaNames.stream().map(a -> a.toLowerCase()).collect(Collectors.toList());
				schemaSet.removeIf(a -> ! lcSchemaNames.contains(a.getName().toLowerCase()));
			}
			reverseSchemaSet(dbc, schemaSet, lazily);
			return schemaSet;
		} finally {
			if(!m_keepConnectionsOpen) {
				FileTool.closeAll(dbc);
			}
		}
	}

	protected void reverseSchemaSet(Connection dbc, Set<DbSchema> schemaSet, boolean lazily) throws Exception {
		initialize(dbc, schemaSet);
		System.out.println("Reversing tables");
		reverseTables(dbc, schemaSet);

		if(!lazily) {
			if(hasOption(ReverserOption.ReverseSequences)) {
				System.out.println("Reversing sequences");
				reverseSequences(dbc, schemaSet);
			}

			if(hasOption(ReverserOption.ReverseColumns, ReverserOption.ReverseIndexes, ReverserOption.ReverseRelations, ReverserOption.ReverseConstraints)) {
				System.out.println("Reversing columns");
				reverseColumns(dbc, schemaSet);
				int ncols = 0;
				for(DbSchema schema : schemaSet) {
					for(DbTable table : schema.getTables()) {
						ncols += table.getColumnList().size();
					}
				}

				msg("Loaded " + ncols + " columns");
			}
			if(hasOption(ReverserOption.ReverseIndexes)) {
				System.out.println("Reversing indices");
				reverseIndexes(dbc, schemaSet);
			}

			if(hasOption(ReverserOption.ReverseColumns)) {
				System.out.println("Reversing primary keys");
				reversePrimaryKeys(dbc, schemaSet);
			}

			if(hasOption(ReverserOption.ReverseRelations)) {
				System.out.println("Reversing relations");
				reverseRelations(dbc, schemaSet);
			}
//				reverseViews(dbc, schema);
//				reverseProcedures(dbc, schema);
//				reversePackages(dbc, schema);
//				reverseTriggers(dbc, schema);
			if(hasOption(ReverserOption.ReverseConstraints)) {
				System.out.println("Reversing constraints");
				reverseConstraints(dbc, schemaSet);
			}

			if(hasOption(ReverserOption.ReverseColumns, ReverserOption.ReverseSequences)) {
				for(DbSchema schema : schemaSet) {
					for(DbTable table : schema.getTables()) {
						for(DbColumn column : table.getColumnList()) {
							String dflt = column.getDefault();
							if(null != dflt) {
								scanColumnDefault(column, dflt);
							}
						}
					}
				}
			}

//
//				afterLoad(dbc, schema);
		}
	}

	protected void scanColumnDefault(DbColumn column, String dflt) throws Exception {
	}

	protected String translateSchemaName(@NonNull Connection dbc, @Nullable String name) throws Exception {
		if(null == name)
			return "public";
		return name;
	}

	protected void afterLoad(@NonNull Connection dbc, @NonNull DbSchema schema) throws Exception {
		// TODO Auto-generated method stub

	}

	private void reverseSequences(Connection dbc, Set<DbSchema> schemaSet) throws Exception {
		for(DbSchema dbSchema : schemaSet) {
			reverseSequences(dbc, dbSchema);
		}
	}

	public void reverseIndexes(@NonNull Connection dbc, @NonNull Set<DbSchema> schemaSet) throws Exception {
		for(DbSchema schema : schemaSet) {
			for(DbTable t : schema.getTables())
				reverseIndexes(dbc, t);
		}
	}

	public void reversePrimaryKeys(@NonNull Connection dbc, @NonNull Set<DbSchema> schemaSet) throws Exception {
		for(DbSchema schema : schemaSet) {
			for(DbTable t : schema.getTables())
				reversePrimaryKey(dbc, t);
		}
	}

	public void reverseRelations(@NonNull Connection dbc, @NonNull Set<DbSchema> schemaSet) throws Exception {
		for(DbSchema schema : schemaSet) {
			for(DbTable t : schema.getTables())
				reverseRelations(dbc, t);
		}
	}

	public void reverseColumns(@NonNull Connection dbc, @NonNull Set<DbSchema> schemaSet) throws Exception {
		int tables = 0;
		int columns = 0;
		int reportcount = 0;
		for(DbSchema schema : schemaSet) {
			for(DbTable t : schema.getTables()) {
				reverseColumns(dbc, t);
				columns += t.getColumnList().size();
				tables++;
				reportcount += t.getColumnList().size();
				if(reportcount++ >= 1000) {
					System.out.println("Reversing columns: table " + tables + ", column count " + columns);
					reportcount = 0;
				}
			}
		}
	}

	@Override
	public String getIdent() {
		return "Generic JDBC database reverse-engineering plug-in";
	}

	protected void msg(String s) {
		System.out.println("reverser: " + s);
	}

	protected void warning(String s) {
		System.out.println("reverser: WARNING " + s);
	}

	protected boolean isValidTable(ResultSet rs) throws Exception {
		return true;
	}

	protected boolean isSchemaIn(Set<DbSchema> schemaSet, String name) {
		return schemaSet.stream().anyMatch(s -> s.getName().equalsIgnoreCase(name));
	}

	@Nullable
	protected DbSchema findSchema(Set<DbSchema> schemaSet, String name) {
		/*
		 * Special case: if we have only the NONAME schema in the schemaset we ignore the
		 * null name and return that schema. This is needed for databases that do not support schema's.
		 */
		if(name == null && schemaSet.size() == 1) {
			DbSchema schema = schemaSet.iterator().next();
			if(schema.getName().isEmpty())
				return schema;
		}

		Optional<DbSchema> first = schemaSet.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst();
		return first.isPresent() ? first.get() : null;
	}

	@Override
	@Nullable
	public DbSchema findSchema(String name) {
		return findSchema(m_schemaSet, name);
	}

	protected void reverseSequences(Connection dbc, DbSchema schema) throws Exception {
	}

	protected void reverseTables(@NonNull Connection dbc, @NonNull Set<DbSchema> schemaSet) throws Exception {
		ResultSet rs = null;
		try {
			rs = dbc.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
			int count = 0;
			ResultSetMetaData md = rs.getMetaData();
			while(rs.next()) {
				if(isValidTable(rs)) {
					String schemaName = getSchemaFromMetadataSet(rs);
					DbSchema schema = findSchema(schemaSet, schemaName);
					if(null != schema) {
						String name = rs.getString("TABLE_NAME");
						DbTable t = schema.createTable(name);
						count++;
						t.setComments(rs.getString("REMARKS"));
					}
				}
			}
			msg("Loaded " + count + " tables");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
			}
		}
	}

	@Nullable
	protected String getSchemaFromMetadataSet(ResultSet rs) throws Exception {
		return rs.getString("TABLE_SCHEM");
	}

	@Override
	public void reverseColumns(@NonNull Connection dbc, DbTable t) throws Exception {
		List<DbColumn> columnList = new ArrayList<DbColumn>();
		Map<String, DbColumn> columnMap = new HashMap<String, DbColumn>();
		try(ResultSet rs = dbc.getMetaData().getColumns(t.getSchema().getInternalCatalogName(), t.getSchema().getInternalSchemaName(), t.getName(), null)) {
			// All columns in the schema.
			int lastord = -1;
			while(rs.next()) {
				String name = rs.getString("COLUMN_NAME");
				int ord = rs.getInt("ORDINAL_POSITION");
				//                System.out.println(ord+" - " +name);
				if(lastord == -1) {
					lastord = ord;
				} else {
					if(ord <= lastord) {
						throw new IllegalStateException("JDBC driver trouble: getColumns() does not return cols ordered by position: " + lastord + ", " + ord);
					}

					//					if(lastord + 1 != ord)
					//						throw new IllegalStateException("JDBC driver trouble: getColumns() does not return cols ordered by position: " + lastord + ", " + ord);
					lastord = ord;
				}
				DbColumn c = reverseColumn(t, rs, name);
				if(c == null)
					continue;

				if(null != columnMap.put(name, c))
					throw new IllegalStateException("Duplicate column name '" + name + "' in table " + t.getName());
				columnList.add(c);
			}
			//msg("Loaded " + t.getName() + ": " + columnMap.size() + " columns");
			t.initializeColumns(columnList, columnMap);
		}
	}

	protected DbColumn reverseColumn(DbTable t, ResultSet rs, String name) throws Exception {
		int daty = rs.getInt("DATA_TYPE");                            // Types.xxx
		String typename = rs.getString("TYPE_NAME");
		int prec = rs.getInt("COLUMN_SIZE");
		int scale = rs.getInt("DECIMAL_DIGITS");
		int nulla = rs.getInt("NULLABLE");
		if(nulla == DatabaseMetaData.columnNullableUnknown)
			throw new IllegalStateException("JDBC driver does not know nullability of " + t.getName() + "." + name);
		String autoi = rs.getString("IS_AUTOINCREMENT");
		Boolean autoIncrement = autoi == null ? null : "yes".equalsIgnoreCase(autoi) ? Boolean.TRUE : Boolean.FALSE;

		ColumnType ct = decodeColumnType(t.getSchema(), daty, typename);
		DbColumn c;
		if(ct == null) {
			c = reverseColumnUnknownType(rs, t, name, daty, typename, prec, scale, nulla == DatabaseMetaData.columnNullable, autoIncrement);
			if(null == c) {
				return null;
			}
		} else {
			c = createDbColumn(t, name, daty, typename, prec, prec, scale, nulla == DatabaseMetaData.columnNullable, autoIncrement, ct);
		}
		c.setComment(rs.getString("REMARKS"));
		return c;
	}

	@NonNull
	protected DbColumn createDbColumn(DbTable table, String name, int daty, String typename, int dataSize, int prec, int scale, boolean nulla, Boolean autoIncrement, ColumnType ct) {
		DbColumn c;
		c = new DbColumn(table, name, ct, dataSize, prec, scale, nulla, autoIncrement);
		c.setPlatformTypeName(typename);
		c.setSqlType(daty);
		return c;
	}

	protected DbColumn reverseColumnUnknownType(ResultSet rs, DbTable t, String name, int sqlType, String typename, int prec, int scale, boolean nulla, Boolean autoIncrement) {
		log("Unknown type: SQLType " + sqlType + " (" + typename + ") in " + t.getName() + "." + name);
		return createDbColumn(t, name, Integer.MAX_VALUE, typename, prec, prec, scale, nulla, autoIncrement, ColumnType.UNKNOWN);
	}

	static private void dumpRow(ResultSet rs) throws Exception {
		for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			System.out.println(i + " (" + rs.getMetaData().getColumnName(i) + ") = " + rs.getString(i));
		}
	}

	@Override
	public void reverseIndexes(@NonNull Connection dbc, DbTable t) throws Exception {
		if(!hasOption(ReverserOption.ReverseIndexes)) {
			t.setIndexMap(new HashMap<>());
			return;
		}
		ResultSet rs = null;
		Map<String, DbIndex> indexMap = new HashMap<String, DbIndex>();
		try {
			rs = dbc.getMetaData().getIndexInfo(t.getSchema().getInternalCatalogName(), t.getSchema().getInternalSchemaName(), t.getName(), false, true);
			int lastord = -1;
			String lastindex = null;
			DbIndex ix = null;
			while(rs.next()) {
				boolean nonunique = rs.getBoolean("NON_UNIQUE");
				String name = rs.getString("INDEX_NAME");
				int ord = rs.getInt("ORDINAL_POSITION");
				String col = rs.getString("COLUMN_NAME");
				String s = rs.getString("ASC_OR_DESC");
				boolean desc = "D".equalsIgnoreCase(s);
				if(name == null) {            // Microsoft, whom else
					System.out.println("Bad JDBC driver: index name null in database metadata query");
				} else {
					if(col == null) {
						System.out.println("Bad JDBC driver (let me guess: MS): index column name is null in index " + name + " of " + t.getName());
						continue;
					}
					DbColumn c = t.findColumn(col);
					if(null == c) {
						if(col.contains("(") || col.contains(")") || col.contains(" ")) {
							//-- Probably an expression, i.e. a functional index
							msg("index '" + name + "' is probably functional, column spec is '" + col + "'.");
						} else {
							System.out.println("Bad JDBC driver? Index column " + col + " not found in table " + t.getName());
						}
					} else {
						//-- Is a new index being defined?
						if(lastindex == null || !lastindex.equals(name)) {
							lastindex = name;

							ix = new DbIndex(t, name, !nonunique);
							indexMap.put(name, ix);
							lastord = -1;
						} else {
							if(lastord == -1) {
								lastord = ord;
							} else {
								if(lastord + 1 != ord)
									throw new IllegalStateException("Bad JDBC driver: getIndexes() does not return cols ordered by position: " + lastord + ", " + ord);
								lastord = ord;
							}
						}
						if(null != ix)                            // Satisfy ecj's null check
							ix.addColumn(c, desc);
					}
				}
			}
			t.setIndexMap(indexMap);
			//msg("Loaded " + t.getName() + ": " + indexMap.size() + " index(es)");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
			}
		}
	}

	@Override
	public void reversePrimaryKey(@NonNull Connection dbc, DbTable t) throws Exception {
		List<DbColumn> pkl = new ArrayList<>(); // Stupid resultset is ordered by NAME instead of ordinal. Dumbfuckers.
		try(ResultSet rs = dbc.getMetaData().getPrimaryKeys(t.getSchema().getInternalCatalogName(), t.getSchema().getInternalSchemaName(), t.getName())) {
			DbPrimaryKey pk;
			String name = null;
			while(rs.next()) {
				name = rs.getString("PK_NAME");
				int ord = rs.getInt("KEY_SEQ");
				String col = rs.getString("COLUMN_NAME");
				if(col == null) {
					System.out.println("Bad JDBC driver: Null PK column in PK " + name + " of table " + t.getName());
					continue;
				}
				DbColumn c = t.findColumn(col);
				if(c == null) {
					System.out.println("Bad JDBC driver: PK column " + col + " was not found in the column list for table " + t.getName());
				} else {
					while(pkl.size() <= ord)
						pkl.add(null);
					pkl.set(ord, c);
				}
			}
			if(name != null && !pkl.isEmpty()) {
				pk = new DbPrimaryKey(t, name);
				t.setPrimaryKey(pk);
				for(DbColumn c : pkl)
					if(c != null)
						pk.addColumn(c);
			} else {
				t.setPrimaryKey(null);
			}
		}
	}

	protected void reverseRelations(@NonNull Connection dbc, DbTable t) throws Exception {
		reverseRelations(dbc, t, true);
		t.markRelationsInitialized();
	}

	/**
	 * Reverse-engineer all PK -> FK relations.
	 */
	protected void reverseRelations(@NonNull Connection dbc, DbTable t, boolean appendalways) throws Exception {
		ResultSet rs = null;
		try {
			int count = 0;
			String name = null;
			rs = dbc.getMetaData().getExportedKeys(t.getSchema().getInternalCatalogName(), t.getSchema().getInternalSchemaName(), t.getName());
			int lastord = -1;
			DbRelation rel = null;
			while(rs.next()) {
				String fkSchemaName = rs.getString("FKTABLE_SCHEM");
				String pkSchemaName = rs.getString("PKTABLE_SCHEM");

				DbSchema fkSchema = findSchema(fkSchemaName);
				DbSchema pkSchema = findSchema(pkSchemaName);

				String fktname = rs.getString("FKTABLE_NAME");
				String pktname = rs.getString("PKTABLE_NAME");
				String fkcname = rs.getString("FKCOLUMN_NAME");
				String pkcname = rs.getString("PKCOLUMN_NAME");
				String fkname = rs.getString("FK_NAME");

				if(fkname != null && !fkname.isEmpty())
					name = fkname;

				int ord = rs.getInt("KEY_SEQ");
				if(!pktname.equals(t.getName()))
					throw new IllegalStateException("JDBC driver trouble: getExportedKeys returned key from table " + pktname + " while asking for table " + t.getName());

				int updr = rs.getInt("UPDATE_RULE");
				int delr = rs.getInt("DELETE_RULE");

				if(null == pkSchema) {
					log("Missing schema '" + pkSchemaName + "' for table " + t + " in relation " + fkSchemaName + "." + fktname + " >- " + pkSchemaName + "." + pktname);
					continue;
				}
				if(null == fkSchema) {
					log("Missing schema '" + fkSchemaName + "' for table " + t + " in relation " + fkSchemaName + "." + fktname + " >- " + pkSchemaName + "." + pktname);
					continue;
				}
				//-- Find FK table and column and PK column referred to
				DbTable fkt = fkSchema.getTable(fktname);
				DbColumn fkc = fkt.getColumn(fkcname);
				DbColumn pkc = t.getColumn(pkcname);

				//-- If this is a new sequence start a new relation else add to current,
				if(lastord == -1 || ord <= lastord) {
					count++;
					//-- New relation.
					rel = new DbRelation(t, fkt, decodeUpdateInt(updr), decodeUpdateInt(delr));
					lastord = ord;
					if(appendalways || !t.internalGetParentRelationList().contains(rel)) {
						t.internalGetParentRelationList().add(rel);
						fkt.internalGetChildRelationList().add(rel);
					}
				}

				//-- Add the relation fields.
				if(rel == null)
					throw new IllegalStateException("Logic error");
				if(name != null)
					rel.setName(name);
				rel.addPair(pkc, fkc);
			}
			//System.out.println("reverser: got " + count + " relations");
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
			}
		}
	}

	protected RelationUpdateAction decodeUpdateInt(int code) {
		switch(code) {
			default:
				log("Unknown action code for constraint: " + code);
				return RelationUpdateAction.None;

			case DatabaseMetaData.importedKeyNoAction:
			case DatabaseMetaData.importedKeyRestrict:
				return RelationUpdateAction.None;

			case DatabaseMetaData.importedKeyCascade:
				return RelationUpdateAction.Cascade;

			case DatabaseMetaData.importedKeySetNull:
				return RelationUpdateAction.SetNull;

			case DatabaseMetaData.importedKeySetDefault:
				return RelationUpdateAction.SetDefault;
		}
	}

	/**
	 * Lazy method to determine what relations the table is a PARENT in.
	 * @see to.etc.dbutil.reverse.Reverser#reverseParentRelation(java.sql.Connection, to.etc.dbutil.schema.DbTable)
	 */
	@Override
	public void reverseParentRelation(Connection dbc, DbTable dbTable) throws Exception {
		reverseRelations(dbc, dbTable, false);
	}

	@Override
	public void reverseChildRelations(Connection dbc, DbTable t) throws Exception {
		ResultSet rs = null;
		try {
			String name = null;
			rs = dbc.getMetaData().getExportedKeys(t.getSchema().getInternalCatalogName(), t.getSchema().getInternalSchemaName(), t.getName());
			int lastord = -1;
			DbRelation rel = null;
			while(rs.next()) {
				String fktname = rs.getString("FKTABLE_NAME");
				String pktname = rs.getString("PKTABLE_NAME");
				String fkcname = rs.getString("FKCOLUMN_NAME");
				String pkcname = rs.getString("PKCOLUMN_NAME");
				String fkname = rs.getString("FK_NAME");
				if(fkname != null && !fkname.isEmpty())
					name = fkname;
				int ord = rs.getInt("KEY_SEQ");
				if(!pktname.equals(t.getName()))
					throw new IllegalStateException("JDBC driver trouble: getExportedKeys returned key from table " + pktname + " while asking for table " + t.getName());

				int updr = rs.getInt("UPDATE_RULE");
				int delr = rs.getInt("DELETE_RULE");

				//-- Find FK table and column and PK column referred to
				DbTable fkt = t.getSchema().getTable(fktname);
				DbColumn fkc = fkt.getColumn(fkcname);
				DbColumn pkc = t.getColumn(pkcname);

				//-- If this is a new sequence start a new relation else add to current,
				if(lastord == -1 || ord <= lastord) {
					//-- New relation.
					rel = new DbRelation(t, fkt, decodeUpdateInt(updr), decodeUpdateInt(delr));
					lastord = ord;
					if(!t.internalGetParentRelationList().contains(rel)) {
						t.internalGetParentRelationList().add(rel);
						fkt.internalGetChildRelationList().add(rel);
					}
				}

				//-- Add the relation fields.
				if(rel == null)
					throw new IllegalStateException("Logic error");
				if(name != null)
					rel.setName(name);
				rel.addPair(pkc, fkc);
			}
		} finally {
			try {
				if(rs != null)
					rs.close();
			} catch(Exception x) {
			}
		}
	}

	/**
	 * Very simple and naive impl of mapping the genericised type.
	 */
	@Nullable
	public ColumnType decodeColumnType(@Nullable DbSchema schema, int sqltype, @Nullable String typename) {
		ColumnType columnType = decodeColumnTypeByPlatformName(schema, sqltype, typename);
		if(null != columnType)
			return columnType;

		columnType = decodeColumnTypeByExplicitCode(schema, sqltype, typename);
		if(null != columnType)
			return columnType;

		columnType = decodeColumnTypeByCodeTypeCodes(sqltype);
		if(null != columnType)
			return columnType;

		return null;
	}

	@Nullable
	protected ColumnType decodeColumnTypeByCodeTypeCodes(int sqltype) {
		for(ColumnType t : ColumnType.getTypes()) {
			if(t.getSqlType() == sqltype)
				return t;
		}
		return null;
	}

	protected ColumnType decodeColumnTypeByPlatformName(@Nullable DbSchema schema, int sqltype, @Nullable String typename) {
		if(null == typename)
			return null;
		for(ColumnType t : ColumnType.getTypes()) {
			for(String pn : t.getPlatformNames()) {
				if(pn.equalsIgnoreCase(typename)) {
					return t;
				}
			}
		}
		return null;
	}

	protected ColumnType decodeColumnTypeByExplicitCode(DbSchema schema, int sqltype, String typename) {
		switch(sqltype) {
			case Types.BIT:
				return ColumnType.BOOLEAN;
			case Types.SMALLINT:
				return ColumnType.INTEGER2;
			case Types.BOOLEAN:
				return ColumnType.BOOLEAN;

			case Types.NCHAR:
				return ColumnType.NCHAR;
			case Types.DECIMAL:
				return ColumnType.NUMBER;
			case Types.LONGVARCHAR:
				return ColumnType.CLOB;
			case Types.VARBINARY:
			case Types.BINARY:
				return ColumnType.BLOB;
			case Types.FLOAT:
				return ColumnType.FLOAT;
			case Types.LONGVARBINARY:
				return ColumnType.BLOB;
		}
		return null;
	}

	public void reverseViews(@NonNull Connection dbc, @NonNull DbSchema schema) throws Exception {
	}

	public void reverseProcedures(@NonNull Connection dbc, @NonNull DbSchema schema) throws Exception {
	}

	public void reverseTriggers(@NonNull Connection dbc, @NonNull DbSchema schema) throws Exception {
	}

	public void reversePackages(@NonNull Connection dbc, @NonNull DbSchema schema) throws Exception {
	}

	public void reverseConstraints(@NonNull Connection dbc, @NonNull Set<DbSchema> schema) throws Exception {

	}

	@Override
	public boolean typeHasPrecision(@NonNull DbColumn column) {
		switch(column.getSqlType()) {
			case Types.CHAR:
			case Types.DECIMAL:
//			case Types.INTEGER:
			case Types.LONGVARCHAR:
			case Types.NCHAR:
			case Types.NUMERIC:
			case Types.NVARCHAR:
			case Types.VARCHAR:
				return true;

			default:
				return false;
		}
	}

	@Override
	public boolean typeHasScale(@NonNull DbColumn column) {
		switch(column.getSqlType()) {
			case Types.DECIMAL:
			case Types.NUMERIC:
				return true;

			default:
				return false;
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Data retrieval.										*/
	/*--------------------------------------------------------------*/
	@Override
	@NonNull
	public SQLRowSet getData(@NonNull QCriteria<SQLRow> table, int start, int end) throws Exception {
		final SQLBuilder b = new SQLBuilder(this, table, isOracleLimitSyntaxDisaster());
		b.createSelect();
		final SQLRowSet[] res = new SQLRowSet[1];
		lazy(new IExec() {

			@Override
			public void exec(@NonNull Connection dbc) throws Exception {
				res[0] = b.query(dbc);
			}
		});
		return res[0];
	}

	@Override
	public void addSelectColumnAs(StringBuilder sb, String colname, String alias) {
		sb.append("\"" + colname + "\"").append(" as ").append(alias);
	}

	@Override
	public String wrapQueryWithRange(List<DbColumn> collist, String sql, int first, int last) {
		return null;
	}

	public boolean isOracleLimitSyntaxDisaster() {
		return false;
	}

	protected DataSource getDataSource() {
		return m_ds;
	}

	protected void log(String what) {
		System.err.println("reverser: " + what);
	}

	public boolean isKeepConnectionsOpen() {
		return m_keepConnectionsOpen;
	}

	private static DataSource from(Connection conn) {
		return new DataSource() {
			@Override
			public Connection getConnection() throws SQLException {
				return conn;
			}

			@Override
			@Nullable
			public Connection getConnection(@Nullable String username, @Nullable String password) throws SQLException {
				throw new IllegalStateException();
			}

			@Nullable
			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}

			@Override
			public void setLogWriter(@Nullable PrintWriter out) throws SQLException {
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
			}

			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}

			@Override
			@Nullable
			public <T> T unwrap(@Nullable Class<T> iface) throws SQLException {
				return null;
			}

			@Override
			public boolean isWrapperFor(@Nullable Class<?> iface) throws SQLException {
				return false;
			}

			@Override
			@Nullable
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				return null;
			}
		};
	}
}
