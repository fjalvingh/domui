package to.etc.domuidemo.pages.test;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.misc.MsgBox;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.UrlPage;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-04-21.
 */
public class AddRemoveClickHandlerPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		DefaultButton testB = new DefaultButton("Test click");
		add(testB);

		add(new VerticalSpacer(20));

		DefaultButton addB = new DefaultButton("Add handler", a -> {
			testB.setClicked(b -> MsgBox.info(this, "Clicked"));
			testB.addCssClass("addtest");
		});
		add(addB);

		add(new VerticalSpacer(20));
		DefaultButton delB = new DefaultButton("Delete handler", a -> {
			testB.setClicked(null);
			testB.removeCssClass("addtest");
		});
		add(delB);
	}
}
