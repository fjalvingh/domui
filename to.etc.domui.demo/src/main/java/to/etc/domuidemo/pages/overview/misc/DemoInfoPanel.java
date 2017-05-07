package to.etc.domuidemo.pages.overview.misc;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoInfoPanel extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		add(new InfoPanel("You can have some information explained here<BR/>and some more<BR/>and ..."));
	}
}
