package to.etc.util;

import java.io.*;
import java.util.*;

/**
 * Contains a (small) set of integers. The set is represented by an
 * array of integers, unsorted.
 * Created on Sep 1, 2003
 * @author jal
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
