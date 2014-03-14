package to.etc.util;

import java.util.*;

import javax.annotation.*;

/**
 * Sigh.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class ArrayIterator<T> implements Iterator<T> {
	@Nonnull
	final private T[]	m_array;

	private int			m_index	= 0;

	public ArrayIterator(@Nonnull T anArray[]) {
		m_array = anArray;
	}

	@Override
	public boolean hasNext() {
		return m_index < m_array.length;
	}

	@Override
	public T next() throws NoSuchElementException {
		if(hasNext())
			return m_array[m_index++];
		else
			throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
