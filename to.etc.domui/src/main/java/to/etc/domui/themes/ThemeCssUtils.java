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
package to.etc.domui.themes;

import to.etc.domui.parts.ButtonPartKey;
import to.etc.domui.state.UIContext;
import to.etc.domui.util.js.IScriptScope;

/**
 * This helper class is passed to the theme factory, and can be used to augment
 * information in the style.properties.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Jan 4, 2011
 */
public class ThemeCssUtils {
	static public final CssColor BLACK = new CssColor(0, 0, 0);

	static public final CssColor WHITE = new CssColor(255, 255, 255);

	private IScriptScope m_scope;

	public ThemeCssUtils(IScriptScope ss) {
		m_scope = ss;
	}

	//	private DomApplication m_application;url("to.etc.domui.parts.PropBtnPart.part?src=/Itris_VO02/$THEME/1100000081/1719297387/defaultbutton.properties") no-repeat scroll 0 0 transparent
	//
	//	public ThemeCssUtils(DomApplication domApplication) {
	//		m_application = domApplication;
	//	}

	public CssColor color(String hex) {
		return new CssColor(hex);
	}

	public String url(String in) {
		return UIContext.getRequestContext().getRelativePath(in);
	}

	public String buttonURL(String text) {
		//		String s = "to.etc.domui.parts.PropBtnPart.part?src=$THEME/1100000081/1719297387/defaultbutton.properties&txt=%21Search&icon=$THEME%2f1100000081%2f1719297387%2fbtnFind.png";


		ButtonPartKey k = new ButtonPartKey();
		String s = m_scope.getValue(String.class, "themePath") + "defaultbutton.properties";
		k.setPropFile(s);
		k.setText(text);
		StringBuilder sb = new StringBuilder();
		sb.append("/").append(UIContext.getRequestContext().getRequestResponse().getWebappContext()).append("/");
		k.append(sb);
		return sb.toString();
	}

	public CssColor hsl(double h, double s, double l) {
		return CssColor.createHSL(h, s, l);
	}
}
