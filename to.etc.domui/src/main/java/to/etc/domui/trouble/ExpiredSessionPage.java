package to.etc.domui.trouble;

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
import org.eclipse.jdt.annotation.NonNull;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.CaptionedPanel;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.MsgDiv;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.MoveMode;
import to.etc.domui.themes.Theme;
import to.etc.domui.util.Msgs;

public class ExpiredSessionPage extends UrlPage {
	public static final String PARAM_CLOSE = "close";

	@Override
	public void createContent() throws Exception {
		//-- Error message
		CaptionedPanel ep = new CaptionedPanel(Msgs.BUNDLE.getString(Msgs.SESSION_EXPIRED_TITLE));
		add(ep);
		Table t = new Table();
		ep.getContent().add(t);
		t.setWidth("100%");
		TBody b = t.addBody();
		TD td = b.addRowAndCell();
		Img img = new Img(Theme.DATA_EXPIRED);
		td.add(img);
		td.setWidth("1%");

		boolean close = getPage().getPageParameters().getBoolean(PARAM_CLOSE, true);

		String msg = Msgs.BUNDLE.getString(Msgs.SESSION_EXPIRED_MESSAGE);

		TD co = b.addCell();
		String txt = msg;
		Div d = new MsgDiv(txt);
		co.add(d);
		d.setCssClass("ui-acd-ttl");

		//-- Add a link to return to the master/index page.
		if(!close && DomApplication.get().getRootPage() != null) {
			d = new Div();
			co.add(d);
			ALink link = new ALink(DomApplication.get().getRootPage(), MoveMode.NEW); // Destroy shelve.
			d.add(link);
			link.setText(Msgs.BUNDLE.getString(Msgs.LOGIN_TO_INDEX));
		} else {
			d = new Div();
			co.add(d);
			String closeTxt = Msgs.BUNDLE.getString(Msgs.BTN_CLOSE);
			closeTxt = closeTxt.replace("!", ""); //no need for hot key on link
			LinkButton closeBtn = new LinkButton(closeTxt, new IClicked<LinkButton>() {

				@Override
				public void clicked(@NonNull LinkButton clickednode) throws Exception {
					closeWindow();
				}
			});
			d.add(closeBtn);
		}
	}
}

