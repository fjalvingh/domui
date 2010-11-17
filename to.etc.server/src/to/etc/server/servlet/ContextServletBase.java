package to.etc.server.servlet;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import to.etc.server.*;
import to.etc.server.servlet.cmd.*;
import to.etc.server.servlet.error.*;
import to.etc.util.*;

/**
 * Created on Feb 4, 2005
 * @author jal
 */
abstract public class ContextServletBase extends HttpServlet implements ILogSink {
	static private boolean		SHOWTS	= false;

	static private DateFormat	DATEF	= DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	abstract public ContextServletContext makeContext(HttpServletRequest req, HttpServletResponse res, boolean ispost);

	/*--------------------------------------------------------------*/
	/*	CODING:	Logging support.									*/
	/*--------------------------------------------------------------*/
	/** The logfile to log to, or null if no logfile needed */
	private RotatingLogfile	m_rlf;

	/**
	 * T if "getLastModified()" should be routed thru the context.
	 */
	private boolean			m_doesLastModified;

	protected ContextServletBase(boolean dolastmodified) {
		m_doesLastModified = dolastmodified;
	}

	/**
	 * Initialize the logfile. Should only be called from init().
	 * @param filename
	 */
	protected void setLogging(String filename) {
		if(m_rlf != null)
			throw new IllegalStateException("Logging already set");
		m_rlf = new RotatingLogfile(filename);
	}

	/**
	 * Returns Is the logfile enabled?
	 * @return
	 */
	public boolean isLogging() {
		return m_rlf != null;
	}

	/**
	 * Logs the string to the servlet's logfile, if present.
	 * @see javax.servlet.GenericServlet#log(java.lang.String)
	 */
	@Override
	public void log(String s) {
		if(m_rlf != null)
			m_rlf.log(s);
	}

	/**
	 * If we're logging log the exception to the log.
	 *
	 * @see to.etc.util.ILogSink#exception(java.lang.Throwable, java.lang.String)
	 */
	public void exception(Throwable t, String s) {
		if(m_rlf != null)
			m_rlf.exception(t, s);
	}

	public void initLogging(String logkey) throws ServletException {
		ConfigSource cs = ServerTools.getSharedConfigSource(getServletContext());
		String log = null;
		try {
			// mbp nov 2005: use logkey first, ir that one fails try "log"
			log = cs.getOption(logkey);
			if(log == null)
				log = cs.getOption("log");
			if(log != null) {
				System.out.println("Logfile of ContextServletBase is initialized to: " + log);
				setLogging(log);
			} else
				System.out.println("Logfile of ContextServletBase is NOT initialized due to minning property value for: " + logkey);
		} catch(Exception e) {
			e.printStackTrace();
			throw new ServletException("Exception in enabling logfile " + logkey + " to " + log, e);
		}
	}

