package to.etc.domuidemo.pages.tutorial.messages;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.MsgBoxButton;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;

/**
 * Tutorial, "telling something to a user", step 2: a message box that waits for an
 * answer, and one that asks for a value.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MsgAskPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Asking the user something");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Asking the user something"));

		Div result = new Div("dm-tut");
		Div buttons = new Div();
		cp.add(buttons);
		cp.add(result);
		result.add("The answers appear here.");

		//-- Yes/no: the answer is a MsgBoxButton.
		buttons.add(new DefaultButton("Delete the album", a -> MsgBox2.on(this)
			.question()
			.text("Delete the album \"Big Ones\"? This cannot be undone.")
			.yesNo()
			.onAnswer(button -> {
				if(button == MsgBoxButton.YES) {
					say(result, "Deleted.");
				} else {
					say(result, "Nothing was deleted.");
				}
			})
		));

		//-- Buttons of your own: the answer is whatever you attached to the button.
		buttons.add(new DefaultButton("Ship it how?", a -> MsgBox2.on(this)
			.question()
			.text("This order has one album in stock and one that has to be ordered. What do we do?")
			.button("Ship what we have", ShipAction.Partial)
			.button("Wait for the rest", ShipAction.Wait)
			.button("Cancel the order", ShipAction.Cancel)
			.onAnswer2(answer -> say(result, "Chosen: " + answer))
		));

		//-- Asking for a value: the box carries a control, and the handler gets its value.
		buttons.add(new DefaultButton("How many copies?", a -> {
			Text2<Integer> copies = new Text2<>(Integer.class);
			copies.setMandatory(true);

			MsgBox2.on(this)
				.title("Order")
				.input("Copies", copies, value -> say(result, TutorialMsg.orderSaved.format(value)))
			;
		}));

		//-- The same, but the box refuses to close while the answer is wrong.
		buttons.add(new DefaultButton("How many copies, checked", a -> {
			Text2<Integer> copies = new Text2<>(Integer.class);
			copies.setMandatory(true);

			MsgBox2.on(this)
				.title("Order")
				.input("Copies", copies, value -> say(result, TutorialMsg.orderSaved.format(value)))
				.onValidate(button -> {
					if(button != MsgBoxButton.CONTINUE) {         // Cancelling is always allowed
						return true;
					}
					Integer value = copies.getValueSafe();        // Reports "mandatory" itself when empty
					if(null == value) {
						return false;
					}
					if(value.intValue() > 10) {
						copies.setMessage(UIMessage.error(TutorialMsg.orderTooLarge, 10));
						return false;                             // Keeps the box open
					}
					return true;
				})
			;
		}));
	}

	private static void say(Div result, String what) {
		result.removeAllChildren();
		result.add(what);
	}

	private enum ShipAction {
		Partial, Wait, Cancel
	}
}
