package to.etc.domuidemo.pages.overview.buttons;

import to.etc.domui.component.buttons.*;
import to.etc.domui.component.misc.*;
import to.etc.domui.dom.html.*;

public class DemoNiceButton extends UrlPage {
	@Override
	public void createContent() throws Exception {
		final Div d = new Div();
		add(d);
		d.add(new BR());

		d.add("Please press ");
		NiceButton nb = new NiceButton("Nice");
		nb.setClicked(new IClicked<NiceButton>() {
			@Override
			public void clicked(NiceButton clickednode) throws Exception {
				MsgBox.message(d, MsgBox.Type.INFO, "Nice button clicked");
			}
		});
		d.add(" and see what happens");
		d.add(nb);
	}
}
