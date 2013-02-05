package to.etc.domui.logic;

import javax.annotation.*;

/**
 * Basic property path element: just a single property.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 5, 2013
 */
public class PropertyPathProperty implements IPropertyPathElement {
	@Nonnull
	final private String m_property;

	public PropertyPathProperty(@Nonnull String property) {
		m_property = property;
	}

	@Nonnull
	public String getProperty() {
		return m_property;
	}

	@Override
	public void appendPath(@Nonnull StringBuilder sb) {
		if(sb.length() > 0)
			sb.append(".");
		sb.append(toString());
	}

	@Nonnull
	@Override
	public String toString() {
		return getProperty();
	}
}
