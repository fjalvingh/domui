package to.etc.domuidemo.pages.overview.delayed;

import to.etc.domui.dom.html.*;

public class DemoPollingDiv extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new SillyClock());
	}
}
