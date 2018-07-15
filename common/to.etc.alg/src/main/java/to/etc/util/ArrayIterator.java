package to.etc.util;

import org.eclipse.jdt.annotation.NonNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Sigh.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
public class ArrayIterator<T> implements Iterator<T> {
	@NonNull
	final private T[]	m_array;

	private int			m_index	= 0;

	public ArrayIterator(@NonNull T anArray[]) {
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
