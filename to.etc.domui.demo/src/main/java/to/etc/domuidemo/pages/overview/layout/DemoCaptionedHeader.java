package to.etc.domuidemo.pages.overview.layout;

import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoCaptionedHeader extends UrlPage {
	@Override
	public void createContent() throws Exception {
		add(new CaptionedHeader("Captioned header"));

		add(new VerticalSpacer(30));

		//-- It can have buttons too
		CaptionedHeader ch = new CaptionedHeader("With buttons");
		add(ch);
		ch.addButton("img/btnSmileyWink.png", "Press me to smile", new IClicked<NodeBase>() {
			@Override
			public void clicked(NodeBase clickednode) throws Exception {
				MsgBox.message(DemoCaptionedHeader.this, MsgBox.Type.INFO, "Button pressed");
			}
		});
	}
}
