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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Span;
import to.etc.domui.util.DomUtil;

/**
 * Display-only radiobutton which renders better than a disabled radiobutton thingy.
 */
@NonNullByDefault
public class DisplayRadiobutton extends Span implements IDisplayControl<Boolean> {

	@Nullable
	private Boolean m_value;

	private final IIconRef m_iconTrue;

	private final IIconRef m_iconFalse;

	private final String m_iconTrueCss;

	private final String m_iconFalseCss;

	public DisplayRadiobutton() {
		this(Icon.faCircleO, "icon", Icon.faDotCircleO, "icon");
	}

	public DisplayRadiobutton(IIconRef iconTrue, String iconTrueCss, IIconRef iconFalse, String iconFalseCss) {
		setCssClass("ui-dsprb");
		m_iconTrue = iconTrue;
		m_iconTrueCss = iconTrueCss;
		m_iconFalse = iconFalse;
		m_iconFalseCss = iconFalseCss;
	}

	/**
	 *
	 * @see IDisplayControl#getValue()
	 */
	@Override
	@Nullable
	public Boolean getValue() {
		return m_value;
	}

	@Override
	public void setValue(@Nullable Boolean v) {
		if(DomUtil.isEqual(v, m_value))
			return;
		m_value = v;
		forceRebuild();
	}

	public void setChecked(boolean on) {
		setValue(Boolean.valueOf(on));
	}

	public boolean isChecked() {
		Boolean value = getValue();
		return value != null && value.booleanValue();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 *
	 * @see to.etc.domui.dom.html.IControl#getValueSafe()
	 */
	@Override
	@Nullable
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
	@Nullable
	public IValueChanged<?> getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(@Nullable IValueChanged<?> onValueChanged) {
		throw new UnsupportedOperationException("Display control");
	}

	@Override public void setHint(@Nullable String hintText) {
		setTitle(hintText);
	}

	@Override
	public void createContent() throws Exception {
		super.createContent();
		boolean isTrue = null != m_value && m_value.booleanValue();
		IIconRef icon = isTrue
			? m_iconTrue
			: m_iconFalse;
		String iconCss = isTrue
			? m_iconTrueCss
			: m_iconFalseCss;
		NodeBase node = icon.createNode(iconCss);
		add(node);
	}

	@Nullable
	@Override
	public NodeBase getForTarget() {
		return null;
	}
}
