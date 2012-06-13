package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoDefaultButton extends UrlPage {

	@Override
	public void createContent() throws Exception {
		final Div d1 = new Div();
		add(d1);
		d1.add(new BR());

		d1.add("Please press ");
		DefaultButton db = new DefaultButton("Defaultbutton");
		d1.add(db);
		db.setClicked(new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.message(d1, MsgBox.Type.INFO, "Button pressed");
			}
		});
		d1.add(" and see what happens.");

		final Div d2 = new Div();
		add(d2);
		d2.add(new VerticalSpacer(40));
		d2.add("An image added to the button. Please press ");
		DefaultButton db2 = new DefaultButton("N!ice", "img/btnSmileyWink.png", new IClicked<DefaultButton>() {
			@Override
			public void clicked(DefaultButton clickednode) throws Exception {
				MsgBox.message(d2, MsgBox.Type.WARNING, "The java-icon is added to the button<br/><br/>" + "you can  aslo use the key-accelerator [i] to activate the button.<br/><br/>"
					+ "(the MsgBox uses also an accelerator C)");
			}
		});
		d2.add(db2);
		d1.add(" and see what happens.");
	}
}
