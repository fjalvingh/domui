package to.etc.domuidemo.pages.components.navigation;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.headers.ExpandHeader;
import to.etc.domui.component.headers.HamburgerMenu;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.menu.IUIAction;
import to.etc.domui.component.menu.UIAction;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

import java.util.ArrayList;
import java.util.List;

/**
 * HamburgerMenu: the list of actions behind the three-bar button, both as
 * ExpandHeader opens it and opened by hand.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class HamburgerMenuPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("HamburgerMenu");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "HamburgerMenu"));

		Div chosen = new Div("dm-tut-q");
		chosen.add("Choose something from one of the menus below.");

		//-- The usual way to get one: give an ExpandHeader a list of actions.
		cp.add(new HTag(2, "The menu of an ExpandHeader"));
		ExpandHeader header = new ExpandHeader("Led Zeppelin IV");
		cp.add(header);
		header.setOnExpand(content -> content.add("The tracks of the album would be listed here."));
		for(IUIAction<?> action : actions(chosen)) {
			header.addAction(action);
		}

		cp.add(new Para().add("The header only shows the three-bar button when it has actions, and "
			+ "pressing it opens a HamburgerMenu of them just under it. Every entry takes its name, "
			+ "its icon and its reason to be disabled from its own action."));

		//-- ...and the same menu, opened from a button of your own.
		cp.add(new HTag(2, "Opened by hand"));
		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("Menu", Icon.faBars, a -> {
			HamburgerMenu menu = new HamburgerMenu(actions(chosen));
			a.appendAfterMe(menu);
			menu.setOnSelection(action -> action.execute(a, null));
		}));

		cp.add(chosen);

		cp.add(new Para().add("A HamburgerMenu is made from a list of actions and appended next to "
			+ "whatever opens it. It closes itself as soon as an item is chosen, when the mouse is "
			+ "pressed anywhere else on the page and when escape is pressed - and opening one closes "
			+ "any other that is still up, so two of these menus are never open at the same time."));

		cp.add(new Para().add("Notice where the menu appears: not under the button, but against the "
			+ "right edge of the block the button is in. The menu is positioned absolutely at "
			+ "right: 0, which is why it belongs to a button that sits at the right of its own "
			+ "block - exactly the place ExpandHeader puts its three-bar button."));
	}

	/**
	 * The actions both menus on this page are made of. They report into the div they are given.
	 */
	private static List<IUIAction<?>> actions(Div chosen) {
		List<IUIAction<?>> list = new ArrayList<>();
		list.add(new UIAction<Void>("Play the album", "Play every track in order", Icon.faMusic,
			null, (node, instance) -> report(chosen, "Play the album")));
		list.add(new UIAction<Void>("Add to the cart", null, Icon.faShoppingCart,
			null, (node, instance) -> report(chosen, "Add to the cart")));
		list.add(new UIAction<Void>("Print the sleeve", null, Icon.faPrint,
			null, (node, instance) -> report(chosen, "Print the sleeve")));
		list.add(new UIAction<Void>("Delete the album", null, Icon.faTrash,
			"The album has been sold, so it cannot be deleted", (node, instance) -> report(chosen, "Delete the album")));
		return list;
	}

	private static void report(Div where, String what) {
		where.removeAllChildren();
		where.add("You chose \"" + what + "\".");
	}
}
