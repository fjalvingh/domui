package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.title.*;
import to.etc.domui.dom.html.*;

public class DemoAppTitle extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		AppPageTitleBar apt = new AppPageTitleBar("Application title bar", false);
		cp.add(apt);
	}

}
