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
package to.etc.domui.fts;

/**
 * A reusable token which is fast.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2008
 */
public class FtsToken implements CharSequence {
	private CharBuf			m_content = new CharBuf();
	private int				m_textOffset;
	private int				m_tokenNumber;
	private int				m_sentenceNumber;

	void	init(CharBuf b, int textoffset, int sentence, int tokennr) {
		m_content = b;
		m_textOffset = textoffset;
		m_tokenNumber = tokennr;
		m_sentenceNumber = sentence;
	}
	void	init(int textoffset, int sentence, int tokennr) {
		m_textOffset = textoffset;
		m_tokenNumber = tokennr;
		m_sentenceNumber = sentence;
	}
	public char charAt(int index) {
		return m_content.charAt(index);
	}
	
	public int length() {
		return m_content.length();
	}
	public CharBuf getContent() {
		return m_content;
	}
	public CharSequence subSequence(int start, int end) {
		throw new IllegalStateException("Not implemented");
	}
	public int getTextOffset() {
		return m_textOffset;
	}
	public int getTokenNumber() {
		return m_tokenNumber;
	}
	public int getSentenceNumber() {
		return m_sentenceNumber;
	}
	public void setTokenNumber(int tokenNumber) {
		m_tokenNumber = tokenNumber;
	}
	public void setSentenceNumber(int sentenceNumber) {
		m_sentenceNumber = sentenceNumber;
	}
	@Override
	public String toString() {
		return m_content.toString();
	}
}
