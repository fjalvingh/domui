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
import java.util.*;

/**
 * Contains a (small) set of integers. The set is represented by an
 * array of integers, unsorted.
 * Created on Sep 1, 2003
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class SmallIntSet implements Serializable {
	private int[]	m_data;

	private int		m_count;

	public SmallIntSet() {
	}

	public SmallIntSet(int size) {
		m_data = new int[size];
	}

	public boolean contains(int val) {
		for(int i = m_count; --i >= 0;) {
			if(m_data[i] == val)
				return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof SmallIntSet))
			return false;
		SmallIntSet o = (SmallIntSet) other;
		if(o.size() != size())
			return false;
		if(size() == 0)
			return true; // Both are empty

		sort();
		o.sort();
		for(int i = m_count; --i >= 0;) {
			if(m_data[i] != o.m_data[i])
				return false;
		}
		return true;
	}

	private void sort() {
		Arrays.sort(m_data, 0, size());
	}


	/**
	 * Add a new member to the set.
	 * @param i
	 */
	public void add(int i) {
		if(contains(i))
			return;
		if(m_data == null || m_data.length <= m_count) // Must grow?
		{
			if(m_data == null)
				m_data = new int[2];
			else {
				int[] ar = new int[m_count + 2];
				System.arraycopy(m_data, 0, ar, 0, m_count);
				m_data = ar;
			}
		}
		m_data[m_count++] = i;
	}

	public void clear() {
		m_count = 0;
	}

	public int size() {
		return m_count;
	}

	public int elementAt(int i) {
		if(i >= m_count)
			throw new IllegalStateException("Index " + i + " is larger than count " + m_count);
		return m_data[i];
	}

	@Override
	public String toString() {
		if(size() == 0)
			return "[]";
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for(int i = size(); --i >= 0;) {
			sb.append(elementAt(i));
			if(i != 0)
				sb.append(',');
		}
		sb.append(']');
		return sb.toString();
	}

}
