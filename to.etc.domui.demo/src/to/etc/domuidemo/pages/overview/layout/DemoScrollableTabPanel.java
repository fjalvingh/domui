package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.html.*;

public class DemoScrollableTabPanel extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ScrollableTabPanel tp = new ScrollableTabPanel();
		add(tp);

		for(int i = 0; i < 20; i++) {
			Div d = new Div();
			d.add("This is the content for tab panel number " + i);
			tp.add(d, "Tab number " + i);
		}
	}
}
