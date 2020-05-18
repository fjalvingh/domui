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

import org.eclipse.jdt.annotation.NonNull;

import java.util.Properties;

/**
 * Pool parameters as a set of properties but for a single pool, so that
 * each property does not have the poolID as a prefix. I.e. we use maxconn
 * instead of id.maxconn to specify max connections.
 */
public class SinglePoolPropertiesSource extends PoolConfigSource {
	final private Properties m_prop = new Properties();

	public SinglePoolPropertiesSource(@NonNull Properties p) {
		m_prop.putAll(p);
	}

	@Override
	public String getProperty(String section, String name) throws Exception {
		return m_prop.getProperty(name);
	}

	public SinglePoolPropertiesSource property(String name, String value) {
		m_prop.setProperty(name, value);
		return this;
	}

}
