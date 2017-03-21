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

import javax.annotation.*;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.themes.*;
import to.etc.domui.util.*;

/**
 * Shows access denied info.
 *
 * @author <a href="mailto:vmijic@execom.eu">Vladimir Mijic</a>
 * Created on 2 Dec 2011
 */
public class AccessDeniedPage extends UrlPage {

	public static final String PARAM_REFUSAL_MSG = "refusalMsg";

	public static final String PARAM_TARGET_PAGE = "targetPage";

	@Override
	public void createContent() throws Exception {
		//-- Can we get the classname?
		String cname = getPage().getPageParameters().getString(PARAM_TARGET_PAGE);
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
		t.addCssClass("ui-acd-tbl");
		ep.getContent().add(t);
		TBody b = t.addBody();
		TD td = b.addRowAndCell();
		Img img = new Img(Theme.ACCESS_DENIED);
		td.add(img);

		TD co = b.addCell();
		String txt = Msgs.BUNDLE.formatMessage(Msgs.LOGIN_ACCESS_DENIED, pageName);
		Div d = new Div("ui-acd-ttl");
		co.add(d);
		d.setText(txt);

		if(getPage().getPageParameters().hasParameter(PARAM_REFUSAL_MSG)) {
			d = new Div();
			co.add(d);
			d.setText(Msgs.BUNDLE.formatMessage(Msgs.LOGIN_REFUSAL_REASON));
			d = new Div();
			co.add(d);
			Ul ul = new Ul();
			d.add(ul);
			Li li = new Li();
			ul.add(li);
			li.add(getPage().getPageParameters().getString(PARAM_REFUSAL_MSG));
		} else {
			d = new Div();
			d.setText(Msgs.BUNDLE.formatMessage(Msgs.LOGIN_REQUIRED_RIGHTS));
			co.add(d);
			d = new Div();
			co.add(d);
			renderMissingRightsInfo(d);
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

	/**
	 * Shows missing right info inside the specified container panel.
	 *
	 * @param d
	 */
	protected void renderMissingRightsInfo(@Nonnull Div d) {
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
}
