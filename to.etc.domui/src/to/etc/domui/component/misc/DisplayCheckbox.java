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
package to.etc.domui.component.misc;

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Display-only checkbox which renders better than a disabled checkbox thingy.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2010
 */
public class DisplayCheckbox extends Img implements IDisplayControl<Boolean>, IBindable {
	private Boolean m_value;

	public DisplayCheckbox() {
		setCssClass("ui-dspcb");
		setSrc("THEME/dspcb-off.png");
	}

	/**
	 *
	 * @see to.etc.domui.dom.html.IDisplayControl#getValue()
	 */
	@Override
	public Boolean getValue() {
		return m_value;
	}

	@Override
	public void setValue(Boolean v) {
		if(DomUtil.isEqual(v, m_value))
			return;
		m_value = v;
		if(v == null)
			setSrc("THEME/dspcb-off.png");
		else if(v.booleanValue())
			setSrc("THEME/dspcb-on.png");
		else
			setSrc("THEME/dspcb-off.png");
	}

	public void setChecked(boolean on) {
		setValue(Boolean.valueOf(on));
	}

	public boolean isChecked() {
		return getValue() != null && getValue().booleanValue();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/
	/** When this is bound this contains the binder instance handling the binding. */
	@Nullable
	private DisplayOnlyBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	@Nonnull
	public IBinder bind() {
		if(m_binder == null)
			m_binder = new DisplayOnlyBinder(this);
		return m_binder;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	public Boolean getValueSafe() {
		return getValue();
	}

	@Override
	public boolean isReadOnly() {
		return true;
	}

	@Override
	public void setReadOnly(boolean ro) {
	}

	@Override
	public boolean isDisabled() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public void setMandatory(boolean ro) {
	}

	@Override
	public void setDisabled(boolean d) {
	}

	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		throw new UnsupportedOperationException("Display control");
	}
}
