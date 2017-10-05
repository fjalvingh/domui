/*
 * DomUI Java User Interface library
 * Copyright (c) 2017 by Frits Jalvingh.
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://github.com/fjalvingh/domui
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */

package to.etc.domuidemo.pages.test.componenterrors;

import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Label;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 5-10-17.
 */
public class Text2LayoutTestPage extends UrlPage {
	@Override public void createContent() throws Exception {
		add(new Caption("Without form builder"));

		//-- Single text
		Div d = new Div();
		add(d);
		Text2<String> t1	= new Text2<>(String.class);
		d.add(new Label(t1,"zzzzzzzz"));
		d.add(t1);
		t1.setValue("zzzzzzzzzzzzzzzzz");

		//-- Single text with button
		add(new VerticalSpacer(10));
		d = new Div();
		add(d);
		Text2<String> t2	= new Text2<>(String.class);
		d.add(new Label(t2,"z22222222"));
		d.add(t2);
		t2.addButton(FaIcon.faAddressBookO, a -> {});
		t2.setValue("zzzzzzzzzzzzzzzzz");

		//-- Single text with 2 buttons
		add(new VerticalSpacer(10));
		d = new Div();
		add(d);
		Text2<String> t3	= new Text2<>(String.class);
		d.add(new Label(t3,"z3333333"));
		d.add(t3);
		t3.addButton(FaIcon.faAddressBookO, a -> {});
		t3.addButton(FaIcon.faAnchor, a -> {});
		t3.setValue("zzzzzzzzzzzzzzzzz");

		add(new VerticalSpacer(20));
		add(new Caption("Form4"));
	}
}
