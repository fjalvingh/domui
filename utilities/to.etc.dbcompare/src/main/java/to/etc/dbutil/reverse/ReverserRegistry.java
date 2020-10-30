package to.etc.dbutil.reverse;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ReverserRegistry {
	static private List<ReverserFactory> m_factories = new ArrayList<ReverserFactory>();

	static public synchronized void register(ReverserFactory f) {
		m_factories.add(f);
	}

	static public synchronized Reverser findReverser(DataSource ds, Set<ReverserOption> optionSet) throws Exception {
		try(Connection dbc = ds.getConnection()) {
			DatabaseMetaData dmd = dbc.getMetaData();
			for(ReverserFactory f : m_factories) {
				try {
					Reverser r = f.createReverser(ds, dmd, optionSet);
					if(r != null)
						return r;
				} catch(Exception x) {
				}
			}
		}
		return new JDBCReverser(ds, optionSet);
	}

	static {
		/**
		 * Oracle reverser
		 */
		register(new ReverserFactory() {
			@Override
			public Reverser createReverser(DataSource dbc, DatabaseMetaData dmd, Set<ReverserOption> optionSet) throws Exception {
				if(dmd.getDatabaseProductName().toLowerCase().contains("oracle"))
					return new OracleReverser(dbc, optionSet);
				return null;
			}

		});

		register(new ReverserFactory() {
			@Override
			public Reverser createReverser(DataSource dbc, DatabaseMetaData dmd, Set<ReverserOption> optionSet) throws Exception {
				if(dmd.getDatabaseProductName().toLowerCase().contains("postgres"))
					return new PostgresReverser(dbc, optionSet);
				return null;
			}
		});
	}
}
