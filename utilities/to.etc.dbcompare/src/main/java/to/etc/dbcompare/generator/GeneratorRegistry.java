package to.etc.dbcompare.generator;

import java.sql.*;
import java.util.*;

public class GeneratorRegistry {
	static private List<GeneratorFactory> m_factories = new ArrayList<GeneratorFactory>();

	static public void register(GeneratorFactory f) {
		m_factories.add(f);
	}

	static public AbstractGenerator findGenerator(Connection dbc) {
		for(GeneratorFactory f : m_factories) {
			try {
				AbstractGenerator r = f.createGenerator(dbc);
				if(r != null)
					return r;
			} catch(Exception x) {}
		}
		return null;
	}

	static {
		/**
		 * Oracle Generator
		 */
		register(new GeneratorFactory() {
			@Override
			public AbstractGenerator createGenerator(Connection dbc) throws Exception {
				DatabaseMetaData dmd = dbc.getMetaData();
				if(dmd.getDatabaseProductName().toLowerCase().contains("oracle"))
					return new OracleGenerator();
				return null;
			}

			@Override
			public AbstractGenerator createGenerator(String id) throws Exception {
				if(id.startsWith("oracle"))
					return new OracleGenerator();
				return null;
			}
		});
		/**
		 * Postgresql Generator
		 */
		register(new GeneratorFactory() {
			@Override
			public AbstractGenerator createGenerator(Connection dbc) throws Exception {
				DatabaseMetaData dmd = dbc.getMetaData();
				if(dmd.getDatabaseProductName().toLowerCase().contains("postgres"))
					return new PostgresGenerator();
				return null;
			}

			@Override
			public AbstractGenerator createGenerator(String id) throws Exception {
				if(id.startsWith("postgres"))
					return new PostgresGenerator();
				return null;
			}
		});
	}
}
