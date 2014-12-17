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
package to.etc.domui.component.input;

import to.etc.domui.component.meta.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.trouble.*;
import to.etc.domui.util.*;

import javax.annotation.*;

@Deprecated
abstract public class SelectBasedControl<T> extends Select implements IControl<T>, IHasModifiedIndication {
	private String m_emptyText;

	private T m_currentValue;

	/**
	 * If this combobox has a "unselected" option currently this contains that option. When present it
	 * means that indexes in the <i>combo</i> list are one <i>higher</i> than indexes in the backing
	 * dataset (because this empty option is always choice# 0).
	 */
	private SelectOption m_emptyOption;

	/**
	 * Locate the "T" value for the nth selected option. This must return the ACTUAL list value and must
	 * not decrement the index for mandatoryness (this has already been done).
	 * @param nindex
	 * @return
	 */
	abstract protected T findListValueByIndex(int nindex);

	abstract protected int findOptionIndexForValue(T newvalue);

	public String getEmptyText() {
		return m_emptyText;
	}

	public void setEmptyText(String emptyText) {
		m_emptyText = emptyText;
	}

	/**
	 * If this combobox has a "unselected" option currently this contains that option. When present it
	 * means that indexes in the <i>combo</i> list are one <i>higher</i> than indexes in the backing
	 * dataset (because this empty option is always choice# 0).
	 * @return
	 */
	protected SelectOption getEmptyOption() {
		return m_emptyOption;
	}

	/**
	 * See getter.
	 * @param emptyOption
	 */
	protected void setEmptyOption(SelectOption emptyOption) {
		m_emptyOption = emptyOption;
	}

	/**
	 * The user selected a different option.
	 * @see to.etc.domui.dom.html.Select#internalOnUserInput(int, int)
	 */
	@Override
	protected boolean internalOnUserInput(int oldindex, int nindex) {
		T	newval;

		if(nindex < 0) {
			newval = null; // Should never happen
		} else if(getEmptyOption() != null) {
			//-- We have an "unselected" choice @ index 0. Is that one selected?
			if(nindex <= 0) // Empty value chosen?
				newval = null;
			else {
				nindex--;
				newval = findListValueByIndex(nindex);
			}
		} else {
			newval = findListValueByIndex(nindex);
		}

		ClassMetaModel cmm = newval == null ? null : MetaManager.findClassMeta(newval.getClass());
		if(MetaManager.areObjectsEqual(newval, m_currentValue, cmm))
			return false;
		m_currentValue = newval;
		return true;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl<T> implementation.						*/
	/*--------------------------------------------------------------*/
	/**
	 * @see to.etc.domui.dom.html.IControl#getValue()
	 */
	@Override
	final public T getValue() {
		if(isMandatory() && m_currentValue == null) {
			setMessage(UIMessage.error(Msgs.BUNDLE, Msgs.MANDATORY));
			throw new ValidationException(Msgs.NOT_VALID, "null");
		}
		return m_currentValue;
	}

	/**
	 *
	 * @see to.etc.domui.dom.html.IControl#setValue(java.lang.Object)
	 */
	@Override
	final public void setValue(@Nullable T v) {
		ClassMetaModel cmm = v != null ? MetaManager.findClassMeta(v.getClass()) : null;
		if(MetaManager.areObjectsEqual(v, m_currentValue, cmm))
			return;
		T prev = m_currentValue;
		m_currentValue = v;
		if(!isBuilt())
			return;
		internalOnValueSet(prev, m_currentValue);
	}

	/**
	 * A value was set through setValue(); we need to find the proper thingy to select.
	 */
	final protected void internalOnValueSet(T previousvalue, T newvalue) {
		int ix = findOptionIndexForValue(newvalue);
		setSelectedIndex(ix);
	}

	protected T internalGetCurrentValue() {
		return m_currentValue;
	}

	protected void internalSetCurrentValue(T val) {
		m_currentValue = val;
	}

	/**
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public T getValueSafe() {
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

}
