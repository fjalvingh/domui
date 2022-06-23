package to.etc.dbutil.reverse;

import to.etc.dbutil.schema.DbSchema;
import to.etc.util.FileTool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 23-06-2022.
 */
public class MysqlReverser extends JDBCReverser {
	public MysqlReverser(DataSource dbc, Set<ReverserOption> optionSet) {
		super(dbc, optionSet);
	}

	public MysqlReverser(Connection conn, Set<ReverserOption> optionSet) {
		super(conn, optionSet);
	}

	/**
	 * MySQL does not have schema's, so return one with an empty name 8-/
	 */
	@Override
	public Set<DbSchema> getSchemas(boolean lazily) throws Exception {
		return loadSchemaSet(lazily);
	}

	private Set<DbSchema> loadSchemaSet(boolean lazily) throws Exception {
		DbSchema schema = new DbSchema(this, "");					// MySQL has no schema's so return an unnamed one

		Connection dbc = getDataSource().getConnection();
		try {
			Set<DbSchema> schemaSet = Set.of(schema);
			reverseSchemaSet(dbc, schemaSet, lazily);
			return schemaSet;
		} finally {
			if(!isKeepConnectionsOpen()) {
				FileTool.closeAll(dbc);
			}
		}
	}
}
