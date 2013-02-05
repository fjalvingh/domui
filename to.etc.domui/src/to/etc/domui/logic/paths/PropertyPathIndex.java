package to.etc.domui.logic.paths;

import java.util.*;

import javax.annotation.*;

/**
 * A part of a property path which is an indexed element of a {@link List} property's value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2013
 */
public class PropertyPathIndex<T> implements IPropertyPathElement<T> {
	final private int m_index;

	@Nonnull
	final private T m_instance;

	public PropertyPathIndex(int index, @Nonnull T instance) {
		m_index = index;
		m_instance = instance;
	}

	@Override
	@Nonnull
	public T getInstance() {
		return m_instance;
	}

	@Override
	public void appendPath(@Nonnull StringBuilder sb) {
		sb.append(toString());
	}

	@Override
	public String toString() {
		return "[" + m_index + "]";
	}
}
