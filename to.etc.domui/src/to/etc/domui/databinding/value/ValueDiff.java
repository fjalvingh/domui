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
package to.etc.domui.databinding.value;

import javax.annotation.*;

/**
 * The old/new value pair for a change.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 23, 2013
 */
public class ValueDiff<T> {
	@Nullable
	private T m_old;

	@Nullable
	private T m_new;

	public ValueDiff(@Nullable T old, @Nullable T nw) {
		m_old = old;
		m_new = nw;
	}

	@Nullable
	public T getOld() {
		return m_old;
	}

	@Nullable
	public T getNew() {
		return m_new;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		T nw = m_new;
		result = prime * result + ((nw == null) ? 0 : nw.hashCode());
		T old = m_old;
		result = prime * result + ((old == null) ? 0 : old.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ValueDiff< ? > other = (ValueDiff< ? >) obj;
		T nw = m_new;
		if(nw == null) {
			if(other.m_new != null)
				return false;
		} else if(!nw.equals(other.m_new))
			return false;
		T old = m_old;
		if(old == null) {
			if(other.m_old != null)
				return false;
		} else if(!old.equals(other.m_old))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValueDiff [m_old=" + m_old + ", m_new=" + m_new + "]";
	}
}
