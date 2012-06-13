package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoVerticalSpacer extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add("Just an easy way");
		add(new VerticalSpacer(40));
		add("to add some space");
	}
}
