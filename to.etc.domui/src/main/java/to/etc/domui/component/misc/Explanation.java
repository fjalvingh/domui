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

import org.eclipse.jdt.annotation.Nullable;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.XmlTextNode;

/**
 * A block of explanation with a severity: a remark, a warning or an error. It is drawn as a
 * callout - a bar and a marker in the severity's colour on that severity's wash - entirely by
 * the theme (.ui-expl), so it follows the theme variant.
 */
public class Explanation extends Div {
	private MsgType m_type;

	private final XmlTextNode m_text = new XmlTextNode();

	public Explanation(MsgType type, String text) {
		setCssClass("ui-expl ui-" + type.name().toLowerCase());
		m_type = type;
		setText(text);
	}

	public Explanation(final String txt) {
		this(MsgType.INFO, txt);
	}

	@Override
	public void createContent() throws Exception {
		//-- The text gets its own block, so that the html it may contain flows as one paragraph beside the marker.
		Div text = new Div("ui-expl-txt");
		add(text);
		text.add(m_text);
	}

	@Override
	public void setText(final @Nullable String txt) {
		m_text.setText(txt);
	}
}
