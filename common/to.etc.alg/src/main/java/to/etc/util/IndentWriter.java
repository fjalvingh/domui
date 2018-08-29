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
package to.etc.util;

import java.io.*;

/**
 * Handles indented writing for dumps and the like.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on May 3, 2004
 */
public class IndentWriter extends Writer {
	/** The writer to dump to. */
	private Writer				m_pw;

	private int					m_spacesPerLevel	= 2;

	/** Current code indent level */
	private int					m_ind_level			= 0;

	/** The current x-position in the file. */
	private int					m_ind_x				= 0;

	private boolean				m_dontclose;

	private boolean				m_indentEnabled		= true;

	static final private char[]	SPACES				= "                                                                                                                                                                 "
														.toCharArray();

	/**
	 * Constructor
	 */
	public IndentWriter() {
	}

	public IndentWriter(Writer w) {
		m_pw = w;
	}

	public IndentWriter(Writer w, boolean dontclose) {
		m_pw = w;
		m_dontclose = dontclose;
	}

	public void init(Writer w, int indentlevel) {
		m_pw = w;
		m_ind_x = 0;
		m_ind_level = indentlevel;
	}

	public boolean isIndentEnabled() {
		return m_indentEnabled;
	}

	public void setIndentEnabled(boolean indentEnabled) {
		m_indentEnabled = indentEnabled;
	}

	@Override
	public void close() throws IOException {
		if(m_dontclose)
			m_pw.flush();
		else
			m_pw.close();
	}

	@Override
	public void flush() throws IOException {
		m_pw.flush();
	}

	public void setInd(int i) {
		m_ind_level = i;
	}

	public void setSpacesPerIndent(int spi) {
		m_spacesPerLevel = spi;
	}

	public void writeRaw(String s) throws IOException {
		m_pw.write(s);
		m_ind_x += s.length();
	}

	public void writeRaw(char[] buf, int off, int len) throws IOException {
		m_pw.write(buf, off, len);
		m_ind_x += len;
	}

	public void writeRaw(String buf, int off, int len) throws IOException {
		m_pw.write(buf, off, len);
		m_ind_x += len;
	}

	/**
	 * Output as many spaces as needed to reach the current indent level.
	 */
	public void indent() throws IOException {
		if(!m_indentEnabled)
			return;
		int ex = m_ind_level * m_spacesPerLevel;
		if(ex > SPACES.length)
			ex = SPACES.length;
		if(m_ind_x >= ex)
			return;
		m_pw.write(SPACES, 0, ex - m_ind_x);
		m_ind_x = ex;
	}

	@Override
	public void write(char[] buf, int off, int len) throws IOException {
		//** Ok. Generate the required string & indent after NL. Writes the code
		//** to the code stream.
		int eoff = off + len;
		int ix = off;
		int spos = ix;
		while(ix < eoff) {
			char c = buf[ix];
			if(c == '\n') {
				//-- Finish off the run before the newline
				if(ix > spos) {
					indent();
					m_pw.write(buf, spos, ix - spos); // Write the run,
				}
				ix = ix + 1;
				spos = ix; // Past newline
				m_pw.write('\n');
				m_ind_x = 0;
			} else
				ix++;
		}
		if(spos < eoff) {
			indent();
			m_pw.write(buf, spos, eoff - spos); // Write the last run,
			m_ind_x += eoff - spos;
		}
	}

	@Override
	public void write(String str) throws IOException {
		int ix = 0;
		int len = str.length();
		while(ix < len) {
			int epos = str.indexOf('\n', ix); // Next newline
			if(epos == -1) {
				indent();
				m_pw.write(str, ix, len - ix); // Write remaining
				m_ind_x += len - ix;
				return;
			}
			//-- Got new line- print everything before it, including the new line
			epos++;
			indent();
			m_pw.write(str, ix, epos - ix);
			m_ind_x = 0;
			ix = epos;
		}
	}

	public void inc() {
		m_ind_level++;
	}

	public void dec() {
		if(m_ind_level > 0)
			m_ind_level--;
	}

	public void forceNewline() throws IOException {
		if(m_ind_x > 0) {
			m_pw.write('\n');
			m_ind_x = 0;
		}
	}

	public void println() throws IOException {
		m_pw.write('\n');
		m_ind_x = 0;
	}

	public void print(String s) throws IOException {
		write(s);
	}

	public void println(String s) throws IOException {
		print(s);
		m_pw.write('\n');
		m_ind_x = 0;
	}

	public Writer getWriter() {
		return m_pw;
	}

	static public IndentWriter getStdoutWriter() {
		OutputStreamWriter osw = new OutputStreamWriter(System.out);
		return new IndentWriter(osw, true);
	}
}
