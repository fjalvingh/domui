package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.FaIcon;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.MsgBox.Type;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domuidemo.components.CenterPanel;

public class DemoDefaultButton extends UrlPage {

	@Override
	public void createContent() throws Exception {
		CenterPanel cp = new CenterPanel();
		add(cp);

		cp.add("This is the default button, called DefaultButton: ");
		DefaultButton db = new DefaultButton("DefaultButton");
		cp.add(db);
		db.setClicked(clickednode -> MsgBox.message(this, Type.INFO, "Button pressed"));
		cp.add(" Click it to see it work.");

		cp.add(new VerticalSpacer(40));
		cp.add("You can easily add an icon and an accelerator to a button: ");
		DefaultButton db2 = new DefaultButton("N!ice", "img/btnSmileyWink.png", n -> MsgBox.message(this, Type.WARNING,
			"you can also use the accelerator [I] to activate the button.<br/><br/>"
			+ "(the Message Box uses also an accelerator, C)"));
		cp.add(db2);

		cp.add(new VerticalSpacer(40));
		cp.add("FontAwesome is supported (as is any other font based icon set): ");
		cp.add(new DefaultButton("FontAwesome", FaIcon.faHeart, n -> MsgBox.message(this, Type.WARNING,
			"you can also use the accelerator [I] to activate the button.<br/><br/>"
				+ "(the Message Box uses also an accelerator, C)")));


		cp.add(new VerticalSpacer(40));
		cp.add("Like all Action Items, buttons can be disabled: ");
		DefaultButton di = new DefaultButton("click me to disable me", b -> b.setDisabled(true));
		cp.add(di);
	}
}
