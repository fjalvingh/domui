package to.etc.domuidemo.pages.components.layout;

import to.etc.domui.component.layout.CaptionedPanel;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.Panel;
import to.etc.domui.component.misc.VerticalSpacer;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * The panels a screen is framed with, and the space between them.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class PanelsPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Panels");

		//-- Added to the page itself: no space around it at all.
		Div bare = new Div();
		add(bare);
		bare.add("This line is on the page itself, hard against the edge.");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Panels"));
		cp.add(new Para().add("Everything below is inside a ContentPanel, which is what "
			+ "supplies the padding a page's content needs. Compare the line above it."));

		//-- A panel with no caption: a box to group things in.
		Panel panel = new Panel();
		cp.add(panel);
		panel.add("A Panel: a div with the ui-spnl class, and nothing else. It is a box to "
			+ "put things in when they belong together.");

		cp.add(new VerticalSpacer(20));

		//-- A panel with a caption bar above it.
		CaptionedPanel captioned = new CaptionedPanel("Delivery address", new Div());
		cp.add(captioned);
		captioned.getContent().add("A CaptionedPanel: the same box with a title bar above it. "
			+ "The title and the content are both nodes, so either can hold anything.");

		cp.add(new VerticalSpacer(40));
		cp.add(new Para().add("Between the two panels above is a VerticalSpacer of 20 pixels, "
			+ "and above this line one of 40. It is a div of exactly that height - the way to "
			+ "push things apart when a css class would be overkill."));

		//-- A panel inside a panel, which is what a screen with sections looks like.
		CaptionedPanel outer = new CaptionedPanel("An order", new Div());
		cp.add(outer);
		Panel inner = new Panel();
		outer.getContent().add(inner);
		inner.add("Panels nest: this one is inside the captioned panel above.");
	}
}
