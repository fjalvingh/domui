package to.etc.dbcompare;

import java.io.*;

import to.etc.dbcompare.db.*;
import to.etc.dbcompare.generator.*;

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

	/**
	 * tries to load a serialized planset. Returns null if load fails.
	 * @param src
	 * @return
	 */
	private Schema loadSchema(File src) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(src));
			Schema s = (Schema) ois.readObject();
			System.out.println("Schema loaded from " + src);
			return s;
		} catch(Exception x) {
			System.out.println("Loading schema failed: " + x);
			return null;
		} finally {
			try {
				if(ois != null)
					ois.close();
			} catch(Exception x) {}
		}
	}

	private void saveSchema(File dst, Schema ps) throws Exception {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(dst));
			oos.writeObject(ps);
		} catch(Exception x) {
			System.out.println("Saving schema failed: " + x);
		} finally {
			try {
				if(oos != null)
					oos.close();
			} catch(Exception x) {}
		}
	}

	private Database initDb(String poolid, String schema) throws Exception {
		//-- 1. Try to load an earlier schema,
		File sf = new File(poolid + "-" + schema + ".ser");
		Schema s = loadSchema(sf);
		if(s != null)
			return new Database(poolid, s);
		Database d = new Database(poolid);
		d.reverse(schema);
		saveSchema(sf, d.getSchema());
		return d;
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
