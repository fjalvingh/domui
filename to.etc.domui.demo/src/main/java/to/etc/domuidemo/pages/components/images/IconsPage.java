package to.etc.domuidemo.pages.components.images;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.buttons.LinkButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.dom.css.DisplayType;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.fontawesome.FaIcon;
import to.etc.domui.themes.Theme;

/**
 * The three kinds of icon - a font icon, an svg icon and an image icon - and the
 * css classes that change their size and their colour.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class IconsPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Icons");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Icons"));

		//-- The three kinds, all made the same way: a reference, asked for a node.
		cp.add(new HTag(2, "The three kinds"));
		Div kinds = new Div("dm-tut");
		cp.add(kinds);
		kinds.add(labelled("a font icon", FaIcon.faMusic.createNode()));
		kinds.add(labelled("an svg icon", Icon.of("img/checkmark.svg").createNode()));
		kinds.add(labelled("an image icon", Icon.of("img/java-icon.png").createNode()));
		kinds.add(labelled("a character", Icon.of('♫').createNode()));

		cp.add(new Para().add("All four are IIconRefs asked for a node. The reference decides what "
			+ "the node is: a span with the font's css classes, the svg file inlined in the page, "
			+ "an img tag, or a span with the character in it. Icon.of() picks by the extension of "
			+ "the path it is given."));

		//-- Size, by adding a css class to the reference.
		cp.add(new HTag(2, "Size"));
		Div sizes = new Div("dm-tut");
		cp.add(sizes);
		for(String size : new String[]{"is-size-7", "is-size-6", "is-size-5", "is-size-4", "is-size-3", "is-size-2", "is-size-1"}) {
			sizes.add(labelled(size, FaIcon.faMusic.css(size).createNode()));
		}

		Div svgSizes = new Div("dm-tut");
		cp.add(svgSizes);
		for(String size : new String[]{"is-size-small", "is-size-normal", "is-size-medium", "is-size-large"}) {
			svgSizes.add(labelled(size, Icon.of("img/checkmark.svg").css(size).createNode()));
		}

		cp.add(new Para().add("css() does not change the reference; it makes a new one that carries "
			+ "those classes, so the same icon can be used large in one place and small in another. "
			+ "The sizes run is-size-1 (largest) to is-size-7, with is-size-small, is-size-normal, "
			+ "is-size-medium and is-size-large next to them."));

		//-- Colour: the same mechanism, and the one kind it does not work on.
		cp.add(new HTag(2, "Colour"));
		Div colours = new Div("dm-tut");
		cp.add(colours);
		for(String colour : new String[]{"is-primary", "is-link", "is-info", "is-success", "is-warning", "is-danger", "is-dark"}) {
			colours.add(labelled(colour, FaIcon.faSkullCrossbones.css("is-size-3", colour).createNode()));
		}

		Div svgColours = new Div("dm-tut");
		cp.add(svgColours);
		svgColours.add(labelled("svg, plain", Icon.of("img/checkmark.svg").css("is-size-3").createNode()));
		svgColours.add(labelled("svg, is-danger", Icon.of("img/checkmark.svg").css("is-size-3", "is-danger").createNode()));
		svgColours.add(labelled("image, is-danger", Icon.of("img/java-icon.png").css("is-danger").createNode()));

		cp.add(new Para().add("A font icon is text, so its colour is the css colour. A single-colour "
			+ "svg is recoloured by filling its paths. An image icon is a picture and cannot be "
			+ "recoloured at all - the last one above is unchanged - which is the reason to prefer "
			+ "a font or an svg icon for anything that has to follow the theme."));

		//-- The framework's own two sets.
		cp.add(new HTag(2, "The icons the framework itself uses"));
		Div sets = new Div("dm-tut");
		cp.add(sets);
		sets.add(labelled("Icon.faSave", Icon.faSave.createNode()));
		sets.add(labelled("Icon.faTrash", Icon.faTrash.createNode()));
		sets.add(labelled("Icon.faSearch", Icon.faSearch.createNode()));
		sets.add(labelled("Theme.BTN_SAVE", Theme.BTN_SAVE.createNode()));
		sets.add(labelled("Theme.BTN_DELETE", Theme.BTN_DELETE.createNode()));
		sets.add(labelled("Theme.ICON_MINI_ERROR", Theme.ICON_MINI_ERROR.createNode()));

		cp.add(new Para().add("Icon is the generic set every font pack has to implement - here they "
			+ "are FontAwesome 6 glyphs, because this application includes the fontawesome6free "
			+ "module. Theme is the set of icons the framework's own components use, and those are "
			+ "images from the current theme's directory. Both can be remapped to something else."));

		//-- Where icons are actually used: a component that takes an IIconRef.
		cp.add(new HTag(2, "In a component"));
		Div inComponents = new Div("dm-tut");
		cp.add(inComponents);
		inComponents.add(new DefaultButton("Save", Icon.faSave, a -> MsgBox2.on(this).info("Saved")));
		inComponents.add(new DefaultButton("Check", Icon.of("img/checkmark.svg"), a -> MsgBox2.on(this).info("Checked")));
		inComponents.add(new LinkButton("Delete", Icon.faTrash.css("is-danger"), a -> MsgBox2.on(this).info("Deleted")));

		cp.add(new Para().add("Every component that shows an icon takes an IIconRef, never a "
			+ "component: a component can be added to the page only once, and a reference can be "
			+ "used as often as you like. That is the whole reason the two are separate."));
	}

	/**
	 * One icon with its name under it, so the examples can be told apart.
	 */
	private static Div labelled(String text, NodeBase icon) {
		Div d = new Div("dm-tut-hi");
		d.setDisplay(DisplayType.INLINE_BLOCK);
		d.setMargin("4px");
		d.add(icon);
		d.add(new Span().add(" " + text));
		return d;
	}
}
