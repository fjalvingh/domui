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
package to.etc.domui.component.layout;

import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.errors.IErrorFence;
import to.etc.domui.dom.errors.IErrorMessageListener;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.domui.util.DomUtil;
import to.etc.domui.util.Msgs;

import javax.annotation.Nonnull;

/**
 * This is a CaptionedPanel which captures and displays errors for the tree
 * it is in. This is the default component used by the framework when no
 * other components captures a generated error; in that case the toplevel
 * code catches the error, creates an ErrorPanel component and inserts that
 * as the first node in the page's DOM.
 * This component only changes the look of the CaptionedPanel and adds listeners
 * for the errors.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 17, 2008
 */
public class ErrorPanel extends CaptionedPanel implements IErrorMessageListener {
	private IErrorFence m_errorFence;

	private MsgType m_highestType;

	/**
	 * Create a default error panel.
	 */
	public ErrorPanel() {
		super(Msgs.BUNDLE.getString(Msgs.UI_ERROR_HEADER), new Div());
		setDisplay(DisplayType.NONE);
		getTitleContainer().setCssClass("ui-err-caption");
		getContent().setCssClass("ui-err-cont");
		setCssClass("ui-err-outer");
	}

	/**
	 * When I'm added to a page register myself as an error listener for that page.
	 * @see to.etc.domui.dom.html.NodeBase#onAddedToPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onAddedToPage(Page p) {
		super.onAddedToPage(p);
		m_errorFence = DomUtil.getMessageFence(this);
		m_errorFence.addErrorListener(this);
	}

	/**
	 * When I'm removed from a page I may no longer handle it's errors, so remove
	 * myself from the error listener chain.
	 *
	 * @see to.etc.domui.dom.html.NodeBase#onRemoveFromPage(to.etc.domui.dom.html.Page)
	 */
	@Override
	public void onRemoveFromPage(Page p) {
		super.onRemoveFromPage(p);
		m_errorFence.removeErrorListener(this);
	}

	/**
	 * Adds the new error message to this panel, making it visible.
	 * @see to.etc.domui.dom.errors.IErrorMessageListener#errorMessageAdded(to.etc.domui.dom.errors.UIMessage)
	 */
	@Override
	public void errorMessageAdded(@Nonnull UIMessage m) {
		Div d = new Div();
		d.setUserObject(m);
		//		String text = m.getErrorLocation() != null ? m.getErrorLocation() + ": " + m.getMessage() : m.getMessage();
		DomUtil.renderErrorMessage(d, m);
		getContent().add(d);
		if(getContent().getChildCount() == 1)
			setDisplay(DisplayType.BLOCK);
		NodeBase errorNode = m.getErrorNode();
		if(errorNode != null) {
			errorNode.addCssClass("ui-input-err");
		}
		if(m_highestType == null || m.getType().getOrder() > m_highestType.getOrder()) {
			//-- Update title.
			m_highestType = m.getType();
			updateRepresentation();
		}
	}

	private void updateRepresentation() {
		if(m_highestType == null)
			return;
		switch(m_highestType){
			default:
				throw new IllegalStateException(m_highestType + " - ?");
			case INFO:
				setTitle(Msgs.BUNDLE.getString(Msgs.UI_INFO_HEADER));
				break;

			case WARNING:
				setTitle(Msgs.BUNDLE.getString(Msgs.UI_WARNING_HEADER));
				break;

			case ERROR:
				setTitle(Msgs.BUNDLE.getString(Msgs.UI_ERROR_HEADER));
				break;
		}
	}

	/**
	 * Removes the error message from this panel, rendering it invisible.
	 * @see to.etc.domui.dom.errors.IErrorMessageListener#errorMessageRemoved(to.etc.domui.dom.errors.UIMessage)
	 */
	@Override
	public void errorMessageRemoved(@Nonnull UIMessage m) {
		MsgType highest = null;
		for(int i = getContent().getChildCount(); --i >= 0;) {
			NodeBase b = getContent().getChild(i);
			if(b.getUserObject() == m) {
				//-- Remove this object!
				b.remove();
				if(getContent().getChildCount() == 0)
					setDisplay(DisplayType.NONE);
				NodeBase errorNode = m.getErrorNode();
				if(errorNode != null) {
					errorNode.removeCssClass("ui-input-err");
				}
			} else {
				if(b.getUserObject() instanceof UIMessage) {
					UIMessage uim = (UIMessage) b.getUserObject();
					if(uim == null)
						throw new IllegalStateException("No user message found in userObject");
					if(highest == null)
						highest = uim.getType();
					else if(uim.getType().getOrder() > highest.getOrder())
						highest = uim.getType();
				}
			}
		}
		if(m_highestType != highest) {
			m_highestType = highest;
			updateRepresentation();
		}
	}
}
