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
package to.etc.domui.component.htmleditor;

import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.util.*;
import to.etc.util.*;

/**
 * This is a small but very fast html editor. It shows way faster than
 * the full Html editor but has a little less options. This uses a slightly
 * adapter version of the <a href="http://code.google.com/p/jwysiwyg/">wysigyg</a>
 * plugin.
 *
 * <p>One oddity in the code here is the handling of the "display" css property. The
 * plugin adds a div just before the original textarea, then it makes the textarea
 * display: none. The textarea is retained so the plugin can put it's content in there
 * still. DomUI however will reset the display:none value after a value is entered
 * because the changeAttribute call sent will clear it (the attribute is BLOCK in
 * the DomUI DOM). To prevent this we set the attribute to BLOCK on a full render and
 * reset it back no none as soon as a partial delta is to be rendered by listening
 * for input on this control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 19, 2010
 */
public class HtmlEditor extends TextArea {
	private String m_styleSheet;

	@Override
	public void createContent() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("$(\"#").append(getActualID()).append("\").wysiwyg({css:");
		String css = getStyleSheet();
		StringTool.strToJavascriptString(sb, css, false);
		sb.append("});");
		appendCreateJS(sb);
		//		appendCreateJS("$(\"#" + getActualID() + "\").wysiwyg({css:'/ui/$themes/blue/style.theme.css'});");
	}

	/**
	 * Contains the in-editor stylesheet to use, which determines the presentation\
	 * of the document inside the editor. If not set it defaults to
	 * THEME/minieditor.css.
	 * @return
	 */
	public String getStyleSheet() throws Exception {
		return DomApplication.get().getThemedResourceRURL(m_styleSheet == null ? "THEME/minieditor.css" : m_styleSheet);
	}

	public void setStyleSheet(String styleSheet) {
		if(DomUtil.isEqual(styleSheet, m_styleSheet))
			return;
		m_styleSheet = styleSheet;
		changed();
	}

	@Override
	public void onBeforeFullRender() throws Exception {
		setDisplay(DisplayType.BLOCK);
	}

	@Override
	public boolean acceptRequestParameter(String[] values) throws Exception {
		setDisplay(DisplayType.NONE);
		for(int i = 0; i < values.length; i++) {
			String s = values[i];
			StringBuilder sb = new StringBuilder();
			try {
				StringTool.entitiesToUnicode(sb, s, true);
				values[i] = sb.toString();
			} catch(Exception e) {
				e.printStackTrace();
				values[i] = e.toString();
			}
		}
		return super.acceptRequestParameter(values);
	}
}
