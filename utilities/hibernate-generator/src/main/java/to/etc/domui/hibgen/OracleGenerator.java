package to.etc.domui.hibgen;

import to.etc.dbutil.reverse.OracleReverser;
import to.etc.dbutil.reverse.Reverser;
import to.etc.dbutil.reverse.ReverserFactory;
import to.etc.dbutil.reverse.ReverserRegistry;
import to.etc.util.DbConnectionInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class OracleGenerator extends AbstractGenerator {
	private final DbConnectionInfo m_connectorUrl;

	public OracleGenerator(DbConnectionInfo url) {
		m_connectorUrl = url;
	}

	@Override protected Connection createConnection() throws Exception {
		DbConnectionInfo parameters = m_connectorUrl;
		Class.forName("oracle.jdbc.driver.OracleDriver");
		int port = parameters.getPort();
		if(port <= 0)
			port = 1521;

		String url = "jdbc:oracle:thin:@" + parameters.getHostname() + ":" + port + ":" + parameters.getSid();
		Properties prop = new Properties();
		prop.setProperty("user", parameters.getUserid());
		prop.setProperty("password", parameters.getPassword());
		Connection connection = DriverManager.getConnection(url, prop);
		connection.setAutoCommit(false);
		return connection;
	}

	@Override protected void loadSchemas() throws Exception {
		Reverser reverser = ReverserRegistry.findReverser(getFakeDatasource());
	}
}
