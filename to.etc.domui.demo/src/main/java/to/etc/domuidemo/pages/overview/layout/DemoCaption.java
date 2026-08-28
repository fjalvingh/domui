package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.Caption;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.UrlPage;

public class DemoCaption extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);

		cp.add(new Caption("Caption component"));

		cp.add(new VerticalSpacer(30));

		//-- It can have buttons too
		Caption ch = new Caption("With buttons");
		cp.add(ch);
		ch.addButton(Icon.of("img/btnSmileyWink.png"), "Press me to smile", clickednode -> MsgBox.message(DemoCaption.this, MsgBox.Type.INFO, "Button pressed"));
	}
}
