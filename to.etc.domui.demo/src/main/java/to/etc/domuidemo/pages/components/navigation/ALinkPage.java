package to.etc.domuidemo.pages.components.navigation;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.ALink;
import to.etc.domui.component.misc.Icon;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component.misc.WindowParameters;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.state.MoveMode;
import to.etc.domui.state.PageParameters;
import to.etc.domuidemo.pages.MiniPage;
import to.etc.domuidemo.pages.tutorial.navigation.NavDetailPage;

/**
 * ALink: a hyperlink to another DomUI page, which is a real link as well as a
 * click handler - so the browser's own "open in a new window" works on it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class ALinkPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("ALink");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "ALink"));

		//-- The plain case: a link to a page, which is shelved on top of this one.
		Div links = new Div("dm-tut");
		cp.add(links);

		ALink sub = new ALink(BreadCrumb2Page.class);
		sub.setText("To the BreadCrumb2 page (SUB: this page is shelved)");
		links.add(new Div().add(sub));

		//-- ...with parameters for the target page.
		ALink withParameters = new ALink(NavDetailPage.class, new PageParameters("level", 3));
		withParameters.setText("To a detail page, with level=3 in the url");
		links.add(new Div().add(withParameters));

		//-- ...replacing this page on the stack instead of stacking on top of it.
		ALink replace = new ALink(HamburgerMenuPage.class, MoveMode.REPLACE);
		replace.setText("To the HamburgerMenu page (REPLACE: this page is gone)");
		links.add(new Div().add(replace));

		//-- ...with an icon in front of the text.
		ALink withIcon = new ALink(PopupMenu2Page.class);
		withIcon.add(Icon.faList.createNode());
		withIcon.add(" To the PopupMenu2 page, with an icon");
		links.add(new Div().add(withIcon));

		cp.add(new Para().add("Right-click one of these and open it in a new tab: the link works there "
			+ "too, because an ALink renders a real href next to its click handler. Clicking it "
			+ "normally moves within this window and its page stack; opening it in a new window "
			+ "starts a window session of its own."));

		//-- A link that opens a browser window with a new DomUI window session in it.
		cp.add(new HTag(2, "A window of its own"));
		Div window = new Div("dm-tut");
		cp.add(window);

		ALink popup = new ALink(MiniPage.class);
		popup.setText("Open a page in a new browser window");
		popup.setNewWindowParameters(WindowParameters.createFixed(500, 400, "mini"));
		window.add(popup);

		cp.add(new Para().add("With window parameters set the link opens a browser window of that size "
			+ "and the page inside it gets a window session of its own: its page stack, its "
			+ "conversations and its state have nothing to do with the ones in this window."));

		//-- ...and the raw tag it is built on.
		cp.add(new HTag(2, "The plain tag"));
		Div plain = new Div("dm-tut");
		cp.add(plain);

		ATag external = new ATag();
		external.setHref("https://www.domui.org/");
		external.setTarget("_blank");
		external.add("An ordinary link to another site");
		plain.add(new Div().add(external));

		ATag handler = new ATag();
		handler.add("A link that only calls the server");
		handler.setClicked(a -> MsgBox2.on(this).info("The link was clicked; the browser went nowhere."));
		plain.add(new Div().add(handler));

		cp.add(new Para().add("ATag is the plain html anchor: give it an href and it is a link, give it "
			+ "a click handler and it is a button that looks like one. ALink is the one to use for "
			+ "another page of the application, because it works out that page's url for you and it "
			+ "says how the move affects the page stack."));
	}
}
