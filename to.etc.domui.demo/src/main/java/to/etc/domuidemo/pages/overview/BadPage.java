package to.etc.domuidemo.pages.overview;

import to.etc.domui.component.buttons.*;
import to.etc.domui.dom.html.*;

public class BadPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add("Please press ");
		LinkButton lb = new LinkButton("this link", new IClicked<LinkButton>() {
			@Override
			public void clicked(LinkButton clickednode) throws Exception {
				System.out.println("Hello??");
				Div xx = new Div();
				xx.add("new text");
				add(xx);
				//				
				//				MsgBox.info(BadPage.this, "The link was clicked");
			}
		});
		add(lb);
		add(" and see what happens");
	}
}
