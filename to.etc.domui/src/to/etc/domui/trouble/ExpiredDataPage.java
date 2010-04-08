package to.etc.domui.trouble;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

public class ExpiredDataPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		//-- Error message
		String msg = getPage().getPageParameters().getString("errorMessage");

		CaptionedPanel ep = new CaptionedPanel(Msgs.BUNDLE.getString("expired.data.title"));
		add(ep);
		Table t = new Table();
		ep.getContent().add(t);
		t.setWidth("100%");
		TBody b = t.addBody();
		TD td = b.addRowAndCell();
		Img img = new Img(ExpiredDataPage.class, "dataExpired.png");
		//		img.setAlign(ImgAlign.LEFT);
		td.add(img);
		td.setWidth("1%");

		TD co = b.addCell();
		String txt = msg;//Msgs.BUNDLE.formatMessage("expired.data.label", pageName);
		Div d = new Div(txt);
		co.add(d);
		d.setCssClass("ui-acd-ttl");

		//-- Add a link to return to the master/index page.
		if(DomApplication.get().getRootPage() != null) {
			d = new Div();
			co.add(d);
			ALink link = new ALink(DomApplication.get().getRootPage(), MoveMode.NEW); // Destroy shelve.
			d.add(link);
			link.setText(Msgs.BUNDLE.getString("login.toindex"));
		}
	}
}
