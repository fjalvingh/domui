package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.UrlPage;

public class DemoCaption extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new Caption("Caption component"));

		add(new VerticalSpacer(30));

		//-- It can have buttons too
		Caption ch = new Caption("With buttons");
		add(ch);
		ch.addButton(Icon.of("img/btnSmileyWink.png"), "Press me to smile", clickednode -> MsgBox.message(DemoCaption.this, MsgBox.Type.INFO, "Button pressed"));
	}
}
