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

import java.util.*;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.css.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

public class BreadCrumb extends Div {
	@Override
	public void createContent() throws Exception {
		setCssClass("ui-brc");
		Table t = new Table();
		add(t);
		//		t.setCssClass("ui-brc");
		t.setCellSpacing("0");
		t.setCellPadding("0");
		t.setTableWidth("100%");
		TBody b = new TBody();
		t.add(b);

		b.addRow();
		TD td = b.addCell();
		td.setCssClass("ui-brc-left");
		TD center = b.addCell();
		center.setCssClass("ui-brc-middle");
		td = b.addCell();
		td.setCssClass("ui-brc-right");

		WindowSession cm = UIContext.getRequestContext().getWindowSession();

		//-- Get the application's main page as the base;
		List<ShelvedEntry> stack = cm.getShelvedPageStack();
		if(stack.size() == 0) {
			setDisplay(DisplayType.NONE);
			return;
		}
		setDisplay(null);

		for(int i = 0; i < stack.size(); i++) {
			boolean last = i + 1 >= stack.size();
			Page p = stack.get(i).getPage();

			if(i > 0) {
				//-- Append the marker,
				Span s = new Span();
				center.add(s);
				s.setCssClass("ui-brc-m");
				s.add(new TextNode(" \u00bb "));
			}

			//-- Create a LINK or a SPAN
			NodeContainer s;
			if(last) {
				s = new Span();
				s.setCssClass("ui-brc-c");
			} else {
				s = new ALink(p.getBody().getClass(), p.getPageParameters());
				s.setCssClass("ui-brc-l");
			}
			center.add(s);
			String bcname = null;
			String bctitle = null;

			if(p.getBody() instanceof IBreadCrumbTitler) {
				bcname = ((IBreadCrumbTitler) p.getBody()).getBreadcrumbName();
				bctitle = ((IBreadCrumbTitler) p.getBody()).getBreadcrumbTitle();
			} else if(!DomUtil.isBlank(p.getBody().getLiteralTitle())) {
				bcname = p.getBody().getLiteralTitle();
			} else {
				bcname = p.getBody().getClass().getName();
				bcname = bcname.substring(bcname.lastIndexOf('.') + 1);
			}
			s.setText(bcname);
			s.setTitle(bctitle);
		}
	}

}
