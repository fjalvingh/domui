package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoBreadCrumb extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add("The DemoApplication has a breadcrumb attached: you can navigate back to the mainpage.");
		cp.add(new VerticalSpacer(20));
		cp.add("The standard look is");
		cp.add(new BreadCrumb());
	}
}
