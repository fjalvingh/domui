package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.layout.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoButtonBar extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		ButtonBar bb = new ButtonBar();
		d.add(bb);
		bb.addButton(new TextNode("Buttonbar (the back button is special):"));

		bb.addButton("Complaint", "img/btnComplaint.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton b) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: Failed");
			}
		});
		bb.addButton("New", "img/btnBaby.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton b) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: New");
			}
		});
		bb.addButton("Retrying", "img/btnReload.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton b) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: Retrying");
			}
		});
		bb.addButton("Completed", "img/btnSmileySmiley.gif", new IClicked<DefaultButton>() {
			@Override
			public void clicked(final DefaultButton b) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Button pressed: Completed");
			}
		});
		bb.addBackButton().setTitle("by default acts as an UIGoto.back()");
	}
}
