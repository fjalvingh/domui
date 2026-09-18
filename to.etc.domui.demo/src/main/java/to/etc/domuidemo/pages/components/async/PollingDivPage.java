package to.etc.domuidemo.pages.components.async;

import to.etc.domui.component.delayed.PollingDiv;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.Span;
import to.etc.domui.dom.html.UrlPage;

import java.text.DateFormat;
import java.util.Date;

/**
 * PollingDiv: a piece of the screen that keeps itself up to date, because the
 * browser keeps asking.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class PollingDivPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("PollingDiv");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "PollingDiv"));

		Div frame = new Div("dm-tut");
		cp.add(frame);
		frame.add(new Clock());

		cp.add(new Para().add("The clock above is not a clock in the browser: every couple of "
			+ "seconds the browser asks the server whether anything changed, the server calls "
			+ "checkForChanges() on every polling div of the conversation, and whatever those "
			+ "changed comes back as a normal delta."));

		cp.add(new Para().add("So the work happens on the server, and the screen follows. That is "
			+ "the component for something that changes on its own - a queue length, a job's state, "
			+ "a machine's temperature - and it costs a request every few seconds per browser, "
			+ "which is the reason not to use it for anything that does not need it."));

		cp.add(new Para().add("A polling div registers itself with the conversation when it is "
			+ "added to the page and unregisters when it is removed, so there is nothing to clean "
			+ "up. The default checkForChanges() rebuilds the whole component; this one changes "
			+ "only the text, which is cheaper and does not make the screen flicker."));
	}

	/**
	 * A clock that updates itself: only the time text changes on each poll.
	 */
	private static final class Clock extends PollingDiv {
		/** The two nodes this div updates: they are kept, so that a poll changes only their text. */
		private Span m_time;

		private Div m_ticksLine;

		private int m_ticks;

		@Override
		public void createContent() throws Exception {
			m_time = new Span();
			m_time.setFontSize("24px");
			m_time.add("Waiting for the first poll...");
			add(m_time);

			m_ticksLine = new Div();
			m_ticksLine.add("Polls so far: " + m_ticks);
			add(m_ticksLine);
		}

		@Override
		public void checkForChanges() throws Exception {
			m_ticks++;
			m_time.setText(DateFormat.getTimeInstance(DateFormat.LONG).format(new Date()));
			m_ticksLine.setText("Polls so far: " + m_ticks);
		}
	}
}
