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
import to.etc.domui.dom.html.*;

public class ButtonBar extends Table implements IButtonBar, IButtonContainer {
	private boolean m_vertical;

	private TD m_center;

	private TBody m_body;

	@Nullable
	private TD	m_right;

	@Nonnull
	final private ButtonFactory m_factory = new ButtonFactory(this);

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
	 * Create horizontal presentation
	 */
	private void createHorizontal() {
		m_body.addRow();
		m_center = m_body.addCell();
		m_center.setCssClass("ui-bb-middle");
	}

	/**
	 * @see to.etc.domui.component.layout.IButtonBar#addButton(to.etc.domui.dom.html.NodeBase)
	 */
	@Override
	public void addButton(@Nonnull NodeBase b, int order) {
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

	public TD	right() throws Exception {
		build();
		TD right = m_right;
		if(null == right) {
			m_right = right = m_body.addCell();
		}
		return right;
	}


	public TD buttonTD() throws Exception {
		build();
		return m_center;
	}

	@Override
	public @Nonnull DefaultButton addButton(String txt, String icon, IClicked<DefaultButton> click, int order) {
		return m_factory.addButton(txt, icon, click, order);
	}

	@Override
	public @Nonnull DefaultButton addButton(String txt, String icon, IClicked<DefaultButton> click) {
		return m_factory.addButton(txt, icon, click);
	}

	@Override
	public @Nonnull DefaultButton addButton(@Nonnull IUIAction<Void> action, int order) throws Exception {
		return m_factory.addButton(action, order);
	}

	@Override
	public @Nonnull DefaultButton addButton(@Nonnull IUIAction<Void> action) throws Exception {
		return m_factory.addButton(action);
	}

	@Override
	public @Nonnull DefaultButton addButton(String txt, IClicked<DefaultButton> click, int order) {
		return m_factory.addButton(txt, click, order);
	}

	@Override
	public @Nonnull DefaultButton addButton(String txt, IClicked<DefaultButton> click) {
		return m_factory.addButton(txt, click);
	}

	@Override
	public @Nonnull DefaultButton addBackButton(String txt, String icon, int order) {
		return m_factory.addBackButton(txt, icon, order);
	}

	@Override
	public @Nonnull DefaultButton addBackButton(String txt, String icon) {
		return m_factory.addBackButton(txt, icon);
	}

	@Override
	public @Nonnull DefaultButton addBackButton(int order) {
		return m_factory.addBackButton(order);
	}

	@Override
	public @Nonnull DefaultButton addBackButton() {
		return m_factory.addBackButton();
	}

	@Override
	public @Nonnull DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon, int order) {
		return m_factory.addCloseButton(txt, icon, order);
	}

	@Override
	public @Nonnull DefaultButton addCloseButton(@Nonnull String txt, @Nonnull String icon) {
		return m_factory.addCloseButton(txt, icon);
	}

	@Override
	public @Nonnull DefaultButton addCloseButton(int order) {
		return m_factory.addCloseButton(order);
	}

	@Override
	public @Nonnull DefaultButton addCloseButton() {
		return m_factory.addCloseButton();
	}

	@Override
	public DefaultButton addBackButtonConditional(int order) {
		return m_factory.addBackButtonConditional(order);
	}

	@Override
	public DefaultButton addBackButtonConditional() {
		return m_factory.addBackButtonConditional();
	}

	@Override
	public @Nonnull DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click, int order) {
		return m_factory.addConfirmedButton(txt, msg, click, order);
	}

	@Override
	public @Nonnull DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click) {
		return m_factory.addConfirmedButton(txt, msg, click);
	}

	@Override
	public DefaultButton addConfirmedButton(String txt, String icon, String msg, IClicked<DefaultButton> click, int order) {
		return m_factory.addConfirmedButton(txt, icon, msg, click, order);
	}

	@Override
	public @Nonnull DefaultButton addConfirmedButton(String txt, String icon, String msg, IClicked<DefaultButton> click) {
		return m_factory.addConfirmedButton(txt, icon, msg, click);
	}

	@Override
	public @Nonnull LinkButton addLinkButton(String txt, String img, IClicked<LinkButton> click, int order) {
		return m_factory.addLinkButton(txt, img, click, order);
	}

	@Override
	public @Nonnull LinkButton addLinkButton(String txt, String img, IClicked<LinkButton> click) {
		return m_factory.addLinkButton(txt, img, click);
	}

	public LinkButton addConfirmedLinkButton(String txt, String img, String msg, IClicked<LinkButton> click, int order) {
		return m_factory.addConfirmedLinkButton(txt, img, msg, click, order);
	}

	public LinkButton addConfirmedLinkButton(String txt, String img, String msg, IClicked<LinkButton> click) {
		return m_factory.addConfirmedLinkButton(txt, img, msg, click);
	}

	@Override
	public @Nonnull <T> DefaultButton addAction(T instance, IUIAction<T> action, int order) throws Exception {
		return m_factory.addAction(instance, action, order);
	}

	@Override
	public @Nonnull <T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception {
		return m_factory.addAction(instance, action);
	}

	public void addButton(@Nonnull NodeBase b) {
		addButton(b, -1);
	}

	@Nonnull
	public ButtonFactory getButtonFactory() {
		return m_factory;
	}
}
