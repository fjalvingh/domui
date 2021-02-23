package to.etc.exceptionscanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.function.SupplierEx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class receives "line" events from one of the streams (only stdout or only stderr)
 * and runs a state machine that recognizes exceptions. Lines that are not part of an
 * exception are stored as context and shared between both implementations so that
 * we can add stdout/stderr context line that appear before an exception.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Feb 4, 2014
 */
@NonNullByDefault
final class ExceptionRecognizer {

	public static final int MAX_CONTEXT_LINES = 20;

	private Consumer<DiscoveredExceptionData> m_exceptionSink;

	private List<SupplierEx<String>> m_contextProviders;

	private interface IState {
		void handleLine(String segment) throws Exception;
	}

	/**
	 * Once we're recognizing exception(s) this will collect each exception line.
	 */
	final private StringBuilder m_exceptionBuilder = new StringBuilder();

	/**
	 * The thread that was "current" once the 1st exception line was received.
	 */
	@Nullable
	private Thread m_exceptionThread;

	/**
	 * The dump of context stdout lines before the exception was logged.
	 */
	private StringBuilder m_contextBuilder = new StringBuilder();

	@Nullable
	private Date m_startDate;

	private List<String> m_contextBuffer = new ArrayList<>(MAX_CONTEXT_LINES);

	public ExceptionRecognizer(Consumer<DiscoveredExceptionData> exceptionSink, List<SupplierEx<String>> contextProviders) {
		m_exceptionSink = exceptionSink;
		m_contextProviders = contextProviders;
	}

	/**
	 * This receives new lines and pushes them through the state machine.
	 */
	public void lineAdded(String line) {
		try {
			m_state.handleLine(line);
		} catch(Exception x) {
			x.printStackTrace(StdoutListener.out());
		}
	}

	/**
	 * Append the line as context to the context collection buffer.
	 */
	private void appendContext(String line) {
		synchronized(this) {
			m_contextBuffer.add(line);
			while(m_contextBuffer.size() > MAX_CONTEXT_LINES) {
				m_contextBuffer.remove(0);
			}
		}
	}

	private void d(String msg) {
//		StdoutScanner.out().println("d" + this + ": " + msg);
	}

	/**
	 * Initial state: we are not inside an exception, but perhaps this line is the start
	 * of one. Try to recognize an exception start line; if it is not then pass the line
	 * upwards to the "context collector" as not an exception line.
	 *
	 * <p>An exception start line has the following rules:
	 * <ul>
	 *	<li>The line starts without whitespace</li>
	 *	<li>The first part, ending in either colon or eoln contains only letters, digits and dots (it is a java class name like java.lang.NullPointerException).</li>
	 *	<li>The name of that first part ends in Exception or Error.</li>
	 * </ul>
	 * If these rules hold we enter EXCEPTION mode and start collecting lines. If not we add this line as CONTEXT.
	 *
	 */
	private final IState S_INITIAL = new IState() {
		@Override
		public void handleLine(String line) throws Exception {
			if(!isExceptionStart(line)) {
				d("initial: not exception start");
				appendContext(line);
			} else {
				d("initial: start of exception");
				startException(line);
			}
		}
	};

	/**
	 * We are part of an exception's lead-in. We are looking for "      at xx.yyy.xxx(nnn)" lines that
	 * define the stack trace. All lines before that are still part of the exception message.
	 */
	private final IState S_INEXCEPTION = new IState() {
		@Override
		public void handleLine(String line) throws Exception {
			if(isExceptionLocation(line)) {
				d("inexception: got location");
				startExceptionLocation(line);
			} else if(isExceptionStart(line)) {
				d("inexception: exception inside exception");
				clearException();							// False start
				startException(line);
			} else {
				//-- Just add as exception message text.
				d("inexception: message text");
				addExceptionText(line);
			}
		}
	};

