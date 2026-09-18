package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.component.layout.MessageLine;
import to.etc.domui.component.misc.Explanation;
import to.etc.domui.component.misc.InfoPanel;
import to.etc.domui.dom.errors.MsgType;
import to.etc.domui.dom.html.ATag;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;

/**
 * The three notices that are part of the page itself instead of being posted as
 * a message: MessageLine, InfoPanel and Explanation.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class NoticePage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("Notices on the page");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "Notices on the page"));

		cp.add(new HTag(2, "MessageLine"));
		cp.add(new MessageLine(MsgType.INFO, "The prices shown are <b>excluding</b> VAT."));
		cp.add(new MessageLine(MsgType.WARNING, "This album has no tracks yet, so nobody can buy it."));
		cp.add(new MessageLine(MsgType.ERROR, "The connection to the shop was lost."));

		//-- An icon of your own instead of the one the type gives.
		cp.add(new MessageLine("img/btnSmileyWink.png", "An icon of your own, by resource url."));

		//-- ...and a line that is built rather than written, so it can hold anything.
		cp.add(new MessageLine(MsgType.INFO, line -> {
			line.add("The stock is counted every night. ");
			ATag link = new ATag();
			link.setHref("https://demo.domui.org/");
			link.add("Where the numbers come from");
			line.add(link);
		}));

		cp.add(new Para().add("A MessageLine is one line: an icon and a text, which may contain "
			+ "simple html. It is written into the page like any other node, so it says something "
			+ "about the screen rather than about what just happened."));

		cp.add(new HTag(2, "InfoPanel"));
		cp.add(new InfoPanel("The CD shop sells albums, not tracks.<br/>"
			+ "A track can only be bought as part of the album it is on."));

		cp.add(new Para().add("An InfoPanel is the same idea with more room: a paragraph of "
			+ "explanation with a large icon beside it."));

		cp.add(new HTag(2, "Explanation"));
		cp.add(new Explanation("Search is on the album title, and it is case insensitive."));
		cp.add(new Explanation(MsgType.WARNING, "Deleting an artist deletes its albums with it."));

		cp.add(new Para().add("An Explanation is an InfoPanel that also has a severity, so the "
			+ "same block can be a remark, a warning or an error."));
	}
}
