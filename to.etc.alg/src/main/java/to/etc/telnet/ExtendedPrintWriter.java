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
package to.etc.telnet;

import to.etc.util.StringTool;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ExtendedPrintWriter extends PrintWriter {
	static public final int		STR			= 0;

	static public final int		INT			= 1;

	static public final int		COMMAD		= 2;

	static public final int		SIZE		= 3;

	static public final int		STRTRUNC	= 4;

	static private final FSpec	NULL_FSPEC	= new FSpec(0, -1, "", false);

	private List<FSpec>			m_flist		= new ArrayList<FSpec>();

	private boolean				m_init;

	private int					m_index;

	public ExtendedPrintWriter(Writer out) {
		super(out);
	}

	/**
	 * Print a string with a fixed width. If the size overflows it takes the overflown size.
	 * @param s
	 * @param width
	 */
	public void print(String s, int width) {
		int len = s.length();
		while(len++ < width)
			print(' ');
	}

	public void printright(String s, int width) {
		int len = s.length();
		while(len++ < width)
			print(' ');
		print(s);
	}

	public void print(long value, int width) {
		printright(Long.toString(value), width);
	}

	public void printCommad(long value, int width) {
		printright(StringTool.strCommad(value), width);
	}

	public void printSize(long value, int width) {
		print(StringTool.strSize(value), width);
	}

	/*--------------------------------------------------------------*/
	/*	CODING: Formatted printer.                           		*/
	/*--------------------------------------------------------------*/
	static private class FSpec {
		public String	m_header;

		public int		m_type;

		public int		m_width;

		public boolean	m_right;

		public FSpec(int width, int type, String header, boolean right) {
			m_width = width;
			m_type = type;
			m_header = header;
			m_right = right;
		}
	}

	public void clear() {
		m_flist.clear();
		if(m_index > 0)
			done();
		m_init = false;
	}

	public void add(int width, int format, String header, boolean right) {
		m_flist.add(new FSpec(width, format, header, right));
	}

	public void add(int width, int format, String header) {
		m_flist.add(new FSpec(width, format, header, false));
	}

	public void header(String hdr, int type) {
		add(hdr.length(), type, hdr, (type == INT || type == COMMAD));
	}

	private void init() {
		if(!m_init) {
			for(FSpec s : m_flist) {
				if(m_init)
					print(' ');
				print(s.m_header);
				m_init = true;
			}
			m_init = true;
			println();
		}
	}

	private FSpec current() {
		if(m_index >= m_flist.size())
			return NULL_FSPEC;
		return m_flist.get(m_index);
	}

	public void out(String s) {
		init();
		FSpec f = current();
		m_index++;
		if(m_index != 1)
			print(' ');
		int len = s.length();
		if(f.m_type == STRTRUNC && len > f.m_width) {
			s = s.substring(0, f.m_width);
			len = f.m_width;
		}
		if(f.m_right) {
			while(len++ < f.m_width)
				print(' ');
		}
		print(s);
		if(!f.m_right) {
			while(len++ < f.m_width)
				print(' ');
		}
		if(m_index >= m_flist.size()) {
			m_index = 0;
			println();
		}
	}

	public void done() {
		if(m_index > 0) {
			m_index = 0;
			println();
		}
	}

	public void out(long v) {
		init();
		FSpec f = current();
		switch(f.m_type){
			case COMMAD:
				out(StringTool.strCommad(v));
				break;
			case SIZE:
				out(StringTool.strSize(v));
				break;
			default:
				out(Long.toString(v));
				break;
		}
	}
}
