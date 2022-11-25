package to.etc.dbutil.reverse;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.dbutil.schema.DbSchema;
import to.etc.util.FileTool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the messy way MySQL handles tables (in catalogs, which are actually databases). As
 * catalogs are apparently seen regardless of which "database" you connect to we will treat them
 * as schema's.
 *
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
	 * MySQL does not have schema's, but we can access all "databases" as if
	 * they are... So return the databases and treat them as schema's.
	 */
	@Override
	protected Set<DbSchema> getSchemasOnly(boolean lazily) throws Exception {
		Connection dbc = getDataSource().getConnection();
		try {
			Set<DbSchema> schemaSet = new HashSet<>();
			DatabaseMetaData dmd = dbc.getMetaData();
			try(ResultSet rs = dmd.getCatalogs()) {
				while(rs.next()) {
					String name = rs.getString("TABLE_CAT");
					name = translateSchemaName(dbc, name);
					if(null != name) {
						DbSchema schema = new DbSchema(this, null, name);		// This is a catalog treated as a schema
						schemaSet.add(schema);
					}
				}
			}
			return schemaSet;
		} finally {
			if(!isKeepConnectionsOpen()) {
				FileTool.closeAll(dbc);
			}
		}
	}

	@Override
	@Nullable
	protected String getSchemaFromMetadataSet(ResultSet rs) throws Exception {
		return rs.getString("TABLE_CAT");
	}

}
