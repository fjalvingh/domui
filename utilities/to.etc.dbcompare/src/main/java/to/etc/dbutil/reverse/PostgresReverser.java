package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbDomain;
import to.etc.dbutil.schema.DbSchema;
import to.etc.dbutil.schema.DbTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostgresReverser extends JDBCReverser {
	private Map<String, DbDomain> m_domainMap = new HashMap<>();

	public PostgresReverser(DataSource dbc) {
		super(dbc);
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

	/**
	 * Can be a domain type- check.
	 */
	@Override protected DbColumn reverseColumnUnknownType(ResultSet rs, DbTable t, String name, int sqlType, String typename, int prec, int scale, int nulla, Boolean autoIncrement) {
		if(sqlType == Types.OTHER || sqlType == Types.DISTINCT) {
			String fullName = t.getSchema().getName() + "." + typename;
			DbDomain domain = findDomain(fullName);
			if(null == domain) {
				domain = findDomain(typename);
				if(null != domain) {
					DbColumn c = new DbColumn(t, name, domain.getType(), domain.getPrecision(), domain.getScale(), nulla == DatabaseMetaData.columnNullable, autoIncrement, domain.getSqlType(), domain.getPlatformTypeName());
					return c;
				}
			}
		}

		if(sqlType == Types.OTHER) {
			if("uuid".equals(typename)) {
				ColumnType ct = ColumnType.VARCHAR;
				return new DbColumn(t, name, ct, prec, 0, nulla == DatabaseMetaData.columnNullable, autoIncrement, ct.getSqlType(), ct.getName());
			}
		}


		return super.reverseColumnUnknownType(rs, t, name, sqlType, typename, prec, scale, nulla, autoIncrement);
	}

	@Override
	public ColumnType decodeColumnTypeByCode(@Nullable DbSchema schema, int sqltype, @Nullable String typename) {
		if("oid".equalsIgnoreCase(typename))
			return ColumnType.BLOB;
		if("bytea".equalsIgnoreCase(typename))
			return ColumnType.BINARY;
		if("text".equalsIgnoreCase(typename))
			return ColumnType.CLOB;

		return super.decodeColumnTypeByCode(schema, sqltype, typename);
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
