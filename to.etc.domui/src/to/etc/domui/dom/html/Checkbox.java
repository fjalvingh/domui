/*
 * DomUI Java User Interface library
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.domui.dom.html;

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.util.*;

public class Checkbox extends NodeBase implements IControl<Boolean>, IHasModifiedIndication {
	/** The properties bindable for this component. */
	@Nonnull
	static private final Set<String> BINDABLE_SET = createNameSet("value", "disabled");

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
	@Nonnull
	public Set<String> getBindableProperties() {
		return BINDABLE_SET;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitCheckbox(this);
	}

	public boolean isChecked() {
		return m_checked;
	}

	public void setChecked(boolean checked) {
		if(m_checked == checked)
			return;
		changed();
		m_checked = checked;
		fireModified("value", Boolean.valueOf(!checked), Boolean.valueOf(checked));
	}

	@Override
	public boolean isDisabled() {
		return m_disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		if(m_disabled == disabled)
			return;
		changed();
		m_disabled = disabled;
		fireModified("value", Boolean.valueOf(!disabled), Boolean.valueOf(disabled));
	}

	/**
	 * Checkboxes cannot be readonly; we make them disabled instead.
	 * @see to.etc.domui.dom.html.IControl#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return isDisabled();
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		setDisabled(readOnly);
		//		if(readOnly)
		//			addCssClass("ui-ro");
		//		else
		//			removeCssClass("ui-ro");
	}

	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) {
		if(isDisabled())								// Never accept data from request in disabled control.
			return false;
		if(values == null || values.length != 1)
			throw new IllegalStateException("Checkbox: expecting a single input value, not " + Arrays.toString(values));
		String s = values[0].trim();

		boolean on = "y".equalsIgnoreCase(s);
		if(m_checked == on)
			return false; // Unchanged

		DomUtil.setModifiedFlag(this);
		setChecked(on);
		fireModified("value", Boolean.valueOf(!on), Boolean.valueOf(on));
		return true; // Value changed
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	@Nonnull
	public Boolean getValue() {
		return Boolean.valueOf(isChecked());
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(@Nullable Boolean v) {
		setChecked((v == null) ? false : v.booleanValue());
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public Boolean getValueSafe() {
		return DomUtil.getValueSafe(this);
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#hasError()
	 */
	@Override
	public boolean hasError() {
		getValueSafe();
		return super.hasError();
	}


	@Override
	public boolean isMandatory() {
		return m_mandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		m_mandatory = mandatory;

	}

	/**
	 * Do not use for Checkbox!!! There is a big bug in Internet Explorer where it does not
	 * call onchange for checkboxes. A workaround has been added to DomUI, but it cannot be
	 * made transparant. It is better to use {@link #setClicked(IClicked)} to handle checkbox
	 * change events!
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Deprecated
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return m_onValueChanged;
	}

	/**
	 * Do not use for Checkbox!!! There is a big bug in Internet Explorer where it does not
	 * call onchange for checkboxes. A workaround has been added to DomUI, but it cannot be
	 * made transparant. It is better to use {@link #setClicked(IClicked)} to handle checkbox
	 * change events!
	 * @see to.etc.domui.dom.html.IHasChangeListener#setOnValueChanged(to.etc.domui.dom.html.IValueChanged)
	 */
	@Deprecated
	@Override
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
	@Override
	public boolean isModified() {
		return m_modifiedByUser;
	}

	/**
	 * Set or clear the modified by user flag.
	 * @see to.etc.domui.dom.html.IHasModifiedIndication#setModified(boolean)
	 */
	@Override
	public void setModified(boolean as) {
		m_modifiedByUser = as;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface.								*/
	/*--------------------------------------------------------------*/

	@Nullable
	private List<SimpleBinder> m_bindingList;

	@Override
	public @Nonnull IBinder bind() {
		return bind("value");
	}

	@Override
	@Nonnull
	public IBinder bind(@Nonnull String componentProperty) {
		List<SimpleBinder> list = m_bindingList;
		if(list == null)
			list = m_bindingList = new ArrayList<SimpleBinder>(1);
		SimpleBinder binder = new SimpleBinder(this, componentProperty);
		list.add(binder);
		return binder;
	}

	@Override
	@Nullable
	public List<SimpleBinder> getBindingList() {
		return m_bindingList;
	}
}
