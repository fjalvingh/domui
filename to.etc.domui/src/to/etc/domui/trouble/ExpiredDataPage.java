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
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

public class ExpiredDataPage extends UrlPage {
	public static final String PARAM_ERRMSG = "errorMessage";

	@Override
	public void createContent() throws Exception {
		String msg = getPage().getPageParameters().getString(PARAM_ERRMSG);

		CaptionedPanel ep = new CaptionedPanel(Msgs.BUNDLE.getString(Msgs.DATA_EXPIRED_TITLE));
		add(ep);
		Table t = new Table();
		t.addCssClass("ui-edp-tbl");
		ep.getContent().add(t);
		TBody b = t.addBody();
		TD td = b.addRowAndCell();
		Img img = new Img(Theme.DATA_EXPIRED);
		td.add(img);

		TD co = b.addCell();
		Div d = new Div();

		DomUtil.renderHtmlString(d, msg);

		co.add(d);
		d.setCssClass("ui-acd-ttl");

		//-- Add a link to return to the master/index page.
		if(DomApplication.get().getRootPage() != null) {
			d = new Div();
			co.add(d);
			ALink link = new ALink(DomApplication.get().getRootPage(), MoveMode.NEW); // Destroy shelve.
			d.add(link);
			link.setText(Msgs.BUNDLE.getString(Msgs.LOGIN_TO_INDEX));
		}
	}
}
