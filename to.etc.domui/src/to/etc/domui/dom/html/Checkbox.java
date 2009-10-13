package to.etc.domui.dom.html;

import java.util.*;

import to.etc.domui.component.input.*;

public class Checkbox extends NodeBase implements IInputNode<Boolean> {

	private boolean m_checked;

	private boolean m_disabled;

	private boolean m_readOnly;

	/**
	 * T when this input value is a REQUIRED value.
	 */
	private boolean m_mandatory;

	private IValueChanged< ? , ? > m_onValueChanged;

	public Checkbox() {
		super("input");
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitCheckbox(this);
	}

	public boolean isChecked() {
		return m_checked;
	}

	public void setChecked(boolean checked) {
		if(m_checked != checked)
			changed();
		m_checked = checked;
	}

	public boolean isDisabled() {
		return m_disabled;
	}

	public void setDisabled(boolean disabled) {
		if(m_disabled != disabled)
			changed();
		m_disabled = disabled;
	}

	public boolean isReadOnly() {
		return m_readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if(m_readOnly != readOnly)
			changed();
		m_readOnly = readOnly;
		m_readOnly = readOnly;
		if(readOnly)
			addCssClass("ui-ro");
		else
			removeCssClass("ui-ro");
	}

	@Override
	public void acceptRequestParameter(String[] values) {
		if(values == null || values.length != 1)
			throw new IllegalStateException("Checkbox: expecting a single input value, not " + Arrays.toString(values));
		String s = values[0].trim();
		m_checked = "y".equalsIgnoreCase(s);
	}

	public Boolean getValue() {
		return new Boolean(isChecked());
	}

	public boolean isMandatory() {
		return m_mandatory;
	}

	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;

	}

	public void setValue(Boolean v) {
		setChecked((v == null) ? false : v.booleanValue());
	}

	/**
	 * @see to.etc.domui.dom.html.IInputBase#getOnValueChanged()
	 */
	public IValueChanged< ? , ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IInputBase#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	public void setOnValueChanged(IValueChanged< ? , ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/

	/** When this is bound this contains the binder instance handling the binding. */
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new SimpleBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 *
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}
}
