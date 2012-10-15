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

import java.util.*;

import javax.annotation.*;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.menu.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

public class ButtonBar extends Table {
	private boolean m_vertical;

	private TD m_center;

	private TBody m_body;

	private List<NodeBase> m_list = new ArrayList<NodeBase>();

	public ButtonBar() {
		setCssClass("ui-buttonbar");
		setCellSpacing("0");
		setCellPadding("0");
		setTableWidth("100%");
	}

	public ButtonBar(boolean vertical) {
		this();
		m_vertical = vertical;
	}

	@Override
	public void createContent() throws Exception {
		m_body = new TBody();
		add(m_body);
		if(m_vertical)
			createVertical();
		else
			createHorizontal();
		for(NodeBase b : m_list)
			appendObject(b);
	}


	private void appendObject(NodeBase b) {
		if(m_vertical)
			appendVertical(b);
		else
			appendHorizontal(b);
	}

	private void appendHorizontal(NodeBase b) {
		m_center.add(b);
	}

	private void appendVertical(NodeBase b) {
		TD td = m_body.addRowAndCell();
		td.add(b);
	}

	/**
	 * For now: just create a row per button; no top- and botton border row.
	 */
	private void createVertical() {
	}

	/**
	 * Create horizontal presentation: 3 cells for border-left, content, border-right
	 */
	private void createHorizontal() {
		m_body.addRow();
		//		TD td = m_body.addCell();	jal 20100222 Buttons do not properly align when this is present.
		//		td.setCssClass("ui-bb-left");

		m_center = m_body.addCell();
		m_center.setCssClass("ui-bb-middle");
		//
		//		td = m_body.addCell();
		//		td.setCssClass("ui-bb-right");
	}

	public void addButton(NodeBase b) {
		m_list.add(b);
		if(isBuilt())
			appendObject(b);
	}

	/**
	 * Removes all buttons. Intended to be used if ButtonBar dinamically changes set of rendered buttons.
	 * @param b
	 */
	public void clearButtons() {
		m_list.clear();
		forceRebuild();
	}


	/**
	 * Add a normal button.
	 * @param txt
	 * @param icon
	 * @param click
	 * @return
	 */
	public DefaultButton addButton(final String txt, final String icon, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, icon, click);
		addButton(b);
		return b;
	}

	public DefaultButton addButton(@Nonnull IUIAction<Void> action) throws Exception {
		DefaultButton b = new DefaultButton(action);
		addButton(b);
		return b;
	}

	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, click);
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton(final String txt, final String icon) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton bxx) throws Exception {
				UIGoto.back();
			}
		});
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton() {
		List<IShelvedEntry> ps = getPage().getConversation().getWindowSession().getShelvedPageStack();
		if(ps.size() > 1) {									// Nothing to go back to (only myself is on page) -> exit
			IShelvedEntry se = ps.get(ps.size() - 2);		// Get the page before me
			if(se.isClose()) {
				return addCloseButton();
			}
		}

		//-- Nothing worked: just add a default back button that will go back to application home if the stack is empty
		return addBackButton(Msgs.BUNDLE.getString("ui.buttonbar.back"), Theme.BTN_CANCEL);
	}

	@Nonnull
	public DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon) {
		DefaultButton b = new DefaultButton(txt, icon, new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				getPage().getBody().closeWindow();
			}
		});
		addButton(b);
		return b;
	}

	@Nonnull
	public DefaultButton addCloseButton() {
		return addCloseButton(Msgs.BUNDLE.getString("ui.buttonbar.close"), Theme.BTN_CLOSE);
	}

	@Nullable
	public DefaultButton addBackButtonConditional() {
		List<IShelvedEntry> ps = getPage().getConversation().getWindowSession().getShelvedPageStack();
		if(ps.size() <= 1)									// Nothing to go back to (only myself is on page) -> exit
			return null;

		IShelvedEntry se = ps.get(ps.size() - 2);			// Get the page before me
		if(se.isClose()) {
			return addCloseButton();
		}
		return addBackButton();
	}

	public DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, msg, click);
		addButton(b);
		return b;
	}

	public DefaultButton addConfirmedButton(final String txt, final String icon, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, icon, msg, click);
		addButton(b);
		return b;
	}

	public LinkButton addLinkButton(final String txt, final String img, final IClicked<LinkButton> click) {
		LinkButton b = new LinkButton(txt, img, click);
		addButton(b);
		return b;
	}

	public LinkButton addConfirmedLinkButton(final String txt, final String img, String msg, final IClicked<LinkButton> click) {
		LinkButton b = MsgBox.areYouSureLinkButton(txt, img, msg, click);
		addButton(b);
		return b;
	}

	public <T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception {
		DefaultButton b = new DefaultButton(instance, action);
		addButton(b);
		return b;
	}
}
