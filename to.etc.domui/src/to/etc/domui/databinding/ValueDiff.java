package to.etc.domui.databinding;

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
		result = prime * result + ((m_new == null) ? 0 : m_new.hashCode());
		result = prime * result + ((m_old == null) ? 0 : m_old.hashCode());
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
		ValueDiff other = (ValueDiff) obj;
		if(m_new == null) {
			if(other.m_new != null)
				return false;
		} else if(!m_new.equals(other.m_new))
			return false;
		if(m_old == null) {
			if(other.m_old != null)
				return false;
		} else if(!m_old.equals(other.m_old))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValueDiff [m_old=" + m_old + ", m_new=" + m_new + "]";
	}
}
