package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;
import to.etc.domuidemo.pages.*;
import to.etc.domuidemo.pages.overview.input.*;

public class DemoALink extends UrlPage {
	@Override
	public void createContent() throws Exception {
		//-- Simple link
		Div	d	= new Div();
		add(d);
		ATag	al	= new ATag();
		d.add(al);
		al.add(new TextNode("Primitive link where clicked and href are different"));
		al.setHref("http://www.lwn.net/");

		al.setClicked(new IClicked<ATag>() {
			@Override
			public void clicked(ATag b) throws Exception {
				MsgBox.message(DemoALink.this, MsgBox.Type.INFO, "You just clicked...");
			}
		});

		d	= new Div();
		add(d);
		ALink l = new ALink(DemoDateInput.class);
		d.add(l);
		l.setText("Link component moving to another page");

		d	= new Div();
		add(d);
		l = new ALink(MiniPage.class);
		d.add(l);
		l.setText("Popup window link creating a new DomUI WindowSession");
		l.setNewWindowParameters(WindowParameters.createFixed(500, 500, "bleh"));
	}
}
