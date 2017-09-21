package to.etc.domui.hibgen;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import to.etc.util.DbConnectionInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-9-17.
 */
public class HibernateGenerator {
	enum DbType {
		postgres,
		oracle
	}

	@Option(name = "-db", usage = "The connection string to the database, as accepted by the specific database version", required = true)
	private String m_dbUrl;

	@Option(name = "-dbtype", usage = "The type of database (oracle, postgresql)")
	private DbType m_dbType = DbType.postgres;

	@Option(name = "-source", usage = "The root directory for the generated/modified java sources (without the package name)", required = true)
	private File m_targetDirectory;

	@Option(name = "-pkgroot", usage = "The root Java package where the tables will be generated", required = true)
	private String m_packageName;

	@Option(name = "-s", aliases = {"-schema"}, usage = "One or more schema names to include in the reverse action", required = true)
	private List<String> m_schemaSet = new ArrayList<>();

	private void run(String[] args) throws Exception {
		CmdLineParser p = new CmdLineParser(this);
		try {
			//-- Decode the tasks's arguments
			p.parseArgument(args);
		} catch (CmdLineException x) {
			System.err.println("Invalid arguments: " + x.getMessage());
			System.err.println("Usage:");
			p.printUsage(System.err);
			System.exit(10);
		}

		String dbUrl = Objects.requireNonNull(m_dbUrl);
		DbConnectionInfo url = DbConnectionInfo.decode(dbUrl);

		AbstractGenerator generator;
		switch(m_dbType) {
			default:
				throw new IllegalStateException(m_dbType + ": unsupported");

			case postgres:
				generator = new PostgresGenerator(url);
				break;

			case oracle:
				generator = new OracleGenerator(url);
				break;
		}

		try {
			generator.createConnection();
			generator.loadSchemas(m_schemaSet);



		} finally {
			generator.close();
		}
	}

	static public void main(String[] args) throws Exception {
		new HibernateGenerator().run(args);
	}
}
