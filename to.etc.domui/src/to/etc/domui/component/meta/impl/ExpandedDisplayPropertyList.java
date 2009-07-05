package to.etc.domui.component.meta.impl;

import java.util.*;

import to.etc.domui.component.meta.*;
import to.etc.domui.util.*;

/**
 * This is a special version of an expanded property, used when
 * the property referred to consists of multiple properties for
 * display (this is the case when the thingy is part of another
 * class). The instance of this class itself describes the
 * property; it's contents (the list of "child" properties) each
 * describe the content of each child.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 28, 2008
 */
public class ExpandedDisplayPropertyList extends ExpandedDisplayProperty {
	private List<ExpandedDisplayProperty> m_children;

	protected ExpandedDisplayPropertyList(DisplayPropertyMetaModel displayMeta, PropertyMetaModel propertyMeta, IValueAccessor< ? > accessor, List<ExpandedDisplayProperty> children) {
		super(displayMeta, propertyMeta, accessor);
		m_children = children;
	}

	public List<ExpandedDisplayProperty> getChildren() {
		return m_children;
	}
}
