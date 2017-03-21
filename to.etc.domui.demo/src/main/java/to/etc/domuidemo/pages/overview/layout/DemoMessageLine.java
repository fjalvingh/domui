package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.dom.errors.*;
import to.etc.domui.dom.html.*;

public class DemoMessageLine extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new MessageLine(MsgType.INFO, "Just a <i>simple</i> message line"));
	}

}
