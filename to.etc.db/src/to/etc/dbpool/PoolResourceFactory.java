package to.etc.dbpool;

import java.io.*;
import java.util.*;

import javax.naming.*;
import javax.naming.spi.*;

/**
 * Object factory for the database pool. This allows this pool to be used from within applications.
 * 
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 16, 2007
 */
public class PoolResourceFactory implements ObjectFactory {

	public Object getObjectInstance(Object arg0, Name arg1, Context arg2, Hashtable arg3) throws Exception {
		System.out.println("Called getObjectFactory.");

		//-- 1. Initialize the pool, and return a datasource.
		final Map map = new HashMap();

		Reference ref = (Reference) arg0;
		for(Enumeration e = ref.getAll(); e.hasMoreElements();) {
			RefAddr ra = (RefAddr) e.nextElement();
			String name = ra.getType();
			String val = (String) ra.getContent();
			if(name.equals("factory"))
				continue;
			map.put(name, val);
		}

		final String id = (String) map.get("poolid");
		if(id == null)
			throw new IllegalArgumentException("Missing 'poolid' parameter for database pool");
		String pfname = (String) map.get("poolfile");
		ConnectionPool p;
		if(pfname != null) {
			//-- File-based pool.
			File f = new File(pfname);
			if(!f.exists())
				throw new IllegalArgumentException("The poolfile " + f + " does not exist");
			p = PoolManager.getInstance().initializePool(f, id);
		} else {
			//-- We expect all thingies from the map.
			PoolConfigSource cs = new PoolConfigSource() {

				@Override
				public String getProperty(String section, String name) throws Exception {
					return (String) map.get(name);
				}
			};
			p = PoolManager.getInstance().initializePool(cs, id);
		}
		return p.getPooledDataSource();
	}
}
