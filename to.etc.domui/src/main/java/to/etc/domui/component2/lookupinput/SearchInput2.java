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
package to.etc.domui.component2.lookupinput;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IForTarget;
import to.etc.domui.dom.html.IReturnPressed;
import to.etc.domui.dom.html.IValueChanged;
import to.etc.domui.dom.html.Input;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.server.IRequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents keyword search panel that is used from other components, like LookupInput.
 * Shows input field, marker that shows found results and waiting image that is hidden by default.
 * Waiting image is passive here, but it is used from browser script.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 21 Jan 2010
 */
public class SearchInput2 extends Div implements IForTarget {
	@Nonnull
	final private Input m_keySearch = new Input();

	@Nullable
	private IValueChanged<SearchInput2> m_onLookupTyping;

	private int m_popupWidth;

	public SearchInput2() {
	}

	public SearchInput2(@Nullable String cssClass) {
		if(cssClass != null)
			m_keySearch.addCssClass(cssClass);
	}

	@Override
	public void createContent() throws Exception {
		css("ui-lui-srip", "ui-control");
		m_keySearch.setMaxLength(40);
		m_keySearch.setSize(14);
		m_keySearch.setMarker();

		add(m_keySearch);
		m_keySearch.addCssClass("ui-input");

		appendCreateJS("new WebUI.SearchPopup('" + getActualID() + "','" + m_keySearch.getActualID() + "');");
	}

	@Nullable
	public IValueChanged<SearchInput2> getOnLookupTyping() {
		return m_onLookupTyping;
	}

	public void setOnLookupTyping(@Nullable IValueChanged<SearchInput2> onLookupTyping) {
		m_onLookupTyping = onLookupTyping;
	}

	@Nullable
	public String getValue() {
		return m_keySearch.getRawValue();
	}

	@Override
	public void setFocus() {
		m_keySearch.setFocus();
	}

	@Nullable @Override public NodeBase getForTarget() {
		return m_keySearch.getForTarget();
	}

	/**
	 * Getter for hint. See {@link SearchInput2#setHint}.
	 */
	@Nullable
	public String getHint() {
		return m_keySearch.getTitle();
	}

	/**
	 * Set hint to keyword search input. Usually says how search condition is resolved.
	 * @param hint
	 */
	public void setHint(@Nullable String hint) {
		m_keySearch.setTitle(hint);
	}

	public int getPopupWidth() {
		return m_popupWidth;
	}

	public void setPopupWidth(int popupWidth) {
		m_popupWidth = popupWidth;
	}

	/**
	 * Sent regularly whenever the search box is typed in. Causes a ValueChanged event which can then do
	 * whatever lookup is needed.
	 * @param ctx
	 * @throws Exception
	 */
	public void webActionlookupTyping(IRequestContext ctx) throws Exception {
		IValueChanged<SearchInput2> lookupTyping = getOnLookupTyping();
		if(null != lookupTyping)
			lookupTyping.onValueChanged(this);
	}

	/**
	 * Send when return is pressed in the search box. Should finalize the selected value, if
	 * one is present.
	 * @param ctx
	 * @throws Exception
	 */
	public void webActionlookupTypingDone(IRequestContext ctx) throws Exception {
		IReturnPressed< ? extends NodeBase> returnPressed = getReturnPressed();
		if(null != returnPressed)
			((IReturnPressed<NodeBase>) returnPressed).returnPressed(this);
	}
}
