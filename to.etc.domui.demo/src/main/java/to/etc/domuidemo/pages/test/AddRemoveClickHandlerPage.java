package to.etc.domuidemo.pages.test;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * A click handler can be added to and removed from an already rendered node.
 * The "Test click" button starts without one: adding a handler must make it
 * clickable without a rebuild, removing it must make it inert again.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 01-04-21.
 */
public class AddRemoveClickHandlerPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Adding and removing a click handler"));

		DefaultButton testB = new DefaultButton("Test click");
		testB.setTestID("test");
		cp.add(testB);

		cp.add(new VerticalSpacer(20));

		DefaultButton addB = new DefaultButton("Add handler", ()-> testB.setClicked(()-> MsgBox2.on(this).info("Clicked")));
		addB.setTestID("add");
		cp.add(addB);

		cp.add(new VerticalSpacer(20));

		DefaultButton delB = new DefaultButton("Delete handler", ()-> testB.clearClicked());
		delB.setTestID("delete");
		cp.add(delB);
	}
}