	/**
	 * We are parsing "    at xxxxx" lines. If this is another one of those just
	 * add it to the collection and stay in this state. If the line is something
	 * like "caused by:" then move back to INEXCEPTION mode because we will
	 * have another exception message.
	 * If neither of those then the exception is finished -> flush it to the
	 * handler thread.
	 */
	private final IState S_PARSELOCATION = new IState() {
		@Override
		public void handleLine(String line) throws Exception {
			if(isExceptionLocation(line)) {
				d("parselocation: is location");
				addExceptionText(line);
			} else if(isCausedBy(line)) {
				d("parselocation: caused-by");
				startCausedBy(line);
			} else {
				//-- Exception is done! Flush it
				d("parselocation: end of exc, flushing");
				flushException();
				startInitial(line);
			}
		}
	};


	/*--------------------------------------------------------------*/
	/*	CODING:	Handler helpers										*/
	/*--------------------------------------------------------------*/
	/**
	 * Collecting the exception is done. Dump it in the completed-exception collector
	 * then clear everything for the next exception.
	 */
	private void flushException() {
		Thread thread;
		String exception;
		String context;
		Date date;
		synchronized(this) {
			if(m_exceptionBuilder.length() == 0)
				return;
			thread = m_exceptionThread;
			exception = m_exceptionBuilder.toString();
			context = m_contextBuilder.toString();
			date = m_startDate;
			clearException();
		}
		if(thread != null && date != null) {
			m_exceptionSink.accept(new DiscoveredExceptionData(thread, date, exception, context));
		}
	}

	/**
	 * Called to clear the exception data, so that the state becomes "no exception".
	 */
	private synchronized void clearException() {
		m_exceptionThread = null;
		m_exceptionBuilder.setLength(0);
		m_contextBuilder.setLength(0);
		m_startDate = null;
	}

	/**
	 * The first line of a possible exception stack trace has been found. We will now collect
	 */
	private void startException(String line) {
		flushException();								// Any earlier exception should be flushed

		synchronized(this) {
			m_exceptionThread = Thread.currentThread();
			m_startDate = new Date();
			m_state = S_INEXCEPTION;
			addExceptionText(line);

			//-- Add stdout context text
			for(String l : m_contextBuffer) {
				m_contextBuilder.append(l).append('\n');
			}

			for(SupplierEx<String> contextProvider : m_contextProviders) {
				try {
					String s = contextProvider.get();
					if(null != s)
						m_contextBuilder.append(s).append("\n");
				} catch(Exception x) {
					StdoutListener.out().println(getClass().getSimpleName() + ": context provider exception: " + x);
				}
			}
		}

		//try {
		//	StringBuilder sb = new StringBuilder();
		//	IRequestContext rc = UIContext.getRequestContext();
		//	if(null != rc) {
		//		RequestContextImpl r = (RequestContextImpl) rc;
		//		String s = r.getInputPath();
		//		sb.append("Request path: ").append(s).append("\n");
		//
		//
		//
		//	}
		//
		//	m_contextBuilder.append(sb);
		//} catch(Exception x) {
		//}
	}

	private void startCausedBy(String line) {
		m_state = S_INEXCEPTION;						// After a caused by we have another exception message
		addExceptionText(line);
	}

	private synchronized void addExceptionText(String line) {
		if(m_exceptionBuilder.length() > 32768) {
			//-- Too much crap in one exception. Just clear and reset,
			m_state = S_INITIAL;
			clearException();
			return;
		}
		m_exceptionBuilder.append(line.toString()).append('\n');
	}

	private void startInitial(String line) throws Exception {
		m_state = S_INITIAL;
		m_state.handleLine(line);
	}

	/**
	 * Got an exception location.
	 */
	private void startExceptionLocation(String line) {
		m_state = S_PARSELOCATION;
		addExceptionText(line);
	}

	static private final String B_EXCEPTION = "Exception";

	static private final String B_ERROR = "Error";

	static private final String B_AUSEDBY = "aused by:";

	static private final String B_DOTDOT = "...";

