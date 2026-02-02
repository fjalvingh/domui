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
package to.etc.lexer;

import java.io.*;

/**
 * A reader which helps with lexical scanning. This contains a variable lookahead text thingy.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 28, 2009
 */
public class TextReaderBase {
	static private final int	MAX_QUEUE_LENGTH	= 1024;

	/** An opague source object used for reporting the source "file" or whatever */
	private Object				m_src;

	private Reader				m_r;

	private boolean				m_eof;

	private int					m_lnr;

	private int					m_cnr;

	/** The round-robin lookahead buffer */
	private int[]				m_la_ar				= new int[MAX_QUEUE_LENGTH];

	private int					m_put_ix;

	private int					m_get_ix;

	private int					m_qlen;

	/** Collection buffer for copy() */
	private StringBuilder		m_collbuf;

	public TextReaderBase(Object source, Reader r) {
		m_r = r;
		m_src = source;
	}

	protected StringBuilder sb() {
		if(m_collbuf == null)
			m_collbuf = new StringBuilder();
		return m_collbuf;
	}

	public String getCopied() {
		if(m_collbuf == null)
			return null;
		return m_collbuf.toString();
	}

	public void clearCopy() {
		if(m_collbuf != null)
			m_collbuf.setLength(0);
	}

	protected void append(char c) {
		sb().append(c);
	}

	public void append(int c) {
		sb().append((char) c);
	}

	public Object getSource() {
		return m_src;
	}

	public int getCurrentLine() {
		return m_lnr;
	}

	public int getCurrentColumn() {
		return m_cnr;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Character read primitives.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Gets the next char from the reader.
	 * @return the next char, or -1 for eof.
	 * @throws IOException
	 */
	private int getc() throws IOException {
		if(m_eof)
			return -1;

		int c = m_r.read();
		if(c == -1)
			m_eof = true;
		//		System.out.println("  getc()="+(int)c);
		return c;
	}

	/**
	 * Adds the character to the lookahead queue at the current PUT position.
	 * @param ch
	 * @return
	 */
	private void putq(int ch) {
		if(m_qlen >= MAX_QUEUE_LENGTH)
			throw new IllegalStateException("Lookahead queue overflow");
		if(m_put_ix >= MAX_QUEUE_LENGTH)
			m_put_ix = 0;
		m_la_ar[m_put_ix++] = ch;
		m_qlen++;
	}

	/**
	 * Returns the "current" character in the queue.
	 * @return
	 */
	final public int LA() throws IOException {
		if(m_qlen == 0)
			putq(getc());
		int c = m_la_ar[m_get_ix];
		//		System.out.println("  LA()="+c+" ("+(char)c+")");
		return c;
	}

	/**
	 * Returns the ixth character for lookahead. ix cannot exceed
	 * the max queue length.
	 * @param ix
	 * @return
	 * @throws IOException
	 */
	final public int LA(int ix) throws IOException {
		while(ix >= m_qlen)
			putq(getc());
		ix = m_get_ix + ix;
		if(ix >= MAX_QUEUE_LENGTH)
			ix -= MAX_QUEUE_LENGTH;
		return m_la_ar[ix];
	}

	/**
	 * Called to advance the character, WITHOUT copying it. Consumes the current character, causing the
	 * next one to become the current one. Accept increments line numbers and column
	 * numbers.
	 * @throws IOException
	 */
	public void accept() {
		if(m_qlen == 0)
			throw new IllegalStateException("accept on empty lookahead queue");
		int ch = m_la_ar[m_get_ix]; // Get current char
		if(ch == '\n') {
			m_lnr++;
			m_cnr = 0;
		} else if(ch != -1) {
			m_cnr++;
		}

		//-- Remove from queue.
		m_qlen--;
		m_get_ix++;
		if(m_get_ix >= MAX_QUEUE_LENGTH)
			m_get_ix = 0;
	}

	/**
	 * Accept ct characters by skipping over them, they are NOT copied.
	 * @param ct
	 */
	public void accept(int ct) {
		while(ct-- > 0)
			accept();
	}

	public void copy() throws IOException {
		int ch = LA();
		sb().append((char) ch);
		accept();
	}

	public void copy(int count) throws IOException {
		while(count-- > 0) {
			int ch = LA();
			sb().append((char) ch);
			accept();
		}
	}
}
