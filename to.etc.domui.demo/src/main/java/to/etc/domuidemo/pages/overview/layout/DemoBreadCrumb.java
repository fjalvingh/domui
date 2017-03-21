package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoBreadCrumb extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add("The DemoApplication has a breadcrumb attached: you can navigate back to the mainpage.");
		add(new VerticalSpacer(20));
		add("The standard look is");
		add(new BreadCrumb());
	}
}
