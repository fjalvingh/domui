package to.etc.domui.component.searchpanel;

import to.etc.domui.component.searchpanel.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.DefaultNonNull;
import javax.annotation.Nullable;

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
@DefaultNonNull
public class SearchControlLine<D> {
	final private ILookupQueryBuilder<D> m_queryBuilder;

	final private IControl<D> m_control;

	@Nullable
	final private NodeContainer m_label;

	@Nullable
	final private D m_defaultValue;

	/** When added by a property name, this refers to that property. */
	@Nullable
	final private PropertyMetaModel<?> m_property;

	private final boolean m_fromMetadata;

	public SearchControlLine(IControl<D> control, ILookupQueryBuilder<D> qb, @Nullable PropertyMetaModel<?> pmm, @Nullable D defaultValue, @Nullable NodeContainer labelNode, boolean fromMetadata) {
		m_control = control;
		m_defaultValue = defaultValue;
		m_queryBuilder = qb;
		m_label = labelNode;
		m_property = pmm;
		m_fromMetadata = fromMetadata;
	}

	@Nullable public PropertyMetaModel<?> getProperty() {
		return m_property;
	}

	public IControl<D> getControl() {
		return m_control;
	}

	@Nullable
	public D getDefaultValue() {
		return m_defaultValue;
	}

	public ILookupQueryBuilder<D> getQueryBuilder() {
		return m_queryBuilder;
	}

	@Nullable
	public NodeContainer getLabel() {
		return m_label;
	}

	public void clear() {
		getControl().setValue(getDefaultValue());
	}

	public boolean isFromMetadata() {
		return m_fromMetadata;
	}
}
