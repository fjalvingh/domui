package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;

public class DemoTabPanel extends UrlPage {
	@Override
	public void createContent() throws Exception {
		Div a = new Div();
		a.add("This is content of tab 1");

		Div b = new Div();
		b.add("And this is tab 2");

		TabPanel p = new TabPanel();
		add(p);

		p.add(a, "One");
		p.add(b, "Two");
	}

}
