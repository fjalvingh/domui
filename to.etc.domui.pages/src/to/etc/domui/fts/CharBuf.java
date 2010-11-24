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
 * A mutable string. This is a reusable buffer, used instead of String, within the text
 * scanners. Because this class is mutable and reusable we prevent shitloads of garbage
 * while handling large amounts of text.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 12, 2008
 */
public class CharBuf implements CharSequence {
	private char[]			m_buf;
	private int				m_end;

	public CharBuf() {
		m_buf = new char[32];
		m_end	= 0;
	}
	public char charAt(int index) {
		return m_buf[index];
	}
	public int length() {
		return m_end;
	}
	public CharSequence subSequence(int start, int end) {
		throw new IllegalStateException("Not impl");
	}
	public char[]	getData() {
		return m_buf;
	}

	public void	toLowerCase() {
		for(int i = m_end; --i >= 0;)
			m_buf[i] = Character.toLowerCase(m_buf[i]);
	}
	public void	toUpperCase() {
		for(int i = m_end; --i >= 0;)
			m_buf[i] = Character.toUpperCase(m_buf[i]);
	}
	@Override
	public boolean equals(Object o) {
		CharSequence s = (CharSequence) o;
		if(s.length() != length())
			return false;
		for(int i = length(); --i >= 0;) {
			if(m_buf[i] != s.charAt(i))
				return false;
		}
		return true;
	}
	@Override
	public int hashCode() {
    	int h = 0;
        for (int i = 0; i < m_end; i++) {
            h = 31*h + m_buf[i++];
        }
        return h;
	}
	public void	setCharAt(int ix, char c) {
		m_buf[ix] = c;
		if(m_end <= ix)
			m_end = ix+1;
	}
	public void	setCharAt(int ix, char[] c) {
		int off = ix;
		for(int i = 0; i < c.length; i++) {
			m_buf[off+i] = c[i];
		}
		if(m_end <= ix+c.length)
			m_end = ix+1+c.length;
	}
	public void	insert(int ix, char c) {
		System.arraycopy(m_buf, ix, m_buf, ix+1, m_end-ix);
		m_buf[ix] = c;
		m_end++;
	}
	public void	insert(int ix, char[] ar) {
		int	cl = ar.length;
		System.arraycopy(m_buf, ix, m_buf, ix+cl, m_end-ix);
		m_end	+= cl;
		System.arraycopy(m_buf, ix, ar, 0, cl);
	}
	public void	deleteCharAt(int ix) {
		if(ix < m_end)
			m_end--;
		System.arraycopy(m_buf, ix+1, m_buf, ix, m_end-ix-1);
	}

	public void	copy(CharSequence s) {
		if(s.length() >= m_buf.length)
			throw new IllegalStateException("Word too long: "+s.length()+" - max="+m_buf.length);
		for(int i = s.length(); --i >= 0;)
			m_buf[i] = s.charAt(i);
		m_end = s.length();
	}
	public void copy(CharBuf b) {
		if(b.length() > m_buf.length)
			throw new RuntimeException("Word string too long");
		System.arraycopy(b.m_buf, 0, m_buf, 0, b.length());
		m_end	= b.length();
	}
	public void	setLength(int ix) {
		if(ix > m_buf.length)
			throw new IllegalStateException("Word too big");
		m_end= ix;
	}
	public boolean	endsWith(CharSequence s) {
		int eix = length();
		if(s.length() > eix)
			return false;
		int six = eix - s.length();
		int cix = 0;
		while(six < eix) {
			if(s.charAt(cix++) != m_buf[six++])
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return new String(m_buf, 0, m_end);
	}
}
