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

import javax.annotation.*;

import to.etc.domui.component.input.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;

/**
 * Represents keyword search panel that is used from other components, like LookupInput.
 * Shows input field, marker that shows found results and waiting image that is hidden by default.
 * Waiting image is passive here, but it is used from browser script.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 21 Jan 2010
 */
public class SearchInput2 extends Div {

	private int m_resultsCount = -1; //-1 states for not visible

	@Nonnull
	final private TextStr m_keySearch = new TextStr();

	@Nullable
	private IValueChanged<SearchInput2> m_onLookupTyping;

	private IValueChanged<SearchInput2> m_onReturn;


//	@Nonnull
//	final private Img m_imgWaiting = new Img("THEME/lui-keyword-wait.gif");
//
	private int m_popupWidth;

	private Div m_pnlSearchPopup;

	public SearchInput2() {
	}

	public SearchInput2(@Nullable String cssClass) {
		if(cssClass != null)
			m_keySearch.setCssClass(cssClass);
	}

	@Override
	public void createContent() throws Exception {
		setCssClass("ui-srip");

//		m_imgWaiting.setCssClass("ui-lui-waiting");
//		m_imgWaiting.setDisplay(DisplayType.NONE);
		if(m_keySearch.getCssClass() == null) {
			m_keySearch.setCssClass("ui-srip-keyword");
		}
		m_keySearch.setMaxLength(40);
		m_keySearch.setSize(14);
		m_keySearch.setMarker();

//		add(m_imgWaiting);
		add(m_keySearch);

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
		return m_keySearch.getValue();
	}

	@Override
	public void setFocus() {
		if(m_keySearch != null) {
			m_keySearch.setFocus();
		}
	}

	/**
	 * Get current window Z index and set its value to current control.
	 */
	private void fixZIndex() {
		//bug fix for IE when combining relative positioning, and overlapping control with absolute positioning.
		FloatingWindow parentFloatingWindow = findParent(FloatingWindow.class);
		int parentWindowZIndex = 0;
		if(parentFloatingWindow != null) {
			parentWindowZIndex = parentFloatingWindow.getZIndex();
		}
		if(parentWindowZIndex < 0) {
			parentWindowZIndex = 0;
		}
		setZIndex(parentWindowZIndex);
	}

	/**
	 * Getter for hint. See {@link SearchInput2#setHint}.
	 * @param hint
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

	public void webActionlookupTyping(IRequestContext ctx) throws Exception {
		IValueChanged<SearchInput2> lookupTyping = getOnLookupTyping();
		if(null != lookupTyping)
			lookupTyping.onValueChanged(this);
	}

	public void webActionlookupTypingDone(IRequestContext ctx) throws Exception {
		IReturnPressed< ? extends NodeBase> returnPressed = getReturnPressed();
		if(null != returnPressed)
			((IReturnPressed<NodeBase>) returnPressed).returnPressed(this);
	}
}
