package to.etc.domuidemo.pages.components.navigation;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.input.Text2;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.title.AppPageTitleBar;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * AppPageTitleBar: the bar at the top of a page - an icon, the title of the
 * screen, buttons, and optionally the page's messages.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class PageTitleBarPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Application title bar");

		//-- The bar belongs to the page itself, above everything else on it.
		AppPageTitleBar bar = new AppPageTitleBar("Album maintenance", false);
		add(bar);
		bar.setIcon("img/logo-small.png");                // An image url, not an icon reference
		bar.setShowBackButton(true);                      // Back to the page below this one on the stack
		bar.setHint("Everything about the albums in the shop");
		bar.addButton(Icon.faSearch, "Find an album", a -> MsgBox2.on(this).info("Find an album"));
		bar.addButton(Icon.faPrint, "Print the catalogue", a -> MsgBox2.on(this).info("Print the catalogue"));

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "AppPageTitleBar"));

		cp.add(new Para().add("The bar above is this page's title bar: the image, the title, the hint "
			+ "you get by hovering over it, and the two buttons at its right. The arrow in front of "
			+ "the image goes back to the page below this one; on a page that is the bottom of the "
			+ "stack it becomes a close button instead."));

		cp.add(new Para().add("Without setIcon() the bar looks for an image itself: the icon named in "
			+ "the page's @UIMenu annotation, or a png beside the page class with the same name as "
			+ "that class. So a screen normally sets nothing at all and still gets its own icon."));

		//-- With catchError the bar also shows what the fence around it caught.
		cp.add(new HTag(2, "A bar that shows the messages"));
		Div fenced = new Div("dm-tut");
		cp.add(fenced);
		fenced.setErrorFence();                           // ...so the messages of this block stop here

		AppPageTitleBar catching = new AppPageTitleBar("Order an album", true);
		fenced.add(catching);

		Text2<Integer> copies = new Text2<>(Integer.class);
		copies.setMandatory(true);

		FormBuilder fb = new FormBuilder(fenced);
		fb.label("Copies of Led Zeppelin IV").control(copies);

		fenced.add(new DefaultButton("Order", a -> {
			Integer value = copies.getValue();            // Empty: the control reports it into this fence
			MsgBox2.on(this).info(value + " copies ordered");
		}));

		cp.add(new Para().add("Press Order with the field empty: the message appears in the title bar "
			+ "of the block, under its title. That is the second argument of the constructor - a bar "
			+ "made with catchError listens to the error fence above it and shows what it catches. "
			+ "A page normally has one such bar, and it is the page's error display."));
	}
}
