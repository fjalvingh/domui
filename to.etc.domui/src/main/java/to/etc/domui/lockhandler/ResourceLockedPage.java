package to.etc.domui.lockhandler;

import to.etc.domui.component.layout.CaptionedPanel;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.Img;
import to.etc.domui.dom.html.NodeContainer;
import to.etc.domui.dom.html.TBody;
import to.etc.domui.dom.html.TD;
import to.etc.domui.dom.html.Table;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.server.DomApplication;
import to.etc.domui.state.MoveMode;
import to.etc.domui.util.Msgs;

public class ResourceLockedPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		//-- Error message
		String msg = getPage().getPageParameters().getString("errorMessage");

		CaptionedPanel ep = new CaptionedPanel("Resource is currently locked");
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
		String txt = msg;//Msgs.BUNDLE.formatMessage("expired.data.label", pageName);
		NodeContainer d = new Div().add(txt);
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