	/**
	 * T if this is an exception start signature:
	 * <ul>
	 *	<li>The line starts without whitespace</li>
	 *	<li>The first part, ending in either colon or eoln contains only letters, digits and dots (it is a java class name like java.lang.NullPointerException).</li>
	 *	<li>The name of that first part ends in Exception or Error.</li>
	 * </ul>
	 * If these rules hold we enter EXCEPTION mode and start collecting lines. If not we add this line as CONTEXT.
	 */
	static private boolean isExceptionStart(String line) {
		if(line.length() < 10)
			return false;
		int end = line.length();

		int dots = 0;

		int i = 0;
		while(i < end) {
			char c = line.charAt(i);
			if(c == ':')
				break;
			else if(c == '.')
				dots++;
			else if(c != '$' && c != '_' && !Character.isLetterOrDigit(c))				// Must be valid java name.
				return false;
			i++;
		}
		if(dots < 2)
			return false;

		//-- Seems to be valid. Does it end in Exception or Error?
		int fl = i;
		if(fl < B_EXCEPTION.length())
			return false;
		if(compare(line, i - B_EXCEPTION.length(), B_EXCEPTION) || compare(line, i - B_ERROR.length(), B_ERROR)) {
			return true;
		}
		return false;
	}

	/**
	 * T if this is an exception line of the form: "        at [class](location)"
	 * or a line like "    ... 26 more"
	 *
	 * , which formally is:
	 * <pre>
	 *
	 *
	 * 	at-line ::= ' '+ "at" ' '+ dotted-name '(' file-name optional-line ')'
	 *
	 *	dotted-name ::= letter [ letter | digit | '.' | '$' | '_' ]*
	 *
	 *	file-name ::= letter [ ^ ')' ]*
	 *
	 *	optional-line ::= | ':' integer
	 * </pre>
	 * In addition we require the name class name/function to have at least 2 dots.
	 */
	static boolean isExceptionLocation(String line) {
		if(line.length() < 10)
			return false;
		int end = line.length();

		//-- skip ws.
		int i = 0;
		while(i < end) {
			char c = line.charAt(i);
			if(c != ' ' && c != '\t')
				break;
			i++;
		}
		if(i == 0)
			return false;

		int len = end - i;
		if(len < 5)
			return false;

		//-- If we have '...' accept it
		if(compare(line, i, B_DOTDOT))
			return true;

		//-- require the word "at"
		if(line.charAt(i++) != 'a')
			return false;
		if(line.charAt(i++) != 't')
			return false;

		//-- Skip whitespace
		int np = i;
		while(i < end) {
			char c = line.charAt(i);
			if(c != ' ' && c != '\t')
				break;
			i++;
		}
		if(i == np)							// No ws after "at" -> no go
			return false;

		//-- Dotted-name
		int ndots = 0;
		while(i < end) {
			char c = (char) line.charAt(i);
			if(c == '.')
				ndots++;
			else if(!Character.isLetterOrDigit(c) && c != '$' && c != '_' && c != '/')		// Yes, lambda's contain slashes in the stacktrace. Sigh.
				break;
			i++;
		}
		if(ndots < 2)
			return false;

		//-- Now must have '('
		len = end - i;
		if(len < 2) {
			return false;
		}
		if(line.charAt(i++) != '(')
			return false;

		//-- Find end ')'
		for(;;) {
			if(i >= end)
				return false;
			if(line.charAt(i) == ')')
				return true;
			i++;
		}
	}

	/**
	 * T if the line starts with: [C|c]aused by:
	 */
	static private boolean isCausedBy(String line) {
		if(line.length() < 10)
			return false;

		//-- skip ws.
		int i = 0;
		int end = line.length();
		for(;;) {
			if(i >= end)
				return false;
			int c = line.charAt(i);
			if(c != ' ' && c != '\t')
				break;
			i++;
		}
		char c = line.charAt(i);
		if(c != 'C' && c != 'c')
			return false;
		i++;
		if(!compare(line, i, B_AUSEDBY))
			return false;
		return true;
	}


	static private boolean compare(String data, int dataoffset, String target) {
		if(dataoffset + target.length() > data.length())
			return false;
		for(int i = 0; i < target.length(); i++, dataoffset++) {

			if(data.charAt(dataoffset) != target.charAt(i))
				return false;
		}
		return true;
	}

	public static void main(String[] args) {
		String test = "     at java.lang.Long.parseLong(Long.java:419)\n";

		if(! isExceptionLocation(test))
			System.err.println("FAILED");
	}



	private volatile IState m_state = S_INITIAL;
}
