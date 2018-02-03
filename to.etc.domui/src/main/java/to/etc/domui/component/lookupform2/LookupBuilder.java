package to.etc.domui.component.lookupform2;

import to.etc.domui.component.lookupform2.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-12-17.
 */
final public class LookupBuilder<T> {
	@Nonnull
	final private LookupForm2<T> m_form;

	@Nullable
	private IControl<?> m_control;

	@Nullable
	private ILookupQueryBuilder<?> m_queryBuilder;

	@Nullable
	private PropertyMetaModel<?> m_property;

	private boolean m_ignoreCase = true;

	@Nullable
	private String m_lookupHint;

	private int m_minLength;

	@Nullable
	private NodeContainer m_labelNode;

	@Nullable
	private String m_labelText;

	@Nullable
	private String m_testId;

	@Nullable
	private Object m_defaultValue;

	private boolean m_popupSearchImmediately;

	private boolean m_popupInitiallyCollapsed;

	LookupBuilder(LookupForm2<T> form) {
		m_form = form;
	}

	public <D> LookupBuilder<T> defaultValue(D value) {
		m_defaultValue = value;
		return this;
	}

	@Nullable public PropertyMetaModel<?> getProperty() {
		return m_property;
	}

	@Nullable public <D> ILookupQueryBuilder<D> getQueryBuilder() {
		return (ILookupQueryBuilder<D>) m_queryBuilder;
	}

	@Nullable public <D> D getDefaultValue() {
		return (D) m_defaultValue;
	}

	@Nullable public NodeContainer getLabelNode() {
		return m_labelNode;
	}

	@Nullable public String getLabelText() {
		return m_labelText;
	}

	@Nullable public IControl<?> getControl() {
		return m_control;
	}

	public LookupBuilder<T> property(String property) {
		m_property = m_form.getMetaModel().getProperty(property);
		return this;
	}

	public LookupBuilder<T> minLength(int len) {
		m_minLength = len;
		return this;
	}

	public int getMinLength() {
		return m_minLength;
	}

	public LookupBuilder<T> hint(String lookupHint) {
		m_lookupHint = lookupHint;
		return this;
	}

	public String getLookupHint() {
		return m_lookupHint;
	}

	public LookupBuilder<T> ignoreCase(boolean yes) {
		m_ignoreCase = yes;
		return this;
	}

	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	public LookupBuilder<T> searchImmediately(boolean yes) {
		m_popupSearchImmediately = true;
		return this;
	}

	public boolean isInitiallyCollapsed() {
		return m_popupInitiallyCollapsed;
	}

	public boolean isSearchImmediately() {
		return m_popupSearchImmediately;
	}

	public LookupBuilder<T> initiallyCollapsed(boolean yes) {
		m_popupInitiallyCollapsed = yes;
		return this;
	}

	public LookupBuilder<T> label(String label) {
		m_labelText = label;
		return this;
	}
	public LookupBuilder<T> label(Label label) {
		m_labelNode = label;
		return this;
	}

	/**
	 * Finish the builder and return the result. This version allows adding a control, but assumes that
	 * the query can be build by a simple equals on the property value.
	 */
	public <D> LookupLine<D> control(IControl<D> control) {
		m_control = control;
		return m_form.finishBuilder(this);
	}

	/**
	 * Finish the builder and return the result. This version allows any query to be constructed from the
	 * control's value.
	 */
	public <D> LookupLine<D> control(ILookupQueryBuilder<D> builder, IControl<D> control) {
		m_control = control;
		m_queryBuilder = builder;
		return m_form.finishBuilder(this);
	}

	public LookupLine<?> control() {
		return m_form.finishBuilder(this);
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();

		IControl<?> control = m_control;
		if(control != null)
			sb.append(" control=").append(control);
		PropertyMetaModel<?> property = m_property;
		if(null != property)
			sb.append(" property=").append(property);
		if(sb.length() == 0)
			return super.toString();
		return sb.toString();
	}
}
