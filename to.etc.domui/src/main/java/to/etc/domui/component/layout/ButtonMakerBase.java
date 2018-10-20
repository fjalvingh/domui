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

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.misc.IIcon;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.state.UIGoto;
import to.etc.domui.themes.Theme;

abstract public class ButtonMakerBase {
	abstract protected void addButton(NodeBase b);

	/**
	 * Add a normal button.
	 */
	public DefaultButton addButton(final String txt, final IIcon icon, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, icon, click);
		addButton(b);
		return b;
	}

	public DefaultButton addButton(final String txt, final IClicked<DefaultButton> click) {
		DefaultButton b = new DefaultButton(txt, click);
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton(final String txt, final IIcon icon) {
		DefaultButton b = new DefaultButton(txt, icon, bxx -> UIGoto.back());
		addButton(b);
		return b;
	}

	public DefaultButton addBackButton() {
		return addBackButton("Terug", Icon.of(Theme.BTN_CANCEL));
	}

	public DefaultButton addConfirmedButton(final String txt, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, msg, click);
		addButton(b);
		return b;
	}

	public DefaultButton addConfirmedButton(final String txt, final IIcon icon, final String msg, final IClicked<DefaultButton> click) {
		DefaultButton b = MsgBox.areYouSureButton(txt, icon, msg, click);
		addButton(b);
		return b;
	}

	public LinkButton addLinkButton(String txt, IIcon img, IClicked<LinkButton> click) {
		LinkButton b = new LinkButton(txt, img, click);
		addButton(b);
		return b;
	}

	public LinkButton addConfirmedLinkButton(String txt, IIcon img, String msg, final IClicked<LinkButton> click) {
		LinkButton b = MsgBox.areYouSureLinkButton(txt, img, msg, click);
		addButton(b);
		return b;
	}
}
