package to.etc.domuidemo.pages.components.buttons;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;

/**
 * DefaultButton: a text, an icon, an accelerator, and the css classes that
 * decide what it looks like.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class DefaultButtonPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("DefaultButton: the ordinary button");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "DefaultButton: the ordinary button"));

		Div shown = new Div("dm-tut-q");
		shown.add("Press one of the buttons.");

		//-- Text, icon, both; and the accelerator marked with a !
		Div row = new Div("dm-tut");
		cp.add(new HTag(2, "Text, icon and accelerator"));
		cp.add(row);
		row.add(new DefaultButton("Just text", a -> say(shown, "Just text")));
		row.add(new DefaultButton("With an icon", Icon.faHeart, a -> say(shown, "With an icon")));
		row.add(new DefaultButton("", Icon.faTrash, a -> say(shown, "Icon only")));
		row.add(new DefaultButton("S!ave", Theme.BTN_SAVE, a -> say(shown, "Save - try alt-A")));

		//-- The fluent form builds the same button.
		row.add(new DefaultButton()
			.text("Fluent")
			.icon(Icon.faCheck)
			.clicked(a -> say(shown, "Built with text().icon().clicked()")));

		cp.add(new HTag(2, "What it looks like"));
		Div colours = new Div("dm-tut");
		cp.add(colours);
		for(String name : new String[]{"primary", "info", "success", "warning", "danger", "dark", "light"}) {
			colours.add(new DefaultButton(name, a -> say(shown, "is-" + name)).css("is-" + name));
		}

		Div sizes = new Div("dm-tut");
		cp.add(sizes);
		sizes.add(new DefaultButton("small").css("is-small"));
		sizes.add(new DefaultButton("normal"));
		sizes.add(new DefaultButton("medium").css("is-medium"));
		sizes.add(new DefaultButton("large").css("is-large"));
		sizes.add(new DefaultButton("outlined").css("is-primary", "is-outlined"));
		sizes.add(new DefaultButton("mini").mini());

		//-- Disabled, and disabled with a reason.
		cp.add(new HTag(2, "Disabled"));
		Div states = new Div("dm-tut");
		cp.add(states);
		DefaultButton disabler = new DefaultButton("Click me to disable me", Theme.BTN_CONFIRM,
			a -> say(shown, "Disabled now - it will not fire again"));
		disabler.setClicked(a -> {
			disabler.setDisabled(true);
			say(shown, "Disabled now - it will not fire again");
		});
		states.add(disabler);

		DefaultButton because = new DefaultButton("Cannot be pressed", Icon.faLock);
		because.setDisabledBecause("The invoice has already been sent");
		states.add(because);

		cp.add(shown);

		cp.add(new Para().add("The ! in a button's text marks its accelerator: 'S!ave' shows "
			+ "Save with an underlined a, and alt-A presses it. Write \\\\! for a real "
			+ "exclamation mark."));
		cp.add(new Para().add("A button is styled entirely by its css classes: is-primary and "
			+ "friends for the colour, is-small to is-large for the size, is-outlined for the "
			+ "outlined variant. mini() is the one built-in shortcut, for a button in a table "
			+ "row."));
	}

	private static void say(Div into, String what) {
		into.removeAllChildren();
		into.add("Pressed: " + what);
	}
}
