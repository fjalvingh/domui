package to.etc.domui.component.searchpanel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.searchpanel.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.NodeContainer;

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
@NonNullByDefault
public class SearchControlLine<Q, D> {
	final private ILookupQueryBuilder<Q, D> m_queryBuilder;

	final private IControl<D> m_control;

	@Nullable
	final private NodeContainer m_label;

	@Nullable
	final private D m_defaultValue;

	/** This is the default value to use only for the first search. When clear is called the value in defaultValue will be used. */
	@Nullable
	final private D m_initialValue;


	/** When added by a property name, this refers to that property. */
	@Nullable
	final private PropertyMetaModel<?> m_property;

	private final boolean m_fromMetadata;

	public SearchControlLine(IControl<D> control, ILookupQueryBuilder<Q, D> qb, @Nullable PropertyMetaModel<?> pmm, @Nullable D defaultValue, @Nullable D initialValue, @Nullable NodeContainer labelNode, boolean fromMetadata) {
		m_control = control;
		m_defaultValue = defaultValue;
		m_initialValue = initialValue;
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

	@Nullable public D getInitialValue() {
		return m_initialValue;
	}

	public ILookupQueryBuilder<Q, D> getQueryBuilder() {
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
