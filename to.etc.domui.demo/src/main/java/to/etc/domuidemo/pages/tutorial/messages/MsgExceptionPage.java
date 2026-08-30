package to.etc.domuidemo.pages.tutorial.messages;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ExceptionDialog;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.nls.CodeException;

/**
 * Tutorial, "telling something to a user", step 4: ExceptionDialog shows an
 * exception. What the user gets to see depends on whether some translator
 * recognises the exception; the one for {@link OutOfStockException} is registered
 * in {@link to.etc.domuidemo.Application}.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class MsgExceptionPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Showing an exception");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Showing an exception"));

		Div buttons = new Div("dm-tut");
		cp.add(buttons);

		//-- Nothing recognises this one: the dialog shows the stack trace.
		buttons.add(new DefaultButton("Save (unexpected failure)", a -> {
			try {
				save(1);
			} catch(Exception x) {
				ExceptionDialog.create(this, "Saving the order failed", x);
			}
		}));

		//-- A CodeException carries a translated message, which is all the dialog shows.
		buttons.add(new DefaultButton("Save (a refused order)", a -> {
			try {
				save(2);
			} catch(Exception x) {
				ExceptionDialog.create(this, "Saving the order failed", x);
			}
		}));

		//-- A translator registered for this exception type turns it into a sentence.
		buttons.add(new DefaultButton("Save (sold out)", a -> {
			try {
				save(3);
			} catch(Exception x) {
				ExceptionDialog.create(this, "Saving the order failed", x);
			}
		}));

		//-- The same try/catch, written once.
		buttons.add(new DefaultButton("Save (with executeWithDialog)", a -> executeWithDialog("Saving the order failed", () -> save(3))));
	}

	/**
	 * Stands in for the business code: it fails in three different ways.
	 */
	private void save(int how) {
		switch(how) {
			case 1:
				throw new IllegalStateException("The connection to the shop database was lost");

			case 2:
				throw new CodeException(TutorialMsg.orderTooLarge, 10);

			default:
				throw new OutOfStockException("Big Ones");
		}
	}
}
