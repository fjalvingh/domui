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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.Table;
import to.etc.function.IExecute;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NonNullByDefault
public class ButtonBar extends Table implements IButtonBar, IButtonContainer {
	private boolean m_vertical;

	@Nullable
	private TD m_center;

	@Nullable
	private TBody m_body;

	@Nullable
	private TD m_right;

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
		Objects.requireNonNull(m_center).add(b);
	}

	private void appendVertical(NodeBase b) {
		TD td = Objects.requireNonNull(m_body).addRowAndCell();
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
		Objects.requireNonNull(m_body).addRow();
		TD cell = m_center = Objects.requireNonNull(m_body).addCell();
		cell.setCssClass("ui-bb-middle");
	}

	@Override
	public void addButton(NodeBase b, int order) {
		m_list.add(b);
		if(isBuilt())
			appendObject(b);
	}

	/**
	 * Removes all buttons. Intended to be used if ButtonBar dynamically changes set of rendered buttons.
	 */
	public void clearButtons() {
		m_list.clear();
		forceRebuild();
	}

	public TD right() throws Exception {
		build();
		TD right = m_right;
		if(null == right) {
			m_right = right = Objects.requireNonNull(m_body).addCell();
			right.addCssClass("ui-bb-right");
		}
		return right;
	}


	public TD buttonTD() throws Exception {
		build();
		return Objects.requireNonNull(m_center);
	}

	@Override
	public DefaultButton addButton(String txt, @Nullable IIconRef icon, IExecute click, int order) {
		return m_factory.addButton(txt, icon, click, order);
	}

	@Override
	public DefaultButton addButton(String txt, @Nullable IIconRef icon, IExecute click) {
		return m_factory.addButton(txt, icon, click);
	}

	@Override
	public DefaultButton addButton(IUIAction action, int order) throws Exception {
		return m_factory.addButton(action, order);
	}

	@Override
	public DefaultButton addButton(IUIAction action) throws Exception {
		return m_factory.addButton(action);
	}

	@Override
	public DefaultButton addButton(String txt, IExecute click, int order) {
		return m_factory.addButton(txt, click, order);
	}

	@Override
	public DefaultButton addButton(String txt, IExecute click) {
		return m_factory.addButton(txt, click);
	}

	@Override
	public DefaultButton addBackButton(String txt, IIconRef icon, int order) {
		return m_factory.addBackButton(txt, icon, order);
	}

	@Override
	public DefaultButton addBackButton(String txt, IIconRef icon) {
		return m_factory.addBackButton(txt, icon);
	}

	@Override
	public DefaultButton addBackButton(int order) {
		return m_factory.addBackButton(order);
	}

	@Override
	public DefaultButton addBackButton() {
		return m_factory.addBackButton();
	}

	@Override
	public DefaultButton addCloseButton(String txt, IIconRef icon, int order) {
		return m_factory.addCloseButton(txt, icon, order);
	}

	@Override
	public DefaultButton addCloseButton(String txt, IIconRef icon) {
		return m_factory.addCloseButton(txt, icon);
	}

	@Override
	public DefaultButton addCloseButton(int order) {
		return m_factory.addCloseButton(order);
	}

	@Override
	public DefaultButton addCloseButton() {
		return m_factory.addCloseButton();
	}

	@Override
	@Nullable
	public DefaultButton addBackButtonConditional(int order) {
		return m_factory.addBackButtonConditional(order);
	}

	@Nullable
	@Override
	public DefaultButton addBackButtonConditional() {
		return m_factory.addBackButtonConditional();
	}

	@Override
	public DefaultButton addConfirmedButton(String txt, String msg, IExecute click, int order) {
		return m_factory.addConfirmedButton(txt, msg, click, order);
	}

	@Override
	public DefaultButton addConfirmedButton(String txt, String msg, IExecute click) {
		return m_factory.addConfirmedButton(txt, msg, click);
	}

	@Override
	public DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, IExecute click, int order) {
		return m_factory.addConfirmedButton(txt, icon, msg, click, order);
	}

	@Override
	public DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, IExecute click) {
		return m_factory.addConfirmedButton(txt, icon, msg, click);
	}

	@Override
	public LinkButton addLinkButton(String txt, IIconRef img, IExecute click, int order) {
		return m_factory.addLinkButton(txt, img, click, order);
	}

	@Override
	public LinkButton addLinkButton(String txt, IIconRef img, IExecute click) {
		return m_factory.addLinkButton(txt, img, click);
	}

	public LinkButton addConfirmedLinkButton(String txt, IIconRef img, String msg, IExecute click, int order) {
		return m_factory.addConfirmedLinkButton(txt, img, msg, click, order);
	}

	public LinkButton addConfirmedLinkButton(String txt, IIconRef img, String msg, IExecute click) {
		return m_factory.addConfirmedLinkButton(txt, img, msg, click);
	}

	@Override
	public DefaultButton addAction(IUIAction action, int order) throws Exception {
		return m_factory.addAction(action, order);
	}

	@Override
	public DefaultButton addAction(IUIAction action) throws Exception {
		return m_factory.addAction(action);
	}

	public void addButton(NodeBase b) {
		addButton(b, -1);
	}

	@NonNull
	public ButtonFactory getButtonFactory() {
		return m_factory;
	}
}
