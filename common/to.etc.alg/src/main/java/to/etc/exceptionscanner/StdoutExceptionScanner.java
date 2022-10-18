package to.etc.exceptionscanner;

import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.ConsumerEx;
import to.etc.function.SupplierEx;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Listens for exception strings written to the console, and calls
 * listeners when exceptions are found.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-01-21.
 */
public class StdoutExceptionScanner {
	private ConsumerEx<String> m_stdoutListener;

	private ConsumerEx<String> m_stderrListener;

	private enum State {
		STOPPED, STARTED, STOPPING
	}

	private State m_state = State.STOPPED;

	@Nullable
	private Thread m_reporterThread;

	private LinkedList<DiscoveredExceptionData> m_exceptionList = new LinkedList<DiscoveredExceptionData>();

	final private List<ConsumerEx<DiscoveredExceptionData>> m_listenerList = new CopyOnWriteArrayList<>();

	final private List<SupplierEx<String>> m_contextProviderList = new CopyOnWriteArrayList<>();

	private ExceptionRecognizer m_stdoutRecognizer = new ExceptionRecognizer(this::registerException, m_contextProviderList);

	private ExceptionRecognizer m_stderrRecognizer = new ExceptionRecognizer(this::registerException, m_contextProviderList);

	public StdoutExceptionScanner() {}

	public synchronized void start() {
		if(m_state != State.STOPPED)
			return;

		//-- Start the exception collection thread to report occurrences.
		Thread thread = m_reporterThread = new Thread(this::run);
		thread.setName("ExceptionScanner");
		thread.setPriority(java.lang.Thread.MAX_PRIORITY);
		thread.setDaemon(true);
		thread.start();
		m_state = State.STARTED;

		//-- Add our listeners
		m_stdoutListener = m_stdoutRecognizer::lineAdded;
		StdoutListener.getInstance().addStdoutListener(m_stdoutListener);
		m_stderrListener = m_stderrRecognizer::lineAdded;
		StdoutListener.getInstance().addStderrListener(m_stderrListener);
	}

	private void run() {
		try {
			LinkedList<DiscoveredExceptionData> todo = new LinkedList<DiscoveredExceptionData>();
			for(; ; ) {
				synchronized(this) {
					if(m_state != State.STARTED)
						break;
					LinkedList<DiscoveredExceptionData> curl = m_exceptionList;
					if(!curl.isEmpty()) {
						//-- Swap lists
						m_exceptionList = todo;
						todo = curl;
					} else {
						wait(10000);
					}
				}

				//-- Out of lock: if todo contains work handle it on this-thread until it's empty.
				while(!todo.isEmpty()) {
					DiscoveredExceptionData rx = todo.removeFirst();
					handleException(rx);
				}
			}
			synchronized(this) {
				m_state = State.STOPPED;
			}
		} catch(InterruptedException x) {
			Thread.currentThread().interrupt();				// Facepalm.
		} finally {
			System.err.println(getClass().getSimpleName() + ": thread terminated");
		}
	}

	private void handleException(DiscoveredExceptionData rx) {
		for(ConsumerEx<DiscoveredExceptionData> ixl : m_listenerList) {
			try {
				ixl.accept(rx);
			} catch(Exception x) {
				PrintStream pw = StdoutListener.out();
				pw.println("stdoutScanner: exception when calling exception listener:");
				x.printStackTrace(pw);
			}
		}
	}

	public void registerException(DiscoveredExceptionData rx) {
		synchronized(this) {
			if(m_exceptionList.size() > 200)						// Prevent from running into big problems
				return;
			m_exceptionList.add(rx);
			notifyAll();
		}
	}

	public synchronized void terminate() {
		if(m_state != State.STARTED)
			return;

		m_state = State.STOPPING;
		notifyAll();
		StdoutListener.getInstance().removeStdoutListener(m_stdoutListener);
		StdoutListener.getInstance().removeStderrListener(m_stderrListener);
	}

	public void addListener(ConsumerEx<DiscoveredExceptionData> l) {
		m_listenerList.add(l);
	}

	public void addContextProvider(SupplierEx<String> provider) {
		m_contextProviderList.add(provider);
	}
}
