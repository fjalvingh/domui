package to.etc.domui.dom.html;

import java.util.*;

import to.etc.domui.component.input.*;
import to.etc.domui.util.*;

public class Checkbox extends NodeBase implements IInputNode<Boolean>, IHasModifiedIndication {

	private boolean m_checked;

	private boolean m_disabled;

	/**
	 * T when this input value is a REQUIRED value.
	 */
	private boolean m_mandatory;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	private IValueChanged< ? > m_onValueChanged;

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

	/**
	 * Checkboxes cannot be readonly; we make them disabled instead.
	 * @see to.etc.domui.dom.html.IInputNode#isReadOnly()
	 */
	public boolean isReadOnly() {
		return isDisabled();
	}

	public void setReadOnly(boolean readOnly) {
		setDisabled(readOnly);
		//		if(readOnly)
		//			addCssClass("ui-ro");
		//		else
		//			removeCssClass("ui-ro");
	}

	@Override
	public boolean acceptRequestParameter(String[] values) {
		if(values == null || values.length != 1)
			throw new IllegalStateException("Checkbox: expecting a single input value, not " + Arrays.toString(values));
		String s = values[0].trim();

		boolean on = "y".equalsIgnoreCase(s);
		if(m_checked == on)
			return false; // Unchanged

		DomUtil.setModifiedFlag(this);
		m_checked = on;
		return true; // Value changed
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValue()
	 */
	public Boolean getValue() {
		return new Boolean(isChecked());
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#setValue(java.lang.Object)
	 */
	public void setValue(Boolean v) {
		setChecked((v == null) ? false : v.booleanValue());
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#getValueSafe()
	 */
	@Override
	public Boolean getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IInputNode#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}


	public boolean isMandatory() {
		return m_mandatory;
	}

	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;

	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		m_onValueChanged = onValueChanged;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IHasModifiedIndication impl							*/
	/*--------------------------------------------------------------*/
	/**
	 * Returns the modified-by-user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#isModified()
	 */
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	public void setModified(boolean as) {
		m_modifiedByUser = as;
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
