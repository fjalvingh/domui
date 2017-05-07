package to.etc.dbcompare;

import java.io.*;

import to.etc.dbcompare.generator.*;
import to.etc.dbutil.schema.*;

public class CompareDB {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new CompareDB().run(args);
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	private Database initDb(String poolid, String schema) throws Exception {
		//-- 1. Try to load an earlier schema,
		File sf = new File(poolid + "-" + schema + ".ser");
		return Database.loadSchema(poolid, schema, sf);
	}

	/**
	 * Usage: src-poolid src-schemaname dst-poolid dst-schemaname
	 * @param args
	 * @throws Exception
	 */
	private void run(String[] args) throws Exception {
		if(args.length != 4)
			throw new IllegalStateException("Usage: src-poolid src-schema dst-poolid dst-schema");
		//-- Allocate connections to compare,
		Database src = initDb(args[0], args[1]);
		Database dest = initDb(args[2], args[3]);

		//-- Get a default output thing
		AbstractGenerator g = GeneratorRegistry.findGenerator(dest.dbc());
		if(g == null)
			throw new IllegalStateException("No generator for target database");
		System.out.println("Generating delta using " + g.getIdent());

		AlteringSchemaComparator dp = new AlteringSchemaComparator(src.getSchema(), dest.getSchema(), g);
		dp.run();

		PrintWriter pw = new PrintWriter(new FileWriter(new File("/tmp/sql.sql")));
		dp.render(pw);
		pw.close();
	}

}
