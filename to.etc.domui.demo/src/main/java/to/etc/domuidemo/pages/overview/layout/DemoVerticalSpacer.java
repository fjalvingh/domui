package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoVerticalSpacer extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add("Just an easy way");
		cp.add(new VerticalSpacer(40));
		cp.add("to add some space");
	}
}
