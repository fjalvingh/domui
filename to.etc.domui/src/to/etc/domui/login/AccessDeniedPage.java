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
		Img	img	= new Img(AccessDeniedPage.class, "accessDenied.png");
		img.setAlign(ImgAlign.LEFT);
		ep.getContent().add(img);

		Div	d	= new Div();
		String	txt	= DomUtil.BUNDLE.formatMessage("login.access.denied", pageName);
		d.setLiteralText(txt);
		ep.getContent().add(d);

//		//-- Get all rights needed.
//		StringBuilder sb	= new StringBuilder(256);
//		for(int i = 0; i < 99; i++) {
//			String r = getPage().getPageParameters().getString("r"+i, null);
//			if(r == null)
//				break;
//			if(sb.length() > 0)
//				sb.append(", ");
//			String desc = DomApplication.get().getRightsDescription(r);
//			sb.append(desc);
//		}
//		ep.getContent().addLiteral(DomUtil.BUNDLE.formatMessage("login.required.rights", sb.toString()));

		ep.getContent().add(new Div(DomUtil.BUNDLE.formatMessage("login.required.rights")));
		d	= new Div();
		ep.getContent().add(d);
		Ul	ul = new Ul();
		d.add(ul);
		for(int i = 0; i < 99; i++) {
			String r = getPage().getPageParameters().getString("r"+i, null);
			if(r == null)
				break;
			Li	li = new Li();
			ul.add(li);
			String desc = DomApplication.get().getRightsDescription(r);
			li.addLiteral(desc+" ("+r+")");
		}

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
