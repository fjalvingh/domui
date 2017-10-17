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

import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.util.DomUtil;

import javax.annotation.Nullable;

/**
 * Display-only checkbox which renders better than a disabled checkbox thingy.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Apr 9, 2010
 */
public class DisplayCheckbox extends Img implements IDisplayControl<Boolean> {
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
	public void setValue(@Nullable Boolean v) {
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
		Boolean value = getValue();
		return value != null && value.booleanValue();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return null;
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
