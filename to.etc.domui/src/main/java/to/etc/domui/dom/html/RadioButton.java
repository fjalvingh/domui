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

import to.etc.domui.util.*;

import javax.annotation.*;
import java.util.*;

/**
 *
 * @author <a href="mailto:jo.seaton@itris.nl">Jo Seaton</a>
 * Created on Aug 20, 2008
 */
public class RadioButton<T> extends NodeBase implements IHasModifiedIndication, IForTarget {
	private RadioGroup<T> m_radioGroup;

	private boolean m_checked;

	private boolean m_disabled;

	private boolean m_readOnly;

	private T m_buttonValue;

	/** Indication if the contents of this thing has been altered by the user. This merely compares any incoming value with the present value and goes "true" when those are not equal. */
	private boolean m_modifiedByUser;

	public RadioButton() {
		super("input");
	}

	public RadioButton(@Nonnull RadioGroup<T> g) {
		super("input");
		m_radioGroup = g;
		g.addButton(this);
	}

	public RadioButton(@Nonnull RadioGroup<T> g, T value) {
		super("input");
		m_radioGroup = g;
		m_buttonValue = value;
		g.addButton(this);
	}

	public RadioButton(T value) {
		super("input");
		m_buttonValue = value;
	}

	@Override
	public void visit(INodeVisitor v) throws Exception {
		v.visitRadioButton(this);
	}

	/**
	 * All buttons must be in a group; all buttons in that group expose a single value.
	 * @param g
	 */
	public void setGroup(@Nonnull RadioGroup<T> g) {
		if(m_radioGroup == g)
			return;
		if(m_radioGroup != null)
			m_radioGroup.removeButton(this);
		m_radioGroup = g;
		g.addButton(this);
		changed();
	}

	public RadioGroup<T> getGroup() {
		if(m_radioGroup == null) {
			m_radioGroup = findParent(RadioGroup.class);
			if(null == m_radioGroup) // Should not happen when properly used.
				throw new IllegalArgumentException("A RadioButton must be part of a RadioGroup");
		}

		return m_radioGroup;
	}

	public String getName() {
		return getGroup().getName();
	}

	public T getButtonValue() {
		return m_buttonValue;
	}

	public void setButtonValue(T selectedValue) {
		m_buttonValue = selectedValue;
	}

	public boolean isChecked() {
		return m_checked;
	}

	void internalSetChecked(boolean on) {
		if(m_checked == on)
			return;
		m_checked = on;
		changed();
	}

	public void setChecked(boolean checked) {
		if(m_checked != checked)
			changed();
		m_checked = checked;
		RadioGroup<T> g = getGroup();
		if(m_checked) {
			//-- This becomes the current group value
			g.internalSetValue(getButtonValue());

			//-- Make sure all other buttons are deselected
			for(RadioButton<T> rb : g.getButtonList()) {
				if(this != rb)
					rb.setChecked(false);
			}
		} else {
			//-- This one was unchecked. If it is the currently selected value too set it to null
			if(g.getValue() == getButtonValue())
				g.internalSetValue(null);
		}
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
	}

	@Override
	public boolean acceptRequestParameter(@Nonnull String[] values) {
		if(isDisabled()) {
			return false;
		}

		if(values == null || values.length != 1)
			throw new IllegalStateException("RadioButton: expecting a single input value, not " + Arrays.toString(values));

		//		System.out.println("Value=" + values[0]);
		String s = values[0].trim();
		boolean on = "y".equalsIgnoreCase(s); // Am I the one selected?
		if(on == m_checked)
			return false; // Unchanged
		DomUtil.setModifiedFlag(this);

		//-- Walk all buttons in the group, and notify them of the change.
		m_checked = on;
		if(!on)
			return true;
		RadioGroup<T> g = getGroup();
		for(RadioButton<T> rb : g.getButtonList())
			rb.selectionChangedTo(this);

		//-- Notify the group of the changed value
		g.internalSetValue(getButtonValue());

		return true;
	}

	@Override
	public void internalOnValueChanged() throws Exception {
		if(m_checked)
			getGroup().internalOnValueChanged(); // Delegate change to group.
	}

	private void selectionChangedTo(RadioButton<T> radioButton) {
//		setChecked(radioButton == this);
		m_checked = radioButton == this;
	}

	@Override
	@Nullable
	public IClickBase< ? > getClicked() {
		IClickBase< ? > clicked = super.getClicked();
		if(null != clicked)
			return clicked;

		final IClicked<RadioGroup<T>> c2 = (IClicked<RadioGroup<T>>) getGroup().getClicked();
		if(c2 != null) {
			return new IClicked<RadioButton<T>>() {
				@Override
				public void clicked(@Nonnull RadioButton<T> clickednode) throws Exception {
					c2.clicked(getGroup());
				}
			};
		}
		return c2;
	}

//	@Override
//	public boolean internalNeedClickHandler() {
//		return getClicked() == null;
//	}
//
//	@Override
//	public String getOnClickJS() {
//		return "WebUI.clicked(this, '" + getActualID() + "', event);return true;";
//	}

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


	@Nullable @Override public NodeBase getForTarget() {
		return this;
	}
}
