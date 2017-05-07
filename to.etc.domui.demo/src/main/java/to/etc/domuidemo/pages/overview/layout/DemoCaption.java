package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoCaption extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new Caption("Caption component"));

		add(new VerticalSpacer(30));

		//-- It can have buttons too
		Caption ch = new Caption("With buttons");
		add(ch);
		ch.addButton("img/btnSmileyWink.png", "Press me to smile", new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				MsgBox.message(DemoCaption.this, MsgBox.Type.INFO, "Button pressed");
			}
		});
	}
}
