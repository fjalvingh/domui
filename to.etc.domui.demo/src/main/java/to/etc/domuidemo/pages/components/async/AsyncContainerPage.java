package to.etc.domuidemo.pages.components.async;

import to.etc.domui.component.buttons.DefaultButton;
import to.etc.domui.component.delayed.AsyncContainer;
import to.etc.domui.component.delayed.IActivity;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.util.Progress;

/**
 * AsyncContainer: work that takes too long to do inside a request, done on a
 * thread of its own while the screen shows how far it got.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class AsyncContainerPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("AsyncContainer");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "AsyncContainer"));

		Div where = new Div("dm-tut");
		cp.add(where);

		cp.add(new DefaultButton("Start the slow job", a -> {
			where.removeAllChildren();
			where.add(new AsyncContainer(new SlowJob(8)));
		}));

		cp.add(new DefaultButton("...one that cannot be cancelled", a -> {
			where.removeAllChildren();
			AsyncContainer ac = new AsyncContainer(new SlowJob(5));
			ac.setAbortable(false);                       // No cancel button
			where.add(ac);
		}));

		cp.add(new DefaultButton("...one that throws", a -> {
			where.removeAllChildren();
			IActivity failing = p -> {
				p.setTotalWork(3);
				for(int i = 0; i < 3; i++) {
					Thread.sleep(400);
					p.setCompleted(i);
				}
				throw new IllegalStateException("The job could not finish");
			};
			where.add(new AsyncContainer(failing));
		}));

		cp.add(new Para().add("Press one of the buttons. The container appears with a spinner, a "
			+ "percentage and - unless it was told not to - a cancel button; the browser asks the "
			+ "server every few seconds how far the job got. When the job is done the container "
			+ "replaces itself with whatever the job produced."));

		cp.add(new HTag(2, "What the job may touch"));
		cp.add(new Para().add("The job runs on a thread of its own, while the page it was started "
			+ "from is not active at all. So it must not touch that page: not its components, not "
			+ "its fields, and not its shared QDataContext. It gets a Progress to report into and "
			+ "to ask whether it was cancelled, and it hands back a Div - which the container puts "
			+ "on the screen in its place, in a request where the page is alive again."));

		cp.add(new Para().add("An exception is not lost: the third button's job throws, and the "
			+ "stack trace arrives in a message box rather than disappearing into a log."));
	}

	/**
	 * A job that takes its time. It reports progress, and stops when it is cancelled.
	 */
	private static final class SlowJob implements IActivity {
		private final int m_steps;

		SlowJob(int steps) {
			m_steps = steps;
		}

		@Override
		public Div run(Progress p) throws Exception {
			p.setTotalWork(m_steps);
			for(int i = 0; i < m_steps; i++) {
				Thread.sleep(700);
				p.setCompleted(i, "step " + (i + 1) + " of " + m_steps);
				if(p.isCancelled()) {
					return null;                          // Cancelled: the container says so
				}
			}
			Div result = new Div("dm-tut-q");
			result.add("The job finished, and this is what it made.");
			return result;
		}
	}
}
