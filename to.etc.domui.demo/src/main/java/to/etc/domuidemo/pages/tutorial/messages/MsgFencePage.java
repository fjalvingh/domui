package to.etc.domuidemo.pages.tutorial.messages;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.ErrorMessageDiv;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "telling something to a user", step 5: a message travels upwards until
 * it meets an error fence, and the fence decides where it is shown. This page has
 * three of them: the two panels, and the page itself.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MsgFencePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Where a message ends up");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Where a message ends up"));

		cp.add(new HTag(2, "Two panels, each with a fence of its own"));
		addPanel(cp, "The first order", "Big Ones");
		addPanel(cp, "The second order", "Nevermind");

		cp.add(new HTag(2, "And the page around them"));
		Div pageButtons = new Div("dm-tut");
		cp.add(pageButtons);
		pageButtons.add(new DefaultButton("A message about the page", a -> addGlobalMessage(UIMessage.warning(TutorialMsg.orderStockLow, "Big Ones", 3))));
		pageButtons.add(new DefaultButton("Clear it", a -> clearGlobalMessage()));
	}

	private void addPanel(ContentPanel cp, String title, String album) throws Exception {
		Div panel = new Div("dm-tut");
		cp.add(panel);
		panel.add(new HTag(3, title));

		//-- Make this panel a fence, and let this div show what it catches.
		ErrorMessageDiv emd = new ErrorMessageDiv(panel);
		panel.add(emd);

		Text2<Integer> copies = new Text2<>(Integer.class);
		copies.setMandatory(true);

		FormBuilder fb = new FormBuilder(panel);
		fb.label("Copies of " + album).control(copies);

		panel.add(new DefaultButton("Order", a -> {
			Integer value = copies.getValue();                   // Empty: reports itself, into this panel
			panel.addGlobalMessage(UIMessage.info(TutorialMsg.orderSaved, value));
		}));
		panel.add(new DefaultButton("Clear", a -> {
			copies.setMessage(null);
			panel.clearGlobalMessage();
		}));
	}
}
