package to.etc.domui.component.lookupform2;

import to.etc.domui.component.lookupform2.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeBase;

/**
 * This is the definition for an Item to look up. A list of these
 * will generate the actual lookup items on the screen, in the order
 * specified by the item definition list.
 *
 * FIXME Should this actually be public??
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jul 31, 2009
 */
public class LookupLine<D> {
	final private ILookupQueryBuilder<D> m_queryBuilder;

	final private IControl<D> m_control;

	final private NodeBase m_label;

	final private D m_defaultValue;

	public IControl<D> getControl() {
		return m_control;
	}

	public D getDefaultValue() {
		return m_defaultValue;
	}

	public ILookupQueryBuilder<D> getQueryBuilder() {
		return m_queryBuilder;
	}

}
