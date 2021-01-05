package to.etc.domui.component.searchpanel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.meta.PropertyMetaModel;
import to.etc.domui.component.searchpanel.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.dom.html.IControl;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.function.IExecute;
import to.etc.webapp.query.QField;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 14-12-17.
 */
final public class SearchItemBuilder<T> {
	@NonNull
	final private SearchPanel<T> m_form;

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

	@Nullable
	private IExecute m_action;

	private Object m_initialValue;

	SearchItemBuilder(SearchPanel<T> form) {
		m_form = form;
	}

	/**
	 * Set the default value for the control to use after a "clear".
	 */
	public <D> SearchItemBuilder<T> defaultValue(D value) {
		m_defaultValue = value;
		return this;
	}

	/**
	 * Sets the one-time initial value for this control, which will be used instead
	 * of the default value the first time the form is shown. After a clear() the
	 * defaultValue() will take precedence.
	 */
	public <D> SearchItemBuilder<T> initialValue(D value) {
		m_initialValue = value;
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

	@Nullable
	public <D> D getInitialValue() {
		return (D) m_initialValue;
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

	/**
	 * Connect the search box to a specified property on the object. Anything else not explicitly
	 * set will take its defaults from this property: the label, the type of control and the query.
	 */
	public SearchItemBuilder<T> property(String property) {
		m_property = m_form.getMetaModel().getProperty(property);
		return this;
	}

	/**
	 * Connect the search box to a specified property on the object. Anything else not explicitly
	 * set will take its defaults from this property: the label, the type of control and the query.
	 */
	public <V> SearchItemBuilder<T> property(QField<T, V> property) {
		m_property = m_form.getMetaModel().getProperty(property);
		return this;
	}

	/**
	 * Set the minimal length that needs to be present in (string) queries for the query to
	 * be valid. It defaults to 0. Any text query with less than minLength letters will be
	 * refused.
	 */
	public SearchItemBuilder<T> minLength(int len) {
		m_minLength = len;
		return this;
	}

	public int getMinLength() {
		return m_minLength;
	}

	/**
	 * Set an optional hint text on the lookup control.
	 */
	public SearchItemBuilder<T> hint(String lookupHint) {
		m_lookupHint = lookupHint;
		return this;
	}

	public String getLookupHint() {
		return m_lookupHint;
	}

	/**
	 * Enables/disables case independence. The default is to search case independently.
	 */
	public SearchItemBuilder<T> ignoreCase(boolean yes) {
		m_ignoreCase = yes;
		return this;
	}

	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	/**
	 * When the control to add is some kind of lookup, this allows that lookup's form
	 * to show results immediately instead of waiting for the user to press the
	 * search button on that form.
	 */
	public SearchItemBuilder<T> searchImmediately(boolean yes) {
		m_popupSearchImmediately = true;
		return this;
	}

	public boolean isInitiallyCollapsed() {
		return m_popupInitiallyCollapsed;
	}

	public boolean isSearchImmediately() {
		return m_popupSearchImmediately;
	}

	/**
	 * When set this defines the search panel of a lookup as initially collapsed. This is usually
	 * used in combination with searchImmediately to show a list-of-values immediately without
	 * a need for searching.
	 */
	public SearchItemBuilder<T> initiallyCollapsed(boolean yes) {
		m_popupInitiallyCollapsed = yes;
		return this;
	}

	/**
	 * Set the label for this lookup control.
	 */
	public SearchItemBuilder<T> label(String label) {
		m_labelText = label;
		return this;
	}

	/**
	 * Set the label for this lookup control.
	 */
	public SearchItemBuilder<T> label(Label label) {
		m_labelNode = label;
		return this;
	}
	public SearchItemBuilder<T> testID(String id) {
		m_testId = id;
		return this;
	}

	@Nullable public String getTestId() {
		return m_testId;
	}

	/**
	 * Finish the builder and return the result. This version allows adding a control, but assumes that
	 * the query can be build by a simple equals on the property value.
	 */
	public <D> SearchControlLine<D> control(IControl<D> control) {
		m_control = control;
		return m_form.finishBuilder(this);
	}

	/**
	 * Finish the builder and return the result. This version allows any query to be constructed from the
	 * control's value.
	 */
	public <D> SearchControlLine<D> control(IControl<D> control, ILookupQueryBuilder<D> builder) {
		m_control = control;
		m_queryBuilder = builder;
		return m_form.finishBuilder(this);
	}

	public SearchControlLine<?> control() {
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

	public void action(IExecute action) {
		m_action = action;
		m_form.addAction(this, action);
	}

	@Nullable public IExecute getAction() {
		return m_action;
	}
}
