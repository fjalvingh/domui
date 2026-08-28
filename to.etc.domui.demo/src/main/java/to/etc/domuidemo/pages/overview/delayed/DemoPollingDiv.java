package to.etc.domuidemo.pages.overview.delayed;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.*;

public class DemoPollingDiv extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new SillyClock());
	}
}
