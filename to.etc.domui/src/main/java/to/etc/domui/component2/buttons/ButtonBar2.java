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
package to.etc.domui.component2.buttons;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.ButtonFactory;
import to.etc.domui.component.layout.IButtonBar;
import to.etc.domui.component.layout.IButtonContainer;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.misc.IIconRef;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ButtonBar2 extends Div implements IButtonBar, IButtonContainer {
	@NonNull
	private Direction m_direction = Direction.HORIZONTAL;

	private Div m_left = new Div("ui-bbar2-l");

	private Div m_right = new Div("ui-bbar2-r");

	@NonNull
	final private ButtonFactory m_factory = new ButtonFactory(this);

	private List<Item> m_list = new ArrayList<>();

	private boolean m_addRight;

	public enum Direction {
		HORIZONTAL, VERTICAL
	}

	public ButtonBar2(@NonNull Direction dir, @Nullable String css) {
		m_direction = dir;
		if(null != css)
			addCssClass(css);
		addCssClass("ui-bbar2 ui-bbar2-" + dir.name().toLowerCase());
	}

	public ButtonBar2() {
		this(Direction.HORIZONTAL, null);
	}

	public ButtonBar2(String css) {
		this(Direction.HORIZONTAL, css);
	}

	public ButtonBar2(@NonNull Direction dir) {
		this(dir, null);
	}

	@Override
	public void createContent() throws Exception {
		m_left.removeAllChildren();
		m_right.removeAllChildren();
		add(m_left);
		add(m_right);
		m_list.sort(Comparator.comparing(Item::getOrder));
		for(Item item : m_list) {
			Div cont = new Div("ui-bbar2-bc");
			cont.add(item.getNode());

			if(item.isRight()) {
				m_right.add(cont);
			} else {
				m_left.add(cont);
			}
		}
	}

	@Override
	public void addButton(@NonNull NodeBase b, int order) {
		m_list.add(new Item(b, order, m_addRight));
		m_addRight = false;
		if(isBuilt())
			forceRebuild();
	}

	/**
	 * Removes all buttons. Intended to be used if ButtonBar dynamically changes set of rendered buttons.
	 */
	public void clearButtons() {
		m_list.clear();
		forceRebuild();
	}

	/**
	 * Removes a button node from button bar and it's internals. Intended to be used if button is dynamically added or removed from the ButtonBar2.
	 */
	public void removeButton(@NonNull NodeBase b) {
		Item item = m_list.stream().filter(it -> it.m_node == b).findFirst().orElse(null);
		b.remove();
		if(null != item) {
			m_list.remove(item);
			if(isBuilt()) {
				forceRebuild();
			}
		}
	}

	public ButtonBar2 right() {
		m_addRight = true;
		return this;
	}

	@Override
	public @NonNull DefaultButton addButton(String txt, IIconRef icon, IClicked<DefaultButton> click, int order) {
		return m_factory.addButton(txt, icon, click, order);
	}

	@Override
	public @NonNull DefaultButton addButton(String txt, IIconRef icon, IClicked<DefaultButton> click) {
		return m_factory.addButton(txt, icon, click);
	}

	@Override
	public @NonNull DefaultButton addButton(@NonNull IUIAction<Void> action, int order) throws Exception {
		return m_factory.addButton(action, order);
	}

	@Override
	public @NonNull DefaultButton addButton(@NonNull IUIAction<Void> action) throws Exception {
		return m_factory.addButton(action);
	}

	@Override
	public @NonNull DefaultButton addButton(String txt, IClicked<DefaultButton> click, int order) {
		return m_factory.addButton(txt, click, order);
	}

	@Override
	public @NonNull DefaultButton addButton(String txt, IClicked<DefaultButton> click) {
		return m_factory.addButton(txt, click);
	}

	@Override
	public @NonNull DefaultButton addBackButton(String txt, IIconRef icon, int order) {
		return m_factory.addBackButton(txt, icon, order);
	}

	@Override
	public @NonNull DefaultButton addBackButton(String txt, IIconRef icon) {
		return m_factory.addBackButton(txt, icon);
	}

	@Override
	public @NonNull DefaultButton addBackButton(int order) {
		return m_factory.addBackButton(order);
	}

	@Override
	public @NonNull DefaultButton addBackButton() {
		return m_factory.addBackButton();
	}

	@Override
	public @NonNull DefaultButton addCloseButton(@NonNull String txt, @NonNull IIconRef icon, int order) {
		return m_factory.addCloseButton(txt, icon, order);
	}

	@Override
	public @NonNull DefaultButton addCloseButton(@NonNull String txt, @NonNull IIconRef icon) {
		return m_factory.addCloseButton(txt, icon);
	}

	@Override
	public @NonNull DefaultButton addCloseButton(int order) {
		return m_factory.addCloseButton(order);
	}

	@Override
	public @NonNull DefaultButton addCloseButton() {
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
	public @NonNull DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click, int order) {
		return m_factory.addConfirmedButton(txt, msg, click, order);
	}

	@Override
	public @NonNull DefaultButton addConfirmedButton(String txt, String msg, IClicked<DefaultButton> click) {
		return m_factory.addConfirmedButton(txt, msg, click);
	}

	@Override
	public DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, IClicked<DefaultButton> click, int order) {
		return m_factory.addConfirmedButton(txt, icon, msg, click, order);
	}

	@Override
	public @NonNull DefaultButton addConfirmedButton(String txt, IIconRef icon, String msg, IClicked<DefaultButton> click) {
		return m_factory.addConfirmedButton(txt, icon, msg, click);
	}

	@Override
	public @NonNull LinkButton addLinkButton(String txt, IIconRef img, IClicked<LinkButton> click, int order) {
		return m_factory.addLinkButton(txt, img, click, order);
	}

	@Override
	public @NonNull LinkButton addLinkButton(String txt, IIconRef img, IClicked<LinkButton> click) {
		return m_factory.addLinkButton(txt, img, click);
	}

	public LinkButton addConfirmedLinkButton(String txt, IIconRef img, String msg, IClicked<LinkButton> click, int order) {
		return m_factory.addConfirmedLinkButton(txt, img, msg, click, order);
	}

	public LinkButton addConfirmedLinkButton(String txt, IIconRef img, String msg, IClicked<LinkButton> click) {
		return m_factory.addConfirmedLinkButton(txt, img, msg, click);
	}

	@Override
	public @NonNull <T> DefaultButton addAction(T instance, IUIAction<T> action, int order) throws Exception {
		return m_factory.addAction(instance, action, order);
	}

	@Override
	public @NonNull <T> DefaultButton addAction(T instance, IUIAction<T> action) throws Exception {
		return m_factory.addAction(instance, action);
	}

	public void addButton(@NonNull NodeBase b) {
		addButton(b, -1);
	}

	@NonNull
	public ButtonFactory getButtonFactory() {
		return m_factory;
	}

	@NonNullByDefault
	private final static class Item {
		private final NodeBase m_node;

		private final int m_order;

		private final boolean m_right;

		public Item(NodeBase node, int order, boolean right) {
			m_node = node;
			m_order = order;
			m_right = right;
		}

		public NodeBase getNode() {
			return m_node;
		}

		public int getOrder() {
			return m_order;
		}

		public boolean isRight() {
			return m_right;
		}
	}
}
