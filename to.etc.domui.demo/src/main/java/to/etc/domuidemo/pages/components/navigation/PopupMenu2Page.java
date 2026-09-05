package to.etc.domuidemo.pages.components.navigation;

import to.etc.domui.component.buttons.ActionButton;
import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.menu.UIAction;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.popupmenus.PopupMenu2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * PopupMenu2: a small menu that appears at a component and closes as soon as
 * something is chosen or the mouse goes elsewhere.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class PopupMenu2Page extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("PopupMenu2");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "PopupMenu2"));

		Div chosen = new Div("dm-tut-q");
		chosen.add("Choose something from one of the menus below.");

		//-- A menu built when the button is pressed, and thrown away when it closes.
		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("What to do with this album", a -> {
			PopupMenu2 pm = new PopupMenu2(a);
			pm.text("Play it").icon(Icon.faMusic).hint("Play the whole album").click(() -> report(chosen, "Play it")).append();
			pm.text("Add to the cart").icon(Icon.faShoppingCart).click(() -> report(chosen, "Add to the cart")).append();
			pm.text("Print the sleeve").icon(Icon.faPrint).click(() -> report(chosen, "Print the sleeve")).append();
			pm.text("Delete it").icon(Icon.faTrash)
				.disableReason("The album has been sold, so it cannot be deleted")
				.click(() -> report(chosen, "Delete it"))
				.append();
			pm.show(a);
		}));

		//-- The same menu, opening upwards.
		buttons.add(new DefaultButton("...opening upwards", a -> {
			PopupMenu2 pm = new PopupMenu2(a);
			pm.above();
			pm.text("Play it").icon(Icon.faMusic).click(() -> report(chosen, "Play it")).append();
			pm.text("Add to the cart").icon(Icon.faShoppingCart).click(() -> report(chosen, "Add to the cart")).append();
			pm.show(a);
		}));

		//-- Icons only: with no text on any item the text column is not rendered at all.
		buttons.add(new DefaultButton("Icons only", a -> {
			PopupMenu2 pm = new PopupMenu2(a);
			pm.icon(Icon.faStar).hint("Favourite").click(() -> report(chosen, "Favourite")).append();
			pm.icon(Icon.faPencil).hint("Rename").click(() -> report(chosen, "Rename")).append();
			pm.icon(Icon.faTrash).hint("Delete").click(() -> report(chosen, "Delete")).append();
			pm.show(a);
		}));

		cp.add(chosen);

		cp.add(new Para().add("An item is described by chaining text(), icon(), hint(), click() and "
			+ "disableReason() and then calling append(), which is what actually adds it. Forgetting "
			+ "the append() throws rather than silently losing the item. The menu closes itself when "
			+ "an item is chosen and when the mouse is pressed anywhere else, and it removes itself "
			+ "from the page when it does."));

		//-- The same menu, but built for you from a list of actions.
		cp.add(new HTag(2, "The same, from actions"));
		Div actionButton = new Div("dm-tut");
		cp.add(actionButton);

		String album = "Led Zeppelin IV";                 // The instance every action below works on
		ActionButton ab = new ActionButton(album, new UIAction<String>("Play it", "Play the whole album", Icon.faMusic,
			null, (node, instance) -> report(chosen, "Play " + instance)));
		ab.addAction(album, new UIAction<String>("Add to the cart", null, Icon.faShoppingCart,
			null, (node, instance) -> report(chosen, "Add " + instance + " to the cart")));
		ab.addAction(album, new UIAction<String>("Delete it", null, Icon.faTrash,
			"The album has been sold, so it cannot be deleted", (node, instance) -> report(chosen, "Delete " + instance)));
		actionButton.add(ab);

		cp.add(new Para().add("An ActionButton is a button plus a chevron that opens a PopupMenu2 of "
			+ "the other actions, and it fills the menu from the actions themselves. Where the entries "
			+ "are actions - which know their own name, icon and reason to be disabled - that is the "
			+ "shorter way to the same menu."));
	}

	private static void report(Div where, String what) {
		where.removeAllChildren();
		where.add("You chose \"" + what + "\".");
	}
}
