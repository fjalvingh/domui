package to.etc.domuidemo.pages.components.buttons;

import to.etc.domui.component.buttons.ActionButton;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.menu.UIAction;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.derbydata.db.Album;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.webapp.query.QCriteria;

/**
 * IUIAction: one description of "what may be done to this thing", used by
 * several buttons at once - each of which knows its name, icon and why it is
 * disabled.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ActionButtonPage extends UrlPage {
	private Album m_album;

	@Override
	public void createContent() throws Exception {
		setPageTitle("Buttons made from an action");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Buttons made from an action"));

		m_album = getSharedContext().query(QCriteria.create(Album.class).limit(1)).get(0);

		Div shown = new Div("dm-tut-q");
		shown.add("The three buttons below are all the same action.");

		//-- One action: its own name, tooltip, icon, reason to be disabled, and what it does.
		IUIAction ship = new UIAction("Ship it", "Send this album to the customer",
			Icon.faTruck, null, node -> {
			shown.removeAllChildren();
			shown.add("Shipped " + m_album.getTitle());
		});

		//-- And one that is disabled, with the reason it gives shown on hover.
		IUIAction reprint = new UIAction("Reprint", "Have the sleeve printed again",
			Icon.faPrint, "The printer is out of ink", node -> {
		});

		cp.add(new HTag(2, "The same actions, on a button and on a bar"));
		Div row = new Div("dm-tut");
		cp.add(row);
		row.add(new DefaultButton(ship));
		row.add(new DefaultButton(reprint));

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addAction(ship);
		bb.addAction(reprint);

		//-- A button with a menu of further actions hanging off it.
		cp.add(new HTag(2, "A button with more actions behind it"));
		ActionButton more = new ActionButton(ship);
		more.addAction(reprint);
		more.addAction(new UIAction("Cancel the order", "Take the order back",
			Icon.faTimes, null, node -> {
			shown.removeAllChildren();
			shown.add("Cancelled the order for " + m_album.getTitle());
		}));
		cp.add(more);
		cp.add(shown);

		cp.add(new Para().add("An action says what may be done to an instance: its name, its "
			+ "tooltip, its icon, the reason it cannot be done right now, and the code to run. "
			+ "A button made from one asks it all five things, so the same action can sit on a "
			+ "button and on a button bar without either of them repeating a text."));
		cp.add(new Para().add("Hover over Reprint: it is disabled because the action said why, "
			+ "and the reason is its tooltip."));
		cp.add(new Para().add("The last button carries the rest of the actions in a menu under "
			+ "the chevron."));
	}
}
