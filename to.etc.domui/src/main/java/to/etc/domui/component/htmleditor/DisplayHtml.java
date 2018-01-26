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
package to.etc.domui.component.htmleditor;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IDisplayControl;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.XmlTextNode;
import to.etc.domui.util.HtmlUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Mini component to display an HTML section.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 16, 2010
 */
public class DisplayHtml extends Div implements IDisplayControl<String> {
	@Nonnull
	final private XmlTextNode m_xtn = new XmlTextNode();

	private boolean m_unchecked;

	public enum Mode {
		BLOCK, INLINE, INLINEBLOCK
	}

	private Mode m_mode = Mode.BLOCK;

	public DisplayHtml() {}

	public DisplayHtml(@Nullable String value) {
		setValue(value);
	}

	@Override
	public void createContent() throws Exception {
		switch(m_mode){
			default:
			case BLOCK:
				addCssClass("ui-dhtml-blk");
				break;
			case INLINE:
				addCssClass("ui-dhtml-inl");
				break;
			case INLINEBLOCK:
				addCssClass("ui-dhtml-ibl");
				break;
		}

		add(m_xtn);
	}

	@Nullable @Override public NodeBase getForTarget() {
		return null;
	}

	@Override
	public String getValue() {
		return m_xtn.getText();
	}

	@Override
	public void setValue(@Nullable String v) {
		if(!m_unchecked)
			v = HtmlUtil.removeUnsafe(v);
		m_xtn.setText(v);
	}

	public boolean isUnchecked() {
		return m_unchecked;
	}

	public void setUnchecked(boolean unchecked) {
		m_unchecked = unchecked;
	}

	public Mode getMode() {
		return m_mode;
	}

	public void setMode(Mode mode) {
		if(mode == m_mode)
			return;
		m_mode = mode;
		forceRebuild();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	IControl implementation.							*/
	/*--------------------------------------------------------------*/
	/**
	 * {@inheritDoc}
	 * @see to.etc.domui.dom.html.IHasChangeListener#getOnValueChanged()
	 */
	@Override
	public IValueChanged< ? > getOnValueChanged() {
		return null;
	}

	@Override
	public void setOnValueChanged(IValueChanged< ? > onValueChanged) {
		throw new UnsupportedOperationException("Display control");
	}

	@Override
	public String getValueSafe() {
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
}
