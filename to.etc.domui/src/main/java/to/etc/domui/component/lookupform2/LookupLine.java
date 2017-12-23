package to.etc.domui.component.lookupform2;

import to.etc.domui.component.lookupform2.lookupcontrols.ILookupQueryBuilder;
import to.etc.domui.dom.html.IControl;

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
	private ILookupQueryBuilder<D> m_queryBuilder;

	private IControl<D> m_control;

	private String m_labelText;

	private String m_lookupHint;

	private String m_errorLocation;

	private String m_testId;

	private D m_defaultValue;

	private boolean m_ignoreCase = true;

	private int m_minLength;

	private boolean m_popupSearchImmediately;

	private boolean m_popupInitiallyCollapsed;

	public String getLabelText() {
		return m_labelText;
	}

	public void setLabelText(String labelText) {
		m_labelText = labelText;
	}

	public String getErrorLocation() {
		return m_errorLocation;
	}

	public void setErrorLocation(String errorLocation) {
		m_errorLocation = errorLocation;
	}

	public IControl<D> getControl() {
		return m_control;
	}

	public D getDefaultValue() {
		return m_defaultValue;
	}

	public ILookupQueryBuilder<D> getQueryBuilder() {
		return m_queryBuilder;
	}

	public LookupLine<D> minLength(int len) {
		m_minLength = len;
		return this;
	}

	public int getMinLength() {
		return m_minLength;
	}

	public LookupLine<D> hint(String lookupHint) {
		m_lookupHint = lookupHint;
		return this;
	}

	public String getLookupHint() {
		return m_lookupHint;
	}

	public LookupLine<D> ignoreCase(boolean yes) {
		m_ignoreCase = yes;
		return this;
	}

	public boolean isIgnoreCase() {
		return m_ignoreCase;
	}

	public LookupLine<D> searchImmediately(boolean yes) {
		m_popupSearchImmediately = true;
		return this;
	}

	public boolean isInitiallyCollapsed() {
		return m_popupInitiallyCollapsed;
	}

	public boolean isSearchImmediately() {
		return m_popupSearchImmediately;
	}

	public LookupLine<D> initiallyCollapsed(boolean yes) {
		m_popupInitiallyCollapsed = yes;
		return this;
	}
}
