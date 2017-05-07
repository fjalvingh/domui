package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoLinkButton extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		add(d);
		d.add(new BR());

		d.add("Please press ");
		LinkButton lb = new LinkButton("this link", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				System.out.println("Hello??");
				MsgBox.info(DemoLinkButton.this, "The link was clicked");
			}
		});
		d.add(lb);
		d.add(" and see what happens");

		//-- Link button with embedded image
		add(new VerticalSpacer(30));

		d = new Div();
		add(d);
		d.add("A link can also contain an image:");
		d.add(" Please press ");
		lb = new LinkButton("image containing link", "img/btnSmileyGrin.gif", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				System.out.println("Hello??");
				MsgBox.info(DemoLinkButton.this, "The link was clicked");
			}
		});
		d.add(lb);
		d.add(" and see what happens");


	}
}
