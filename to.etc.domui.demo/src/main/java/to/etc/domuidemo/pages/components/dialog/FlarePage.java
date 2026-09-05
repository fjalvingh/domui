package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MessageFlare;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.errors.UIMessage;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * MessageFlare: a message that appears over the page and vanishes again, for
 * something the user does not have to answer.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class FlarePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("MessageFlare");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "MessageFlare"));

		Div buttons = new Div("dm-tut");
		cp.add(buttons);

		//-- One line, of a given severity.
		buttons.add(new DefaultButton("Saved", a -> MessageFlare.display(this, MsgType.INFO, "The album has been saved.")));
		buttons.add(new DefaultButton("Careful", a -> MessageFlare.display(this, MsgType.WARNING, "This album has no tracks yet.")));
		buttons.add(new DefaultButton("Failed", a -> MessageFlare.display(this, MsgType.ERROR, "The album could not be saved.")));

		//-- A UIMessage carries its own severity, so it needs no type.
		buttons.add(new DefaultButton("From a UIMessage", a ->
			MessageFlare.display(this, UIMessage.warning(DialogMsg.albumStockLow, "Big Ones", Integer.valueOf(3)))));

		//-- One flare per request: several messages end up in the same one, and the severest wins.
		buttons.add(new DefaultButton("Three at once", a -> {
			MessageFlare flare = MessageFlare.display(this, MsgType.INFO, "212 albums were read.");
			flare.addMessage("3 albums were skipped: they have no artist.", MsgType.WARNING);
			flare.addMessage("1 album was refused: it has no title.", MsgType.ERROR);
		}));

		cp.add(new Para().add("A flare is not part of the page: it is added to the page body for one "
			+ "request, shown over everything, and removed again when the user moves the mouse. "
			+ "It says something happened; it never asks anything, and there is nothing to press."));
		cp.add(new Para().add("Adding several messages in one request fills one flare, and the "
			+ "severest of them decides the colour and the icon - press \"three at once\" and the "
			+ "flare is an error flare."));
	}
}
