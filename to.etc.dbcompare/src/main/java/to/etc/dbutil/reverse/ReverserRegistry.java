package to.etc.dbutil.reverse;

import to.etc.util.FileTool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class ReverserRegistry {
	static private List<ReverserFactory> m_factories = new ArrayList<ReverserFactory>();

	static public synchronized void register(ReverserFactory f) {
		m_factories.add(f);
	}

	static public synchronized Reverser findReverser(DataSource ds) throws Exception {
		try(Connection dbc = ds.getConnection()) {
			DatabaseMetaData dmd = dbc.getMetaData();
			for(ReverserFactory f : m_factories) {
				try {
					Reverser r = f.createReverser(ds, dmd);
					if(r != null)
						return r;
				} catch(Exception x) {
				}
			}
		}
		return new JDBCReverser(ds);
	}

	static {
		/**
		 * Oracle reverser
		 */
		register(new ReverserFactory() {
			@Override
			public Reverser createReverser(DataSource dbc, DatabaseMetaData dmd) throws Exception {
				if(dmd.getDatabaseProductName().toLowerCase().contains("oracle"))
					return new OracleReverser(dbc);
				return null;
			}

		});

		register(new ReverserFactory() {
			@Override
			public Reverser createReverser(DataSource dbc, DatabaseMetaData dmd) throws Exception {
				if(dmd.getDatabaseProductName().toLowerCase().contains("postgres"))
					return new PostgresReverser(dbc);
				return null;
			}
		});
	}
}
