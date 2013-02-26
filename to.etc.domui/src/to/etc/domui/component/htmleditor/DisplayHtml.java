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

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

/**
 * Mini component to display an HTML section.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Mar 16, 2010
 */
public class DisplayHtml extends Div implements IDisplayControl<String>, IBindable {
	@Nonnull
	final private XmlTextNode m_xtn = new XmlTextNode();

	private boolean m_unchecked;

	public enum Mode {
		BLOCK, INLINE, INLINEBLOCK
	}

	private Mode m_mode = Mode.BLOCK;

	@Override
	public void createContent() throws Exception {
		switch(m_mode){
			default:
			case BLOCK:
				setCssClass("ui-dhtml-blk");
				break;
			case INLINE:
				setCssClass("ui-dhtml-inl");
				break;
			case INLINEBLOCK:
				setCssClass("ui-dhtml-ibl");
				break;
		}

		add(m_xtn);
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

	/*--------------------------------------------------------------*/
	/*	CODING:	IBindable interface (EXPERIMENTAL)					*/
	/*--------------------------------------------------------------*/
	/** When this is bound this contains the binder instance handling the binding. */
	@Nullable
	private SimpleBinder m_binder;

	/**
	 * Return the binder for this control.
	 * @see to.etc.domui.component.input.IBindable#bind()
	 */
	@Override
	@Nonnull
	public IBinder bind() {
		IBinder b = m_binder;
		if(b == null)
			b = m_binder = new SimpleBinder(this);
		return b;
	}

	/**
	 * Returns T if this control is bound to some data value.
	 * @see to.etc.domui.component.input.IBindable#isBound()
	 */
	@Override
	public boolean isBound() {
		return m_binder != null && m_binder.isBound();
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
