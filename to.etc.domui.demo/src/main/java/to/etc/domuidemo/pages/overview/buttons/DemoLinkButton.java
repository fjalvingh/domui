package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.IClicked;
import to.etc.domui.dom.html.UrlPage;

public class DemoLinkButton extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div d = new Div();
		add(d);
		d.add(new BR());

		d.add("Please press ");
		LinkButton lb = new LinkButton("this link", (IClicked<LinkButton>) clickednode -> {
			System.out.println("Hello??");
			MsgBox.info(DemoLinkButton.this, "The link was clicked");
		});
		d.add(lb);
		d.add(" and see what happens");

		//-- Link button with embedded image
		add(new VerticalSpacer(30));

		d = new Div();
		add(d);
		d.add("A link can also contain an image:");
		d.add(" Please press ");
		lb = new LinkButton("image containing link", Icon.of("img/btnSmileyGrin.gif"), (IClicked<LinkButton>) clickednode -> {
			System.out.println("Hello??");
			MsgBox.info(DemoLinkButton.this, "The link was clicked");
		});
		d.add(lb);
		d.add(" and see what happens");


	}
}
