package to.etc.domuidemo.pages.components.dialog;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.domui.util.bugs.Bug;

/**
 * The bug indicator: the badge that appears in the corner of the page when
 * something went wrong that the user cannot be told about in words.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class BugIndicatorPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("The bug indicator");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "The bug indicator"));

		Div buttons = new Div("dm-tut");
		cp.add(buttons);
		buttons.add(new DefaultButton("Report a bug", () -> Bug.bug("The album has a price but no currency")));
		buttons.add(new DefaultButton("Report one with an exception",
			() -> Bug.bug(new IllegalStateException("Track 4 has no album"), "The track could not be priced")));
		buttons.add(new DefaultButton("Report five at once", () -> {
			for(int i = 1; i <= 5; i++)
				Bug.bug("Track " + i + " is longer than the album it is on");
		}));
		buttons.add(new DefaultButton("Report more than twenty", () -> {
			for(int i = 1; i <= 25; i++)
				Bug.bug("Order line " + i + " refers to an album that was deleted");
		}));

		cp.add(new Para().add("Press a button: a red badge appears in the top right corner of the "
			+ "page with the number of problems in it. Press the badge and the report opens, "
			+ "newest first; the triangle in front of a line opens the stack trace of the place "
			+ "the bug was reported from. Closing the report throws the collected bugs away, so "
			+ "the badge goes with it. Past twenty the count becomes an infinity sign."));

		cp.add(new HTag(2, "Reporting a bug"));
		cp.add(new Para().add("A bug is a broken assumption: data that cannot be what it is, a case "
			+ "the code does not have. It is not a validation error and not an exception - the "
			+ "user did nothing wrong, and the request can go on. Bug.bug() records it, the "
			+ "screen keeps working, and the badge tells whoever is looking that something in "
			+ "here is not right."));
		cp.add(new Para().add("Bug.bug() takes a message, a Throwable, or both. Whatever a bug is "
			+ "reported with, the reporting location is recorded with it, which is what the "
			+ "opened line shows."));

		cp.add(new HTag(2, "Turning it on"));
		cp.add(new Para().add("The badge is drawn by DefaultBugListener, which an application "
			+ "switches on in its DomApplication.initialize() with "
			+ "DefaultBugListener.registerSessionListener(this). It collects the bugs of a "
			+ "conversation, so they survive until the report is closed. Without a listener "
			+ "Bug.bug() still records - to the log - but nothing appears on the screen."));
	}
}
