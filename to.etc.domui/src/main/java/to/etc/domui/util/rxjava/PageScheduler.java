package to.etc.domui.util.rxjava;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import to.etc.domui.dom.html.NodeBase;
import to.etc.domui.dom.html.Page;
import to.etc.function.IExecute;
import to.etc.util.StringTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 15-11-19.
 */
final public class PageScheduler extends Scheduler {
	final private Page m_page;

	public PageScheduler(Page page) {
		m_page = page;
	}

	static public Scheduler on(NodeBase pageNode) {
		Page page = pageNode.getPage();
		return new PageScheduler(page);
	}

	/**
	 * Create a worker for the specified scheduling chain. Each schedule action gets its own
	 * worker.
	 */
	@Override
	public Worker createWorker() {
		return new DomUIPageWorker(m_page);
	}

	/**
	 * Worker that registers with a DomUI page, and uses the callbacks to execute rxjava events only
	 * when the page has been activated, for instance because of a PollingDiv.
	 */
	static private final class DomUIPageWorker extends Worker {
		static public final boolean DEBUG = false;

		private final List<PageWork> m_workList = new ArrayList<>();

		private final Page m_page;

		private boolean m_disposed;

		private final IExecute m_destroyListener = this::dispose;

		private final IExecute m_requestListener = this::sync;

		public DomUIPageWorker(Page page) {
			m_page = page;
			m_page.addDestroyListener(m_destroyListener);
			m_page.addBeforeRequestListener(m_requestListener);
		}

		/**
		 * Called when we're entering page context: this processes all queued work.
		 */
		private void sync() {
			long cts = System.currentTimeMillis();
			List<PageWork> todoList;
			synchronized(this) {
				d("PAGESCHEDULER: work size=" + m_workList.size());
				if(m_workList.isEmpty())
					return;
				todoList = new ArrayList<>();
				while(!m_workList.isEmpty()) {
					PageWork w = m_workList.get(0);
					if(w.getExecuteWhen() > cts) {
						break;
					}
					todoList.add(w);
					m_workList.remove(0);
				}
			}

			for(PageWork pageWork : todoList) {
				pageWork.getWork().run();
			}
		}

		static private void d(String s) {
			if(DEBUG)
				System.out.println("PAGESCHEDULER: " + s);
		}

		@Override
		public Disposable schedule(Runnable runnable, long l, TimeUnit timeUnit) {
			d("schedule called");
			long ets = System.currentTimeMillis() + timeUnit.toMillis(l);			// Find execution time.
			PageWork w = new PageWork(this, runnable, ets);
			synchronized(this) {
				d("Got work " + runnable.getClass());
				if(m_disposed) {
					d("But I am disposed");
					return Disposable.disposed();
				}

				if(m_workList.isEmpty()) {
					m_workList.add(w);
				} else {
					int index = Collections.binarySearch(m_workList, w, Comparator.comparingLong(PageWork::getExecuteWhen));
					if(index < 0)
						index = (-index - 1);
					m_workList.add(index, w);
				}
			}

			return w;
		}

		@Override
		public void dispose() {
			if(DEBUG) {
				StringTool.dumpLocation("DISPOSE CALLED");
			}
			List<PageWork> todo;
			synchronized(this) {
				if(m_disposed) {
					d("Dispose called but already disposed");
					return;
				}
				todo = new ArrayList<>(m_workList);
				m_workList.clear();
				m_disposed = true;
			}
			for(PageWork pageWork : todo) {
				pageWork.dispose();
			}
			m_page.removeDestroyListener(m_destroyListener);
			m_page.removeBeforeRequestListener(m_requestListener);
		}

		@Override
		public synchronized boolean isDisposed() {
			return m_disposed;
		}
	}

	private final static class PageWork implements Disposable {
		private final DomUIPageWorker m_worker;

		private final Runnable m_work;

		private final long m_executeWhen;

		private boolean m_disposed;

		public PageWork(DomUIPageWorker worker, Runnable work, long executeWhen) {
			m_worker = worker;
			m_work = work;
			m_executeWhen = executeWhen;
		}

		public Runnable getWork() {
			return m_work;
		}

		public long getExecuteWhen() {
			return m_executeWhen;
		}

		@Override
		public void dispose() {
			DomUIPageWorker.d("pagework dispose called");
			synchronized(m_worker) {
				if(m_disposed)
					return;
				m_disposed = true;
				m_worker.m_workList.remove(this);
			}
		}

		@Override
		public boolean isDisposed() {
			synchronized(m_worker) {
				return m_disposed;
			}
		}
	}
}
