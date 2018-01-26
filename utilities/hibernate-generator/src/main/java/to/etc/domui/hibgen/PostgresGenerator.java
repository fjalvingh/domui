package to.etc.domui.hibgen;

import to.etc.dbutil.reverse.Reverser;
import to.etc.dbutil.reverse.ReverserRegistry;
import to.etc.dbutil.schema.DbColumn;
import to.etc.dbutil.schema.DbSchema;
import to.etc.util.DbConnectionInfo;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class PostgresGenerator extends AbstractGenerator {
	private final DbConnectionInfo m_connectorUrl;

	public PostgresGenerator(DbConnectionInfo connectorUrl) {
		m_connectorUrl = connectorUrl;
	}

	@Override protected Connection createConnection() throws Exception {
		DbConnectionInfo parameters = m_connectorUrl;
		Class.forName("org.postgresql.Driver");
		int port = parameters.getPort();
		if(port <= 0)
			port = 5432;

		String url = "jdbc:postgresql://" + parameters.getHostname() + ":" + port + "/" + parameters.getSid();
		Properties prop = new Properties();
		prop.setProperty("user", parameters.getUserid());
		prop.setProperty("password", parameters.getPassword());
		prop.setProperty("defaultRowFetchSize", "65536");
		prop.setProperty("reWriteBatchedInserts", "true");
		Connection connection = DriverManager.getConnection(url, prop);
		connection.setAutoCommit(false);
		return connection;
	}

	@Override protected Set<DbSchema> loadSchemas(List<String> schemaSet) throws Exception {
		Reverser reverser = ReverserRegistry.findReverser(getFakeDatasource());
		return reverser.loadSchemaSet(schemaSet, false);
	}

	static private final String SQL = "select case a.atttypid\n"
		+ "   when 'int'::regtype  then 'serial'\n"
		+ "   when 'int8'::regtype then 'bigserial'\n"
		+ "   when 'int2'::regtype then 'smallserial'\n"
		+ "   end as serial_type\n"
		+ " , ad.adsrc\n"
		+ "from   pg_attribute  a\n"
		+ " join   pg_constraint c on c.conrelid  = a.attrelid\n"
		+ "         and c.conkey[1] = a.attnum\n"
		+ " join   pg_attrdef   ad on ad.adrelid  = a.attrelid\n"
		+ "         and ad.adnum    = a.attnum\n"
		+ "where   a.attrelid = ?::regclass   -- table name, optionally schema-qualified\n"
		+ "   and     a.attnum > 0\n"
		+ "   and     not a.attisdropped\n"
		+ "   and     a.atttypid = any('{int,int8,int2}'::regtype[]) -- integer type\n"
		+ "   and     c.contype = 'p'                 -- PK\n"
		+ "   and     array_length(c.conkey, 1) = 1   -- single column\n"
		+ "   and  a.attname = ?";

	@Nullable @Override protected String getIdColumnSequence(DbColumn column) throws Exception {
		try(PreparedStatement ps = dbc().prepareStatement(SQL)) {
			ps.setString(1, column.getTable().getSchema().getName() + "." + column.getTable().getName());
			ps.setString(2, column.getName());
			try(ResultSet rs = ps.executeQuery()) {
				if(rs.next()) {
					String serial = rs.getString(1);
					String deflt = rs.getString(2);

					if(null != deflt) {
						String hdr = "nextval('";
						if(deflt.startsWith(hdr)) {
							int ep = deflt.indexOf('\'', hdr.length());
							if(ep > hdr.length()) {
								String sequence = deflt.substring(hdr.length(), ep);
								if(!sequence.contains(".")) {
									//-- Append schema name
									sequence = column.getTable().getSchema().getName() + "." + sequence;
									return sequence;
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
}
