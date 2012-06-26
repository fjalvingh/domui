package to.etc.domuidemo.pages.overview.htmleditor;

import to.etc.domui.component.htmleditor.*;
import to.etc.domui.dom.html.*;

public class DemoHtmlEditor extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		HtmlEditor he = new HtmlEditor();
		he.setWidth("400px");
		he.setText("Some sample text");
		d.add(he);
	}
}
