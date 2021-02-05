package to.etc.exceptionscanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.ConsumerEx;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Captures the stdout and stderr streams, and allows collecting data line
 * by line from these.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-01-21.
 */
@NonNullByDefault
public class StdoutListener {
	static private final StdoutListener INSTANCE = new StdoutListener();

	@Nullable
	private PrintStream m_stdout;

	@Nullable
	private PrintStream m_stderr;

	private boolean m_started;

	@Nullable
	private LineCollectingOutputStream m_capturingOutput;

	@Nullable
	private LineCollectingOutputStream m_capturingError;

	private List<ConsumerEx<String>> m_stdoutListenerList = new CopyOnWriteArrayList<>();

	private List<ConsumerEx<String>> m_stderrListenerList = new CopyOnWriteArrayList<>();

	private Charset m_consoleCharset = StandardCharsets.UTF_8;

	private StdoutListener() {}

	/**
	 * Take over stdout and stderr, assign our own output stream from them, and start the thread that scans the output for exceptions.
	 */
	static public void initialize(Charset consoleCharset) {
		INSTANCE.start(consoleCharset);
	}

	/**
	 * Kill the handler thread and reassign stdout and stderr back.
	 */
	static public void terminate() {
		INSTANCE.stop();
	}

	private synchronized void start(Charset consoleCharset) {
		if(m_started)
			return;
		m_consoleCharset = consoleCharset;

		PrintStream stdout = System.out;
		PrintStream stderr = System.err;

		LineCollectingOutputStream cout = m_capturingOutput = new LineCollectingOutputStream(stdout, consoleCharset, string -> {
			for(ConsumerEx<String> l : m_stdoutListenerList) {
				try {
					l.accept(string);
				} catch(Exception x) {
					System.err.println(getClass().getSimpleName() + ": exception in listener: " + x);
				}
			}
		});
		LineCollectingOutputStream cerr = m_capturingError = new LineCollectingOutputStream(stderr, consoleCharset, string -> {
			for(ConsumerEx<String> l : m_stderrListenerList) {
				try {
					l.accept(string);
				} catch(Exception x) {
					System.err.println(getClass().getSimpleName() + ": exception in listener: " + x);
				}
			}
		});

		//-- Swap streams.
		System.out.println(getClass().getSimpleName() + ": capturing stdout and stderr.");
		m_stdout = System.out;
		m_stderr = System.err;
		System.setErr(new PrintStream(cerr));
		System.setOut(new PrintStream(cout));
		m_started = true;
	}

	/**
	 * Return the original, unwrapped out.
	 */
	static public synchronized PrintStream out() {
		if(INSTANCE.m_started) {
			return Objects.requireNonNull(INSTANCE.m_stdout);
		}
		return System.out;
	}

	private synchronized void stop() {
		if(!m_started)
			return;

		//-- Un-capture
		PrintStream stdout = m_stdout;
		if(null != stdout)
			System.setOut(stdout);
		PrintStream stderr = m_stderr;
		if(null != stderr)
			System.setErr(stderr);
		m_started = false;
		System.out.println(getClass().getSimpleName() + ": released stdout and stderr, capture terminated.");
	}

	public static StdoutListener getInstance() {
		return INSTANCE;
	}

	public void addStdoutListener(ConsumerEx<String> l) {
		m_stdoutListenerList.add(l);
	}

	public void addStderrListener(ConsumerEx<String> l) {
		m_stderrListenerList.add(l);
	}

	public void removeStdoutListener(ConsumerEx<String> l) {
		m_stdoutListenerList.remove(l);
	}
	public void removeStderrListener(ConsumerEx<String> l) {
		m_stderrListenerList.remove(l);
	}
}
