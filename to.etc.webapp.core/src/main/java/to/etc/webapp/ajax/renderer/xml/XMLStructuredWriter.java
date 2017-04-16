/*
 * DomUI Java User Interface library
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
package to.etc.webapp.ajax.renderer.xml;

import java.io.*;
import java.util.*;

import to.etc.webapp.ajax.renderer.*;
import to.etc.xml.*;

public class XMLStructuredWriter extends StructuredWriter {
	//	static private final byte	SOR		= 0;
	static private final byte REC = 1;

	static private final byte LIST = 2;

	private int m_lvl = 0;

	private final byte[] m_type = new byte[50];

	private final XmlRenderer m_r;

	public XMLStructuredWriter(final XmlRenderer r) {
		super(r.getWriter());
		m_r = r;
	}

	public XmlWriter xw() {
		return m_r.xw();
	}

	@Override
	public void end() throws IOException {
		if(m_lvl <= 0)
			throw new IllegalStateException("Underflow.");
		byte type = m_type[m_lvl--];
		switch(type){
			case LIST:
			case REC:
				xw().tagendnl();
				break;
		}
	}

	/**
	 * Only allowed in a record.
	 * @see to.etc.webapp.ajax.renderer.StructuredWriter#field(java.lang.String, java.lang.String)
	 */
	@Override
	public void field(final String name, final String value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	@Override
	public void field(final String name, final boolean value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	@Override
	public void field(final String name, final Date value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	@Override
	public void field(final String name, final Number value) throws Exception {
		byte type = m_type[m_lvl];
		if(type != REC)
			throw new IllegalStateException("A field is valid in a record only (field=" + name + ", value=" + value + ")");
		xw().tagfull(name, value);
	}

	/**
	 * Starts a list. The list is simply a tag with the specified name.
	 *
	 * @see to.etc.webapp.ajax.renderer.StructuredWriter#list(java.lang.String)
	 */
	@Override
	public void list(final String name) throws Exception {
		xw().tag(name);
		m_type[++m_lvl] = LIST;
	}

	/**
	 * Starts a record. Yes, another tag.
	 *
	 * @see to.etc.webapp.ajax.renderer.StructuredWriter#record(java.lang.String)
	 */
	@Override
	public void record(final String name) throws Exception {
		xw().tag(name);
		m_type[++m_lvl] = REC;
	}

	@Override
	public void close() throws IOException {
		while(m_lvl > 0)
			end();
	}

	@Override
	public void flush() throws IOException {}
}
