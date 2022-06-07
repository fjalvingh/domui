package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbDomain;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbSequence;
import to.etc.dbutil.schema.DbTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostgresReverser extends JDBCReverser {

	public static final String NEXTVAL = "nextval('";

	private Map<String, DbDomain> m_domainMap = new HashMap<>();

	public PostgresReverser(DataSource dbc, Set<ReverserOption> optionSet) {
		super(dbc, optionSet);
	}

	public PostgresReverser(Connection conn, Set<ReverserOption> optionSet) {
		super(conn, optionSet);
	}

	@Override public String getDefaultSchemaName() throws Exception {
		return "public";
	}

	@Override protected void initialize(Connection dbc, Set<DbSchema> schemaSet) throws Exception {
		try(PreparedStatement ps = dbc.prepareStatement("select * from information_schema.domains");
			ResultSet rs = ps.executeQuery()) {
			while(rs.next()) {
				decodeDomain(rs, schemaSet);
			}
		}
	}

	private void decodeDomain(ResultSet rs, Set<DbSchema> schemaSet) throws SQLException {
		String sn = rs.getString("domain_schema");
		DbSchema schema;
		if("public".equals(sn))
			schema = null;
		else {
			schema = findSchema(sn);
			if(null == schema)
				return;
		}

		String dname = rs.getString("domain_name");
		String fullName = sn + "." + dname;
		String altName = null;
		if("public".equalsIgnoreCase(sn)) {
			altName = dname;
		}
		String type1 = rs.getString("data_type");
		String type2 = rs.getString("udt_name");
		int charsz = rs.getInt("character_maximum_length");
		if(rs.wasNull())
			charsz = -1;
		int prec = rs.getInt("numeric_precision");
		if(rs.wasNull())
			prec = -1;
		int scale = rs.getInt("numeric_scale");
		if(rs.wasNull())
			scale = -1;
		if(prec == -1)
			prec = charsz;

		ColumnType columnType = decodeColumnType(schema, Types.OTHER, type1);
		if(null == columnType) {
			columnType = decodeColumnType(schema, Types.OTHER, type2);
			if(null == columnType)
				return;
		}

		DbDomain d = new DbDomain(dname, columnType, prec, scale, true, columnType.getSqlType(), type1);
		m_domainMap.put(fullName, d);
		if(null != altName) {
			m_domainMap.put(altName, d);
		}
	}

	@Override protected void reverseSequences(Connection dbc, DbSchema schema) throws Exception {
		if(dbc.getMetaData().getDatabaseMajorVersion() >= 10)
			reverseSequencesNew(dbc, schema);
		else
			reverseSequencesOld(dbc, schema);
	}

	static private final String SEQSQL =
		"select sequencename,data_type,start_value,min_value,max_value,increment_by,cache_size,last_value from pg_catalog.pg_sequences where schemaname=?";

	private void reverseSequencesNew(Connection dbc, DbSchema schema) throws Exception {
		try(PreparedStatement ps = dbc.prepareStatement(SEQSQL)) {
			ps.setString(1, schema.getName());
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					reverseSequence(schema, rs);
				}
			}
		}
	}

	static private String SEQSQLOLD = "select sequence_name,data_type,start_value,minimum_value,maximum_value,increment,-1,-1"
		+ " from information_schema.sequences"
		+ " where sequence_schema = ?";

	private void reverseSequencesOld(Connection dbc, DbSchema schema) throws Exception {
		try(PreparedStatement ps = dbc.prepareStatement(SEQSQLOLD)) {
			ps.setString(1, schema.getName());
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					reverseSequence(schema, rs);
				}
			}
		}

	}

	private void reverseSequence(DbSchema schema, ResultSet rs) throws SQLException {
		int i = 1;
		String name = rs.getString(i++);
		String type = rs.getString(i++);
		long startValue = rs.getLong(i++);
		long minValue = rs.getLong(i++);
		long maxValue = rs.getLong(i++);
		long increment = rs.getLong(i++);
		long cacheSize = rs.getLong(i++);
		long lastValue = rs.getLong(i);

		if(maxValue == Long.MAX_VALUE)
			maxValue = Long.MIN_VALUE;

		ColumnType columnType = decodeColumnTypeByExplicitCode(schema, Integer.MAX_VALUE, type);
		DbSequence seq = new DbSequence(schema, name, columnType, type, startValue, minValue, maxValue, increment, cacheSize, lastValue);
		schema.addSequence(seq);
	}

	static private final String COLFLD = "select column_name, ordinal_position, column_default, is_nullable, data_type"
		+ ", character_maximum_length, character_octet_length, numeric_precision, numeric_scale, description";

	static private final String COLSQL =
		" from information_schema.columns co\n"
			+ "inner join pg_catalog.pg_namespace sch\n"
			+ "    on co.table_schema = sch.nspname\n"
			+ "inner join pg_catalog.pg_class cl\n"
			+ "    on cl.relname = co.table_name\n"
			+ "    and cl.relnamespace = sch.oid\n"
			+ "left join pg_catalog.pg_description pd\n"
			+ "\ton pd.objoid = cl.oid\n"
			+ "\tand pd.objsubid = co.ordinal_position\n"
			+ " where co.table_schema=? and co.table_name = ?"
			+ " order by ordinal_position";

	/**
	 * Reverse all cols for a table.
	 */
	@Override public void reverseColumns(@NonNull Connection dbc, DbTable t) throws Exception {
		Map<String, Integer> datymap = new HashMap<>();
		try(ResultSet rs = dbc.getMetaData().getColumns(null, t.getSchema().getName(), t.getName(), null)) {
			// All columns in the schema.
			int lastord = -1;
			while(rs.next()) {
				String name = rs.getString("COLUMN_NAME");
				int daty = rs.getInt("DATA_TYPE");                            // Types.xxx
				datymap.put(name, daty);
			}
		}

		try(PreparedStatement ps = dbc.prepareStatement(COLFLD + COLSQL)) {
			ps.setString(1, t.getSchema().getName());
			ps.setString(2, t.getName());

			List<DbColumn> columnList = new ArrayList<>();
			Map<String, DbColumn> columnMap = new HashMap<>();
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					String colName = rs.getString(1);
					Integer daty = datymap.get(colName);

					DbColumn c = decodePostgresColumn(dbc, t, rs, daty == null ? Integer.MAX_VALUE : daty.intValue());
					if(null != columnMap.put(c.getName(), c))
						throw new IllegalStateException("Duplicate column name '" + c.getName() + "' in table " + t.getName());
					columnList.add(c);
				}
			}
			t.initializeColumns(columnList, columnMap);
		}
	}

	private DbColumn decodePostgresColumn(Connection dbc, DbTable t, ResultSet rs, int daty) throws Exception {
		int i = 1;
		String name = rs.getString(i++);
		int pos = rs.getInt(i++);
		String deflt = rs.getString(i++);
		boolean nullable = "YES".equalsIgnoreCase(rs.getString(i++));
		String typename = rs.getString(i++);
		int charLen = rs.getInt(i++);
		if(rs.wasNull())
			charLen = -1;
		int octets = rs.getInt(i++);
		if(rs.wasNull())
			octets = -1;
		int prec = rs.getInt(i++);
		if(rs.wasNull())
			prec = -1;
		int scale = rs.getInt(i++);
		if(rs.wasNull())
			scale = -1;
		if(prec == -1)
			prec = charLen;
		String comment = rs.getString(i++);

		boolean autoIncrement = typename.toLowerCase().contains("serial")
			|| (deflt != null && deflt.toLowerCase().contains("nextval"))
			;

		ColumnType ct = decodeColumnType(t.getSchema(), daty, typename);
		DbColumn c;
		if(ct == null) {
			c = reverseColumnUnknownType(rs, t, name, daty, typename, prec, scale, nullable, autoIncrement);
			if(null == c) {
				return null;
			}
		} else {
			c = createDbColumn(t, name, daty, typename, prec, scale, nullable, autoIncrement, ct);
		}
		c.setComment(comment);
		c.setDefault(deflt);

		DbSequence dbs = scanSequenceName(t.getSchema(), deflt);
		c.setUsedSequence(dbs);
		return c;
	}

	/**
	 * Extract sequence from "nextval('sectormodel.e_refdata_enum_id_seq'::regclass)"
	 */
	@Nullable
	private DbSequence scanSequenceName(DbSchema schema, @Nullable String deflt) {
		if(null == deflt)
			return null;

		if(null != deflt && deflt.toLowerCase().startsWith(NEXTVAL)) {
			String sub = deflt.substring(NEXTVAL.length());

			//-- Do we have a cast?
			int pos = sub.indexOf("::");
			if(pos > 0) {
				sub = sub.substring(0, pos);					// Remove cast
			}

			while(sub.endsWith(")") || sub.endsWith("'"))
				sub = sub.substring(0, sub.length() - 1);

			//-- We should now have a sequence name. Does it have a schema?
			pos = sub.indexOf('.');
			if(pos == -1) {
				DbSequence seq = schema.findSequence(sub);
				if(null == seq) {
					log("Sequence '" + sub + "' not found in column default '" + deflt + "'");
				}
				return seq;
			}

			//-- Find the schema
			DbSchema subSchemaName = findSchema(sub.substring(0, pos));
			DbSchema s = subSchemaName;
			if(null == s) {
				log("Schema '" + subSchemaName + "' not found in column default '" + deflt + "'");
				return null;
			}
			String seqName = sub.substring(pos + 1).trim();
			DbSequence seq = schema.findSequence(seqName);
			if(null == seq) {
				log("Sequence '" + sub + "' not found in column default '" + deflt + "'");
			}
			return seq;
		}

		return null;
	}

	@NonNull @Override protected DbColumn createDbColumn(DbTable t, String name, int daty, String typename, int prec, int scale, boolean nulla, Boolean autoIncrement, ColumnType ct) {
		if("bpchar".equals(typename))								// 8-(
			typename = "char";

		//-- The precision field for Postgres binary types like int32 holds a fscking 32, sign.
		switch(daty) {
			default:
				break;

			case Types.INTEGER:
				prec = 10;
				break;

			case Types.SMALLINT:
				prec = 5;
				break;

			case Types.BIGINT:
				prec = 20;
				break;
		}

		return super.createDbColumn(t, name, daty, typename, prec, scale, nulla, autoIncrement, ct);
	}

	/**
	 * Can be a domain type- check.
	 */
	@Override protected DbColumn reverseColumnUnknownType(ResultSet rs, DbTable t, String name, int sqlType, String typename, int prec, int scale, boolean nulla, Boolean autoIncrement) {
		if(sqlType == Types.OTHER || sqlType == Types.DISTINCT) {
			String fullName = t.getSchema().getName() + "." + typename;
			DbDomain domain = findDomain(fullName);
			if(null == domain) {
				domain = findDomain(typename);
				if(null != domain) {
					DbColumn c = new DbColumn(t, name, domain.getType(), domain.getPrecision(), domain.getScale(), nulla, autoIncrement, domain.getSqlType(), domain.getPlatformTypeName());
					return c;
				}
			}
		}

		if(sqlType == Types.OTHER) {
			if("uuid".equals(typename)) {
				ColumnType ct = ColumnType.VARCHAR;
				return new DbColumn(t, name, ct, prec, 0, nulla, autoIncrement, ct.getSqlType(), ct.getName());
			}
			if("json".equals(typename)) {
				ColumnType ct = ColumnType.JSON;
				return new DbColumn(t, name, ct, prec, 0, nulla, autoIncrement, ct.getSqlType(), ct.getName());
			}
		}


		return super.reverseColumnUnknownType(rs, t, name, sqlType, typename, prec, scale, nulla, autoIncrement);
	}

	@Override
	public ColumnType decodeColumnTypeByExplicitCode(@Nullable DbSchema schema, int sqltype, @Nullable String typename) {
		if("oid".equalsIgnoreCase(typename))
			return ColumnType.BLOB;
		if("bytea".equalsIgnoreCase(typename))
			return ColumnType.BINARY;
		if("text".equalsIgnoreCase(typename))
			return ColumnType.CLOB;

		return super.decodeColumnTypeByExplicitCode(schema, sqltype, typename);
	}

	//@Override protected ColumnType decodeColumnTypeByPlatformName(DbSchema schema, int sqltype, String typename) {
	//	ColumnType columnType = super.decodeColumnTypeByPlatformName(schema, sqltype, typename);
	//	if(null == columnType) {
	//		//-- Check domain
	//		if(sqltype == Types.OTHER) {
	//			if(null != schema) {
	//				String fullName = schema.getName() + "." + typename;
	//				DbDomain domain = findDomain(fullName);
	//				return domain.
	//
	//			}
	//
	//
	//
	//
	//		}
	//
	//
	//	}
	//
	//	return columnType;
	//}

	@Nullable
	public DbDomain findDomain(String fqdn) {
		return m_domainMap.get(fqdn);
	}

	@Override
	public String wrapQueryWithRange(List<DbColumn> collist, String sql, int first, int max) {
		return sql + " limit " + max + " offset " + first;
	}
}
