package to.etc.domuidemo.pages.tutorial.layout;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.misc.MsgBox2;
import to.etc.domui.component2.buttons.ButtonBar2;
import to.etc.domui.component2.form4.FormBuilder;
import to.etc.domui.component.input.Text2;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.themes.Theme;

/**
 * Tutorial, "layout", step 1: the two things almost every screen is made of - a
 * ContentPanel holding the content, and a ButtonBar2 holding what you can do with it.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class LayoutPanelPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Panels and button bars");

		//-- Deliberately added to the page itself, so you can see what the panel does.
		Div outside = new Div();
		add(outside);
		outside.add("This line sits on the page itself: no padding, hard against the edge.");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Panels and button bars"));
		cp.add("Everything below is inside the ContentPanel, which is what supplies the space around it.");

		Text2<String> customer = new Text2<>(String.class);
		Text2<Integer> copies = new Text2<>(Integer.class);

		FormBuilder fb = new FormBuilder(cp);
		fb.label("Customer").control(customer);
		fb.label("Copies").control(copies);

		ButtonBar2 bb = new ButtonBar2();
		cp.add(bb);
		bb.addBackButton();
		bb.addButton("Save", Theme.BTN_SAVE, a -> MsgBox2.on(this).info("Saved."));
		bb.addConfirmedButton("Delete", Theme.BTN_DELETE, "Delete this order?", a -> MsgBox2.on(this).info("Deleted."));
		bb.right();                                        // Everything after this goes to the right
		bb.addLinkButton("Help", Theme.ICON_BIG_INFO, a -> MsgBox2.on(this).info("No help for you."));
	}
}
