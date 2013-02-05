package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;

/**
 * A part of a property path which is an indexed element of a {@link List} property's value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2013
 */
public class PropertyPathIndex implements IPropertyPathElement {
	final private int m_index;

	public PropertyPathIndex(int index) {
		m_index = index;
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
