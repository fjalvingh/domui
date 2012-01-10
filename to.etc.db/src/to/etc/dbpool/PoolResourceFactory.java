/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.dbpool;

import java.io.*;
import java.util.*;

import javax.naming.*;
import javax.naming.spi.*;

/**
 * Object factory for the database pool. This allows this pool to be used from within applications.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 16, 2007
 */
public class PoolResourceFactory implements ObjectFactory {
	public Object getObjectInstance(Object arg0, Name arg1, Context arg2, Hashtable< ? , ? > arg3) throws Exception {
		System.out.println("Called getObjectFactory.");

		//-- 1. Initialize the pool, and return a datasource.
		final Map<String, String> map = new HashMap<String, String>();

		Reference ref = (Reference) arg0;
		for(Enumeration<RefAddr> e = ref.getAll(); e.hasMoreElements();) {
			RefAddr ra = e.nextElement();
			String name = ra.getType();
			String val = (String) ra.getContent();
			if(name.equals("factory"))
				continue;
			map.put(name, val);
		}

		final String id = map.get("poolid");
		if(id == null)
			throw new IllegalArgumentException("Missing 'poolid' parameter for database pool");
		String pfname = map.get("poolfile");
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
					return map.get(name);
				}
			};
			p = PoolManager.getInstance().initializePool(cs, id);
		}
		return p.getPooledDataSource();
	}
}
