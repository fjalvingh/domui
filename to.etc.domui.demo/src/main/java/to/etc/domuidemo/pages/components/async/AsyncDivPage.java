package to.etc.domuidemo.pages.components.async;

import to.etc.domui.component.delayed.AsyncDiv;
import to.etc.domui.component.layout.ContentPanel;
import to.etc.domui.dom.html.Div;
import to.etc.domui.dom.html.HTag;
import to.etc.domui.dom.html.Para;
import to.etc.domui.dom.html.UrlPage;
import to.etc.parallelrunner.IAsyncRunnable;
import to.etc.util.Progress;

import java.util.ArrayList;
import java.util.List;

/**
 * AsyncDiv: the same delayed work, but the result is built by the component
 * instead of by the job.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 */
public class AsyncDivPage extends UrlPage {
	@Override
	public void createContent() throws Exception {
		setPageTitle("AsyncDiv");

		ContentPanel cp = new ContentPanel();
		add(cp);
		cp.add(new HTag(1, "AsyncDiv"));

		cp.add(new CountingDiv(new CountingJob(8), "Counting slowly"));

		cp.add(new Para().add("The job and the screen are separated here: the job collects the "
			+ "answer into itself, and createContent(task) - which runs when the job is done, back "
			+ "on the page's own thread - reads it out and builds the result. That is the "
			+ "difference with AsyncContainer, whose job builds the Div itself."));

		cp.add(new Para().add("A cancelled job leaves a line saying so; a job that throws leaves "
			+ "the message and a foldable stack trace, which is what the component does with an "
			+ "exception unless createError() is overridden."));

		cp.add(new HTag(2, "One that fails"));
		cp.add(new CountingDiv(new CountingJob(-1), "This one throws"));
	}

	/**
	 * The component: it knows how to show what the job collected.
	 */
	private static final class CountingDiv extends AsyncDiv<CountingJob> {
		CountingDiv(CountingJob runnable, String what) {
			super(runnable, what);
		}

		@Override
		public void createContent(CountingJob task) throws Exception {
			Div result = new Div("dm-tut-q");
			add(result);
			for(String line : task.getLines()) {
				result.add(new Div().add(line));
			}
		}
	}

	/**
	 * The job: it only collects, and never touches the page.
	 */
	private static final class CountingJob implements IAsyncRunnable {
		private final int m_count;

		private final List<String> m_lines = new ArrayList<>();

		CountingJob(int count) {
			m_count = count;
		}

		@Override
		public void run(Progress p) throws Exception {
			if(m_count < 0) {
				throw new IllegalStateException("There is nothing to count");
			}
			p.setTotalWork(m_count);
			for(int i = 0; i < m_count; i++) {
				Thread.sleep(500);
				p.setCompleted(i);
				synchronized(this) {
					m_lines.add("Counted " + (i + 1));
				}
			}
		}

		synchronized List<String> getLines() {
			return new ArrayList<>(m_lines);              // Written on one thread, read on another
		}
	}
}
