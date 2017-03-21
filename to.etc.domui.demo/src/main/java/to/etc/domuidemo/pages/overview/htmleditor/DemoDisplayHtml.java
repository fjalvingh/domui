package to.etc.domuidemo.pages.overview.htmleditor;

import to.etc.domui.component.htmleditor.*;
import to.etc.domui.dom.html.*;

public class DemoDisplayHtml extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		DisplayHtml dh = new DisplayHtml();
		dh.setWidth("400px");
		dh.setText("Some sample text");
		//		dh.setMode(Mode.INLINEBLOCK);
		d.add(dh);
	}
}
