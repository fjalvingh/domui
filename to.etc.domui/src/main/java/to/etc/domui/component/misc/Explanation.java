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
package to.etc.domui.component.misc;

import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.ImgAlign;
import to.etc.domui.dom.html.XmlTextNode;

import javax.annotation.Nullable;

public class Explanation extends Div {
	private final XmlTextNode m_text = new XmlTextNode();

	public Explanation() {
	}

	public Explanation(final String txt) {
		setCssClass("ui-expl");
		setText(txt);
	}

	@Override
	public void createContent() throws Exception {
		Img i = new Img("THEME/big-info.png");
		i.setAlign(ImgAlign.LEFT);
		add(0, i);
		add(1, m_text);
	}

	@Override
	public void setText(final @Nullable String txt) {
		m_text.setText(txt);
	}
}
