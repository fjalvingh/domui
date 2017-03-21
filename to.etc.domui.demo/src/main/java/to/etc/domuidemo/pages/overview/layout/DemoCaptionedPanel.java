package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoCaptionedPanel extends UrlPage {
	@Override
	public void createContent() throws Exception {
		CaptionedPanel cp = new CaptionedPanel("Captioned panel: title area");
		add(cp);
		cp.getContent().add("This is the content- which can be anything, really");

		add(new VerticalSpacer(40));

		//-- We can also add anything inside the title.
		NodeContainer d = new Div();
		d.add("Hello world");
		d.add(new Img("img/btnSmileyWink.png"));
		cp = new CaptionedPanel(d);
		add(cp);
		cp.getContent().add("Bla bla");
	}
}
