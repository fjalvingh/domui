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
 * A tokenizing lexer which passes information into LexerToken instances, allowing tokens to
 * be pushed back where needed. This does no token management (responsibility of caller) because
 * that performs way better when using token instances.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 28, 2009
 */
abstract public class TextTokenizerBase extends ReaderScannerBase {
	private LexerToken[]	m_tokenQueue	= new LexerToken[32];

	private int				m_qget, m_qput, m_qlen;

	/**
	 * Handles actual token generation using the reader's globals.
	 * @param t
	 * @return
	 */
	abstract protected int nextTokenPrim() throws Exception;


	public TextTokenizerBase(Object source, Reader r) {
		super(source, r);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Pushback token stream code.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Push the specified token into the token queue. The token becomes OWNED by this - it should not be re-used.
	 * @param t
	 */
	public void pushToken(LexerToken t) {
		if(t == null)
			throw new IllegalArgumentException("token cannot be null");
		if(m_qlen >= m_tokenQueue.length) {
			LexerToken[] ar = new LexerToken[m_tokenQueue.length * 2];
			System.arraycopy(m_tokenQueue, 0, ar, 0, m_tokenQueue.length);
			m_tokenQueue = ar;
		}
		if(m_qput >= m_tokenQueue.length)
			m_qput = 0;
		m_tokenQueue[m_qput++] = t;
		m_qlen++;
	}

	public void pushTokenCopy(LexerToken t) {
		pushToken(t.dup());
	}

	public LexerToken popToken() {
		if(m_qlen <= 0)
			return null;
		if(m_qget >= m_tokenQueue.length)
			m_qget = 0;
		m_qlen--;
		return m_tokenQueue[m_qget++];
	}

	public int tokenStackSize() {
		return m_qlen;
	}

	/**
	 * Checks if the token queue contains tokens, and if so returns the topmost one. If not this
	 * scans input for the next token.
	 * @param t
	 * @return
	 */
	public int nextToken(LexerToken t) throws Exception {
		if(tokenStackSize() > 0) {
			t.assignFrom(popToken());
			return t.getTokenCode();
		}
		startToken();
		int tc = nextTokenPrim();
		t.setTokenCode(tc);
		t.setColumn(getTokenColumn());
		t.setLine(getTokenLine());
		t.setSrc(getSource());
		t.setText(getCopied());
		return tc;
	}
}
