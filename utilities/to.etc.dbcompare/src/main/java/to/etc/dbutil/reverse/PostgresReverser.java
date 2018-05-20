package to.etc.dbutil.reverse;

import to.etc.dbutil.schema.ColumnType;
import to.etc.dbutil.schema.DbColumn;

import javax.sql.DataSource;
import java.util.List;

public class PostgresReverser extends JDBCReverser {
	public PostgresReverser(DataSource dbc) {
		super(dbc);
	}

	@Override public String getDefaultSchemaName() throws Exception {
		return "public";
	}

	@Override
	public ColumnType decodeColumnType(int sqltype, String typename) {
		if("oid".equalsIgnoreCase(typename))
			return ColumnType.BLOB;
		if("bytea".equalsIgnoreCase(typename))
			return ColumnType.BINARY;
		if("text".equalsIgnoreCase(typename))
			return ColumnType.CLOB;

		return super.decodeColumnType(sqltype, typename);
	}

	@Override
	public String wrapQueryWithRange(List<DbColumn> collist, String sql, int first, int max) {
		return sql + " limit " + max + " offset " + first;
	}
}
