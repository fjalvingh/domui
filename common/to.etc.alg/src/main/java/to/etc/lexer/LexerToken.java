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

/**
 * Base type for lexer tokens. For performance reasons this is a MUTABLE
 * object; users of the tokenizers are supposed to provide instances to be
 * filled.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 28, 2009
 */
public class LexerToken {
	static public final int	T_EOF		= -1;

	static public final int	T_STRING	= -2;

	static public final int	T_NUMBER	= -3;

	static public final int	T_IPADDR	= -4;

	static public final int	T_IDENT		= -5;

	static public final int	T_COMMENT	= -6;

	static public final int	T_BASE_LAST	= -7;

	private Object	m_src;

	private int		m_line;

	private int		m_column;

	private String	m_text;

	/**
	 * When +ve this is a literal character code; when -ve this is a TOKEN code. -1 is EOF by definition.
	 */
	private int		m_tokenCode;

	public Object getSrc() {
		return m_src;
	}

	public void setSrc(Object src) {
		m_src = src;
	}

	public int getLine() {
		return m_line;
	}

	public void setLine(int line) {
		m_line = line;
	}

	public int getColumn() {
		return m_column;
	}

	public void setColumn(int column) {
		m_column = column;
	}

	public String getText() {
		return m_text;
	}

	public void setText(String text) {
		m_text = text;
	}

	public int getTokenCode() {
		return m_tokenCode;
	}

	public void setTokenCode(int tokenCode) {
		m_tokenCode = tokenCode;
	}

	public void assignFrom(LexerToken t) {
		m_column = t.m_column;
		m_line = t.m_line;
		m_src = t.m_src;
		m_text = t.m_text;
		m_tokenCode = t.m_tokenCode;
	}

	public LexerToken dup() {
		try {
			LexerToken t = getClass().newInstance();
			t.assignFrom(this);
			return t;
		} catch(Exception x) {
			throw new RuntimeException(x);
		}
	}
}
