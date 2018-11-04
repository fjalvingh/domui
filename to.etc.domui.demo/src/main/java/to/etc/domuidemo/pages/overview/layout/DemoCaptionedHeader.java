package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.CaptionedHeader;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.UrlPage;

public class DemoCaptionedHeader extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new CaptionedHeader("Captioned header"));

		add(new VerticalSpacer(30));

		//-- It can have buttons too
		CaptionedHeader ch = new CaptionedHeader("With buttons");
		add(ch);
		ch.addButton(Icon.of("img/btnSmileyWink.png"), "Press me to smile", clickednode -> MsgBox.message(DemoCaptionedHeader.this, MsgBox.Type.INFO, "Button pressed"));
	}
}
