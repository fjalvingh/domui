package to.etc.dbcompare.reverse;

import java.sql.*;
import java.util.*;

public class ReverserRegistry {
	static private List<ReverserFactory>	m_factories	= new ArrayList<ReverserFactory>();

	static public void register(ReverserFactory f) {
		m_factories.add(f);
	}

	static public Reverser findReverser(Connection dbc, String schemaname) {
		for(ReverserFactory f : m_factories) {
			try {
				Reverser r = f.createReverser(dbc, schemaname);
				if(r != null)
					return r;
			} catch(Exception x) {}
		}
		return new JDBCReverser(dbc, schemaname);
	}

	static {
		/**
		 * Oracle reverser
		 */
		register(new ReverserFactory() {

			public Reverser createReverser(Connection dbc, String schemaName) throws Exception {
				DatabaseMetaData dmd = dbc.getMetaData();
				if(dmd.getDatabaseProductName().toLowerCase().contains("oracle"))
					return new OracleReverser(dbc, schemaName);
				return null;
			}

		});
	}
}
