package to.etc.domui.login;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domui.server.*;
import to.etc.domui.state.*;
import to.etc.domui.util.*;

public class AccessDeniedPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		//-- Can we get the classname?
		String cname = getPage().getPageParameters().getString("targetPage");
		String pageName	= "...";
		if(cname != null) {
			//-- Try to load the class to access it's meta
			Class<?> clz = null;
			try {
				clz	= DomApplication.get().loadPageClass(cname);
			} catch(Exception x) {
			}
			if(clz == null)
				pageName = cname;
			else {
				pageName = "to be done";
			}
		}

		CaptionedPanel	ep	= new CaptionedPanel(DomUtil.BUNDLE.getString("login.access.title"));
		add(ep);
		Div	d	= new Div();
		String	txt	= DomUtil.BUNDLE.formatMessage("login.access.denied", pageName);
		d.setLiteralText(txt);
		ep.getContent().add(d);

		//-- Add a link to return to the master/index page.
		if(DomApplication.get().getRootPage() != null) {
			d	= new Div();
			ep.getContent().add(d);
			ALink	link	= new ALink(DomApplication.get().getRootPage(), MoveMode.NEW);		// Destroy shelve.
			d.add(link);
			link.setText(DomUtil.BUNDLE, "login.toindex");
		}
	}
}
