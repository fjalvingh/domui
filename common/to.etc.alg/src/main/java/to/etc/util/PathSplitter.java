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

/**
 * Helps with splicing paths into fragments one part at a time.
 *
 * @author jal
 * Created on Dec 24, 2005
 */
final public class PathSplitter {
	/** The complete input path */
	private String	m_path;

	/** The size of the path */
	private int		m_length;

	/** The current fragment's start index */
	private int		m_curstart;

	/** The end of the current item, ex. slash */
	private int		m_curend;

	/** The next fragment's start index (and the end of the current one) */
	private int		m_nextstart;

	/** T if the current fragment ends in / */
	private boolean	m_endWithSlash;

	private String	m_current;

	private String	m_rest;

	private String	m_currest;

	public PathSplitter() {
	}

	public PathSplitter(String path) {
		init(path);
	}

	public void init(String path) {
		m_path = path;
		m_length = path.length();
		m_curstart = 0;
		m_nextstart = 0;
		m_current = null;
		m_rest = null;
		m_currest = null;
		next();
	}

	/**
	 * Return the entire input path.
	 * @return
	 */
	public String getPath() {
		return m_path;
	}

	public boolean next() {
		if(m_nextstart >= m_length) // Exhausted?
			return false;
		m_curstart = m_nextstart; // Make the next thingy the current thingy.
		m_curend = m_nextstart;

		//-- Find a new next
		m_current = null;
		m_rest = null;
		m_currest = null;
		m_endWithSlash = false;
		int ix = m_path.indexOf('/', m_curstart); // Can we find a next /?
		if(ix < 0) {
			//-- We found the last segment.
			m_nextstart = m_length; // No next one
			m_curend = m_length;
			return true; // Done.
		}
		m_curend = ix;
		m_nextstart = ix + 1; // Next starts after slash
		m_endWithSlash = true; // This fragment ends in slash
		return true;
	}

	public String getCurrent() {
		if(m_current == null)
			m_current = m_path.substring(m_curstart, m_curend);
		return m_current;
	}

	public String getCurrentAndRest() {
		if(m_currest == null)
			m_currest = m_path.substring(m_curstart);
		return m_currest;
	}

	public String getRest() {
		if(m_rest == null)
			m_rest = m_path.substring(m_nextstart);
		return m_rest;
	}

	public String getUptoCurrent() {
		return m_path.substring(0, m_curend);
	}

	public boolean isRestEmpty() {
		return m_nextstart >= m_length;
	}

	public boolean endsWithSlash() {
		return m_endWithSlash;
	}

	public boolean isEmpty() {
		return m_curstart >= m_length;
	}

	/**
	 * Compares the current string with the specified thingy.
	 * @param str
	 * @return
	 */
	public boolean isEqual(String str) {
		int sl = str.length();
		if(m_curend - m_curstart != sl)
			return false;
		int rix = m_curend;
		while(--sl >= 0) {
			rix--;
			if(str.charAt(sl) != m_path.charAt(rix))
				return false;
		}
		return true;
	}
}
