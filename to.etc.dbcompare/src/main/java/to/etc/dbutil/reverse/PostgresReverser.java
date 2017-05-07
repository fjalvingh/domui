package to.etc.dbutil.reverse;

import java.sql.*;
import java.util.*;

import javax.sql.*;

import to.etc.dbutil.schema.*;

public class PostgresReverser extends JDBCReverser {
	public PostgresReverser(DataSource dbc, DatabaseMetaData dmd) {
		super(dbc, dmd);
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
