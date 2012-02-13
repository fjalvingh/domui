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
package to.etc.domui.login;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

public class AccessDeniedPage extends UrlPage {

	public static final String PARAM_REFUSAL_MSG = "refusalMsg";

	@Override
	public void createContent() throws Exception {
		//-- Can we get the classname?
		String cname = getPage().getPageParameters().getString("targetPage");
		String pageName = "...";
		if(cname != null) {
			//-- Try to load the class to access it's meta
			Class< ? > clz = null;
			try {
				clz = DomApplication.get().loadPageClass(cname);
			} catch(Exception x) {}
			if(clz == null)
				pageName = cname;
			else {
				String s = DomUtil.calcPageTitle(clz);
				if(s == null)
					s = DomUtil.calcPageLabel(clz);
				if(s != null)
					pageName = s;
			}
		}

		CaptionedPanel ep = new CaptionedPanel(Msgs.BUNDLE.getString(Msgs.LOGIN_ACCESS_TITLE));
		add(ep);
		Table t = new Table();
		ep.getContent().add(t);
		t.setWidth("100%");
		TBody b = t.addBody();
		TD td = b.addRowAndCell();
		Img img = new Img("THEME/accessDenied.png");
		td.add(img);
		td.setWidth("1%");

		TD co = b.addCell();
		String txt = Msgs.BUNDLE.formatMessage(Msgs.LOGIN_ACCESS_DENIED, pageName);
		Div d = new Div(txt);
		co.add(d);
		d.setCssClass("ui-acd-ttl");

		if(getPage().getPageParameters().hasParameter(PARAM_REFUSAL_MSG)) {
			co.add(new Div(Msgs.BUNDLE.formatMessage(Msgs.LOGIN_REFUSAL_REASON)));
			d = new Div();
			co.add(d);
			Ul ul = new Ul();
			d.add(ul);
			Li li = new Li();
			ul.add(li);
			li.add(getPage().getPageParameters().getString(PARAM_REFUSAL_MSG));
		} else {
			co.add(new Div(Msgs.BUNDLE.formatMessage(Msgs.LOGIN_REQUIRED_RIGHTS)));
			d = new Div();
			co.add(d);
			Ul ul = new Ul();
			d.add(ul);
			for(int i = 0; i < 99; i++) {
				String r = getPage().getPageParameters().getString("r" + i, null);
				if(r == null)
					break;
				Li li = new Li();
				ul.add(li);
				String desc = DomApplication.get().getRightsDescription(r);
				li.add(desc + " (" + r + ")");
			}
		}

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