	@Override
	public void init(ServletConfig cfg) throws ServletException {
		super.init(cfg);
		String logname = cfg.getInitParameter("logfile");
		if(logname != null)
			setLogging(logname);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Context init and termination handler registry		*/
	/*--------------------------------------------------------------*/
	private List	m_inithandler_l	= new ArrayList();

	private List	m_termhandler_l	= new ArrayList();

	static public interface InitHandler {
		public void handle(ContextServletContext ctx) throws Exception;
	}
	static public interface TermHandler {
		public void handle(ContextServletContext ctx) throws Exception;
	}


	protected void callInitHandlers(ContextServletContext ctx) throws Exception {
		for(int i = m_inithandler_l.size(); --i >= 0;) {
			((InitHandler) m_inithandler_l.get(i)).handle(ctx);
		}
	}

	protected void callTermHandlers(ContextServletContext ctx) {
		for(int i = m_termhandler_l.size(); --i >= 0;) {
			try {
				((TermHandler) m_termhandler_l.get(i)).handle(ctx);
			} catch(Exception e) {
				exception(e, "In termination context handler " + m_termhandler_l.get(i));
				e.printStackTrace();
			}
		}
	}

	public void addTermHandler(TermHandler h) {
		m_termhandler_l.add(h);
	}

	public void addInitHandler(InitHandler h) {
		m_inithandler_l.add(h);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Context maintenance.								*/
	/*--------------------------------------------------------------*/
	/** The requests hash connects requests with their actions. */
	private Map	m_req_ht	= new HashMap();

	/**
	 * Find out if the request has a context already; if so get it else allocate
	 * a new one.
	 * @param req
	 * @param res
	 * @param ispost
	 * @return
	 * @throws ServletException
	 */
	private ContextServletContext findContext(HttpServletRequest req, HttpServletResponse res, boolean ispost) {
		//-- Present in hash -> return,
		ContextServletContext c;
		synchronized(m_req_ht) {
			c = (ContextServletContext) m_req_ht.get(req);
			if(c != null && m_doesLastModified) {
				((LastModifiedHandler) c).setResponse(res, ispost);
				return c;
			}
			c = makeContext(req, res, ispost);
			m_req_ht.put(req, c);
		}
		c.initialize(); // Initialize the context
		return c;
	}

	/**
	 * Called when the context is no longer needed. All resources are released
	 * and the context is invalidated to be freed by GC.
	 * @param req
	 */
	protected void closeContext(HttpServletRequest req) {
		ContextServletContext ctx;
		synchronized(m_req_ht) {
			ctx = (ContextServletContext) m_req_ht.remove(req); // Find it AND remove it
			if(ctx == null)
				return; // If not there exit.
		}

		try {
			ctx.discard();
		} catch(Exception x) {
			x.printStackTrace();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		//		System.out.println("## doGet: "+req.getRequestURI());
		doAction(req, res, false);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		//		System.out.println("## doPost: "+req.getRequestURI());
		doAction(req, res, true);
	}

	/**
	 * Gets called when the browser only wants the document if it has changed
	 * after a date. This MUST locate the resource to generate and return it's
	 * modification date. If the resource cannot be found or if the resource
	 * is a template then this should return -1.
	 * @param req		the request.
	 * @return
	 */
	@Override
	protected long getLastModified(HttpServletRequest req) {
		if(!m_doesLastModified)
			return -1;

		//-- Allocate a context.
		long ts = PrecisionTimer.getTime();
		long tm = 0;
		StringBuffer sb = SHOWTS ? new StringBuffer(128) : null;
		try {
			ContextServletContext ctx = findContext(req, null, false); // Allocate,
			tm = ((LastModifiedHandler) ctx).getLastModified(); // Ask context
			if(sb != null) {
				sb.append("CC: ");
				sb.append(ServerTools.makeTimeString());
				sb.append(' ');
				String s = req.getRequestURI();
				if(s.length() > 0 && s.charAt(0) == '/')
					s = s.substring(1);
				sb.append(s);
				sb.append(' ');
				if(tm == -1)
					sb.append("NONE");
				else
					sb.append(DATEF.format(new java.util.Date(tm)));
			}
		} catch(Exception x) {
			//-- Need to fucking wrap the exception - assholes.
			//			handleException(req, null, x);
			throw new RuntimeException("Wrapped exception: " + x, x);
		} finally {
			if(sb != null) {
				ts = PrecisionTimer.getTime() - ts;
				sb.append(" in ");
				sb.append(Long.toString(ts));
				sb.append(" us");
			}
		}
		if(sb != null)
			System.out.println(sb.toString());
		return tm;
		//		System.out.println("getLastModified call: "+parm1);
		//		return TMPC.getTimeInMillis();
	}

	protected void doAction(HttpServletRequest request, HttpServletResponse response, boolean ispost) throws ServletException, IOException {
		//-- Create/locate the context
		ContextServletContext c = null;
		try {
			c = findContext(request, response, ispost);
			callInitHandlers(c);
			c.execute();
		} catch(Exception x) {
			if(handleException(request, response, x))
				return;
			throwWrappedException(x);
		} finally {
			if(c != null)
				callTermHandlers(c);
		}
	}

	private void throwWrappedException(Exception x) throws IOException, ServletException {
		if(x instanceof InvocationTargetException) // Unwrap InvocationTargetException
		{
			Throwable cause = x.getCause();
			if(cause instanceof Exception)
				x = (Exception) cause;
		}
		if(x instanceof RuntimeException)
			throw (RuntimeException) x;
		if(x instanceof ServletException)
			throw (ServletException) x;
		if(x instanceof IOException)
			throw (IOException) x;
		throw new WrappedException(x);
	}

	/**
	 * The default handleException uses the exception beautifier, if possible.
	 * @param x
	 * @return
	 */
	public boolean handleException(HttpServletRequest req, HttpServletResponse res, Exception x) {
		x.printStackTrace();
		try {

			if(ExceptionBeautifier.generateException(this, req, res, x))
				return true;
		} catch(Exception e) {
			System.out.println("While trying to beautify an exception: " + e);
			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws javax.servlet.ServletException, java.io.IOException {
		//		System.out.println("## service: "+req.getRequestURI());
		try {
			super.service(req, res);
		} finally {
			closeContext(req);
		}
	}
}
