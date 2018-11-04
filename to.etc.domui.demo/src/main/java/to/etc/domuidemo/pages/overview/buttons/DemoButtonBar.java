package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.layout.ButtonBar;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.dom.html.BR;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.TextNode;
import to.etc.domui.dom.html.UrlPage;

public class DemoButtonBar extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		ButtonBar bb = new ButtonBar();
		d.add(bb);
		bb.addButton(new TextNode("Buttonbar (the back button is special):"));

		bb.addButton("Complaint", Icon.of("img/btnComplaint.gif"), b -> MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: Failed"));
		bb.addButton("New", Icon.of("img/btnBaby.png"), b -> MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: New"));
		bb.addButton("Retrying", Icon.of("img/btnReload.gif"), b -> MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: Retrying"));
		bb.addButton("Completed", Icon.of("img/btnSmileySmiley.gif"), b -> MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: Completed"));
		bb.addBackButton().setTitle("by default acts as an UIGoto.back()");
	}
}
