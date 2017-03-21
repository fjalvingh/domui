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

import to.etc.domui.dom.html.*;
import to.etc.domui.util.*;

public class InfoPanel extends Div {
	final private String m_text;

	final private Img m_icon = new Img();

	public InfoPanel(String text) {
		this(text, "THEME/big-info.png");
	}

	public InfoPanel(String text, String icon) {
		m_text = text;
		setIcon(icon);
		setCssClass("ui-ipa");
	}

	@Override
	public void createContent() throws Exception {
		add(m_icon);
		m_icon.setAlign(ImgAlign.LEFT);
		DomUtil.renderHtmlString(this, m_text);
	}

	public void setIcon(String rurl) {
		m_icon.setSrc(rurl);
		forceRebuild();
	}

	public String getIcon() {
		return m_icon.getSrc();
	}
}
