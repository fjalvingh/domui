package to.etc.domuidemo.pages.tutorial.messages;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "telling something to a user", step 3: a UIMessage is a message that
 * stays on the page, either on the page as a whole or on one control.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MsgMessagePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Messages on the page");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Messages on the page"));

		cp.add(new HTag(2, "A message about the page"));
		Div pageButtons = new Div("dm-tut");
		cp.add(pageButtons);

		pageButtons.add(new DefaultButton("Info", a -> addGlobalMessage(UIMessage.info(TutorialMsg.orderSaved, 3))));
		pageButtons.add(new DefaultButton("Warning", a -> addGlobalMessage(UIMessage.warning(TutorialMsg.orderStockLow, "Big Ones", 3))));
		pageButtons.add(new DefaultButton("Error", a -> addGlobalMessage(UIMessage.error(TutorialMsg.orderTooLarge, 10))));
		pageButtons.add(new DefaultButton("Error about a field", a -> addGlobalMessage(UIMessage
			.error(TutorialMsg.orderEmpty)
			.location("Copies")                              // Names the field the message is about
		)));
		pageButtons.add(new DefaultButton("Clear them all", a -> clearGlobalMessage()));

		cp.add(new HTag(2, "A message about one control"));
		Text2<Integer> copies = new Text2<>(Integer.class);
		Text2<String> customer = new Text2<>(String.class);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").control(customer);
		fb.label("Copies").control(copies);

		Div controlButtons = new Div("dm-tut");
		cp.add(controlButtons);
		controlButtons.add(new DefaultButton("Warn about Copies", a -> copies.setMessage(UIMessage.warning(TutorialMsg.orderStockLow, "Big Ones", 3))));
		controlButtons.add(new DefaultButton("Reject Copies", a -> copies.setMessage(UIMessage.error(TutorialMsg.orderTooLarge, 10))));
		controlButtons.add(new DefaultButton("Clear Copies", a -> copies.setMessage(null)));
	}
}
