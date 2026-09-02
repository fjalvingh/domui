package to.etc.domuidemo.pages.components.buttons;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.HoverButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.buttons.SmallImgButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * The other buttons: a link that acts as a button, a small icon button, and
 * the hover button the framework's own title bar uses.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ButtonKindsPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("The kinds of button");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "The kinds of button"));

		Div shown = new Div("dm-tut-q");
		shown.add("Press any of them.");

		DefaultButton normal = new DefaultButton("Save", Icon.faCheck, a -> say(shown, "DefaultButton"));

		LinkButton link = new LinkButton("Forgot your password?", a -> say(shown, "LinkButton"));
		LinkButton linkIcon = new LinkButton("Delete", Icon.faTrash, a -> say(shown, "LinkButton with an icon"));

		SmallImgButton small = new SmallImgButton(Icon.faSearch, a -> say(shown, "SmallImgButton"));
		small.setTitle("A small button, for inside a control or a table row");

		HoverButton hover = new HoverButton("THEME/72x24_close.png", a -> say(shown, "HoverButton"));
		hover.setTitle("Its image holds three states side by side");

		FormBuilder fb = new FormBuilder(cp);
		fb.label("DefaultButton").control(normal);
		fb.label("LinkButton").control(link);
		fb.label("LinkButton with an icon").control(linkIcon);
		fb.label("SmallImgButton").control(small);
		fb.label("HoverButton").control(hover);
		cp.add(shown);

		cp.add(new Para().add("All four do the same thing - they call a click handler - and "
			+ "differ only in how loudly they ask to be pressed. A DefaultButton is the "
			+ "action of a screen; a LinkButton is a way out of it; a SmallImgButton belongs "
			+ "inside something else, like the calendar button of a DateInput2."));
		cp.add(new Para().add("The HoverButton draws itself from one image holding three "
			+ "states next to each other - normal, hover and disabled. It is what the "
			+ "framework's own page title bar uses for its back and close buttons."));

		//-- Disabled versions of the same four
		cp.add(new HTag(2, "The same four, disabled"));
		Div row = new Div("dm-tut");
		cp.add(row);
		DefaultButton d1 = new DefaultButton("Save", Icon.faCheck);
		d1.setDisabled(true);
		LinkButton d2 = new LinkButton("Forgot your password?");
		d2.setDisabled(true);
		SmallImgButton d3 = new SmallImgButton(Icon.faSearch);
		d3.setDisabled(true);
		HoverButton d4 = new HoverButton("THEME/72x24_close.png");
		d4.setDisabled(true);
		row.add(d1);
		row.add(d2);
		row.add(d3);
		row.add(d4);
	}

	private static void say(Div into, String what) {
		into.removeAllChildren();
		into.add("Pressed: " + what);
	}
}
