package to.etc.domuidemo.pages.overview.input;

import to.etc.domui.component.input.*;
import to.etc.domui.dom.html.*;

public class DemoTextStr extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);

		d.add("TextStr");
		TextStr sts = new TextStr();
		sts.setMaxLength(100);
		sts.setSize(30);
		d.add(sts);
		d.add(" (TextStr is short for Text<String> )");
	}
}

