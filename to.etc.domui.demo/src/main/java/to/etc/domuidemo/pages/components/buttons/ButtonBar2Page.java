package to.etc.domuidemo.pages.components.buttons;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.buttons.ButtonBar2.Direction;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;

/**
 * ButtonBar2: the bar a screen's buttons sit on - a left group, a right group,
 * and the kinds of button it knows how to make.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ButtonBar2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ButtonBar2: the bar the buttons sit on");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ButtonBar2: the bar the buttons sit on"));

		Div shown = new Div("dm-tut-q");
		shown.add("Press something.");

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addButton("Save", Theme.BTN_SAVE, a -> say(shown, "Save"));
		bb.addConfirmedButton("Delete", Theme.BTN_DELETE, "Delete this album?", a -> say(shown, "Deleted"));
		bb.addBackButton();
		bb.right();                                        // Everything after this goes right
		bb.addLinkButton("Help", Icon.faQuestionCircle, a -> MsgBox2.on(this).info("No help yet"));
		cp.add(shown);

		cp.add(new Para().add("The bar has a left group and a right group; right() switches "
			+ "over. Delete asks its question first and only calls the handler on yes."));
		cp.add(new Para().add("Back goes to the page you came from - and when there is nothing "
			+ "to go back to it quietly becomes a Close button instead. Open this page from "
			+ "the component list to see it as Back."));

		//-- Order decides the position, not the call order.
		cp.add(new HTag(2, "Order, not call order"));
		ButtonBar2 ordered = new ButtonBar2();
		cp.add(ordered);
		ordered.addButton("third", a -> say(shown, "third"), 300);
		ordered.addButton("first", a -> say(shown, "first"), 100);
		ordered.addButton("second", a -> say(shown, "second"), 200);

		//-- A vertical bar, for a side panel.
		cp.add(new HTag(2, "Vertical"));
		ButtonBar2 vertical = new ButtonBar2(Direction.VERTICAL);
		cp.add(vertical);
		vertical.addButton("Play", Icon.faPlay, a -> say(shown, "Play"));
		vertical.addButton("Pause", Icon.faPause, a -> say(shown, "Pause"));
		vertical.addButton("Stop", Icon.faStop, a -> say(shown, "Stop"));

		cp.add(new Para().add("Every add takes an optional order number and the bar sorts on "
			+ "it, so where a button ends up does not depend on the order the code happened "
			+ "to run in - which matters when a base class and a page both add buttons."));
	}

	private static void say(Div into, String what) {
		into.removeAllChildren();
		into.add("Pressed: " + what);
	}
}
