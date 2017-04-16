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
package to.etc.domui.trouble;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

public class DataAccessViolationPage extends UrlPage {
	public static final String PARAM_ERRMSG = "errorMessage";

	@Override
	public void createContent() throws Exception {
		//-- Error message
		String msg = getPage().getPageParameters().getString(PARAM_ERRMSG);

		CaptionedPanel ep = new CaptionedPanel(Msgs.BUNDLE.getString(Msgs.DATA_ACCESS_VIOLATION_TITLE));
		add(ep);
		Table t = new Table();
		t.addCssClass("ui-acd-tbl");
		ep.getContent().add(t);
		TBody b = t.addBody();
		TD td = b.addRowAndCell();
		Img img = new Img(Theme.ACCESS_DENIED);
		td.add(img);

		TD co = b.addCell();
		String txt = msg;
		Div d = new Div("ui-acd-ttl");
		d.setText(txt);
		co.add(d);
	}
}
