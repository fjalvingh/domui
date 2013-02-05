package to.etc.domui.logic;

import java.util.*;

import javax.annotation.*;
import javax.annotation.concurrent.*;

/**
 * Describes the full path to some value/instance location.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2013
 */
@Immutable
final public class PropertyPath implements Iterable<IPropertyPathElement> {
	@Nonnull
	final private List<IPropertyPathElement> m_path;

	public PropertyPath(@Nonnull List<IPropertyPathElement> path) {
		m_path = Collections.unmodifiableList(new ArrayList<IPropertyPathElement>(path));
	}

	@Override
	@Nonnull
	public Iterator<IPropertyPathElement> iterator() {
		return m_path.iterator();
	}

	@Override
	@Nonnull
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(IPropertyPathElement ppe : this) {
			ppe.appendPath(sb);
		}
		return sb.toString();
	}
}
