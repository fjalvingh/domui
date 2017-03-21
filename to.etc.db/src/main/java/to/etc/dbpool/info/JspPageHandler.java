/*
 * DomUI Java User Interface - shared code
 * Copyright (c) 2010 by Frits Jalvingh, Itris B.V.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * See the "sponsors" file for a list of supporters.
 *
 * The latest version of DomUI and related code, support and documentation
 * can be found at http://www.domui.org/
 * The contact for the project is Frits Jalvingh <jal@etc.to>.
 */
package to.etc.dbpool.info;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import javax.script.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import to.etc.dbpool.*;

/**
 * Helper class to reduce the horrible code in pool.jsp, without having to copy multiple
 * parts.
 *
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2010
 */
final public class JspPageHandler {
	private JspWriter m_out;

	private HttpServletRequest m_request;

	/** If a pool= parameter was present this is the resolved pool. */
	private ConnectionPool m_pool;

	private String m_message;

	private String m_jspname;

	private String m_url;

	private int m_timeout = 0;

	private boolean m_error;

	private boolean m_odd;

	private GlobalPerformanceStore m_globalStore;

	private SessionStatistics m_sessionStats;

	public JspPageHandler(JspWriter out, HttpServletRequest request, String jspname) {
		m_out = out;
		m_request = request;
		m_jspname = jspname;
		m_globalStore = StatisticsRequestListener.getGlobalStore();
		m_sessionStats = StatisticsRequestListener.getSessionStatistics(request);
	}

	public JspWriter getOut() {
		return m_out;
	}

	public HttpServletRequest getRequest() {
		return m_request;
	}

	private String getParam(String name) {
		String s = getRequest().getParameter(name);
		if(s == null)
			return null;
		s = s.trim();
		if(s.length() == 0)
			return null;
		return s;
	}

	public ConnectionPool getPool() {
		if(null == m_pool) {
			addError("The command requires the pool= parameter and a valid pool id");
			return null;
		}

		return m_pool;
	}

	public String getMessage() {
		return m_message;
	}

	public void addMessage(String msg) {
		m_timeout = 5;
		m_url = m_jspname;
		if(m_message == null)
			m_message = msg;
		else {
			m_message = m_message + "<br>" + msg;
		}
	}

	public void addError(String msg) {
		addMessage(msg);
		m_error = true;
	}

	/**
	 * Locate the specified method name inside this class, then call it.
	 * @param name
	 */
	private void executeHandler(String name) throws Exception {
		Method m;
		try {
			m = getClass().getMethod(name);
		} catch(Exception x) {
			throw new IllegalStateException("Unknown pool display task: " + name);
		}

		try {
			m.invoke(this);
		} catch(InvocationTargetException x) {
			if(x.getCause() instanceof Exception)
				throw (Exception) x.getCause();
			throw x;
		}
	}

	static private String niceName(String in) {
		if(in == null || in.length() == 0)
			return null;

		StringBuilder sb = new StringBuilder();
		char c = in.charAt(0);
		sb.append(Character.toUpperCase(c));
		sb.append(in.substring(1).toLowerCase());
		return sb.toString();
	}

	/**
	 * Decode all parameters and define the action to take.
	 */
	public void initialize() throws Exception {
		//-- If a pool id is present- decode it.
		String s = getRequest().getParameter("pool");
		if(null != s) {
			m_pool = PoolManager.getInstance().getPool(s);
			if(null == m_pool) {
				addError("The database pool " + s + " is not found.");
				return;
			}
		}
		m_url = m_jspname;

		//-- First execute any ACTIONS - state changes and stuff.
		s = getParam("action");
		if(null != s) {
			executeHandler("action" + niceName(s));
			if(m_error)
				return;
		}
		if(m_timeout == 0 && getParam("show") == null) {
			m_timeout = 30;
		}
	}

	public void displayPage() throws Exception {
		if(getMessage() != null) {
			expandTemplate("jspMessage");
			return;
		}

		//-- Now handle the appropriate SHOW commands.
		String s = getParam("show");
		if(s == null)
			showIndex();
		else
			executeHandler("show" + niceName(s));
	}


	/**
	 * Called from page to generate the refresh tag if needed.
	 * @throws IOException
	 */
	public void generateRefresh() throws IOException {
		if(m_timeout > 0) {
			tag("meta", "http-equiv", "refresh", "content", m_timeout + ";URL=" + m_url);
			finishOpenTag();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Simple html writer code.							*/
	/*--------------------------------------------------------------*/
	private boolean m_intag;

	/**
	 * Generate an html tag and any optional attributes.
	 * @param name
	 * @param attr
	 * @throws IOException
	 */
	public void tag(String name, String... attr) throws IOException {
		finishOpenTag();
		m_intag = true;
		getOut().append("<").append(name);
		if((attr.length & 0x1) != 0)
			throw new IllegalStateException("Usage error: need even set of attribute name/value.");

		for(int i = 0; i < attr.length; i += 2) {
			String an = attr[i];
			String av = attr[i + 1];
			attr(an, av);
		}
	}

	public void etag(String name) throws IOException {
		finishOpenTag();
		getOut().append("</").append(name).append(">");
	}

	public JspPageHandler full(String name, String... attr) throws IOException {
		tag(name, attr);
		finishOpenTag();
		return this;
	}

	public JspPageHandler text(String text) throws IOException {
		getOut().append(DbPoolUtil.q(text));
		return this;
	}

	public JspPageHandler tagText(String tag, String text, String... attr) throws IOException {
		full(tag, attr);
		text(text);
		etag(tag);
		return this;
	}

	public JspPageHandler nl() throws IOException {
		getOut().append("\n");
		return this;
	}

	public JspPageHandler attr(String name, String value) throws IOException {
		if(!m_intag)
			throw new IllegalStateException("Usage error: need to be in tag.");
		getOut().append(" ").append(name).append("=\"");
		value = value.replace("\"", "&quot;");
		getOut().append(value).append("\"");
		return this;
	}

	private void finishOpenTag() throws IOException {
		if(m_intag) {
			m_intag = false;
			getOut().append(">");
		}
	}

	public void h1(String text) throws IOException {
		tagText("h1", text);
	}

	private String urlBuilder(String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(m_jspname);
		for(int i = 0; i < args.length; i += 2) {
			sb.append(i == 0 ? "?" : "&");
			sb.append(args[i]);
			sb.append('=');
			sb.append(args[i + 1]);
		}
		return sb.toString();
	}

	public void actionTag(ConnectionPool pool, String action, String text) throws IOException {
		String url = urlBuilder("pool", pool.getID(), "action", action);
		tagText("a", text, "href", url);
	}

	public void showTag(ConnectionPool pool, String action, String text) throws IOException {
		String url = urlBuilder("pool", pool.getID(), "show", action);
		tagText("a", text, "href", url);
	}

	public void atag(String contents, String... args) throws IOException {
		tagText("a", contents, "href", urlBuilder(args));
	}

	public void backlink() throws IOException {
		tagText("a", "<--- Back to the main page", "href", m_jspname);
	}

	public String odd() {
		m_odd = !m_odd;
		return m_odd ? "odd" : "even";
	}

	public long now() {
		return System.currentTimeMillis();
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Action handlers.									*/
	/*--------------------------------------------------------------*/
	/**
	 * Switch on tracing if disabled.
	 * @throws Exception
	 */
	public void actionTraceon() throws Exception {
		ConnectionPool pool = getPool();
		if(null == pool)
			return;
		addMessage("Use tracing for pool " + pool.getID() + " enabled.");
		pool.dbgSetStacktrace(true);
	}

	public void actionTraceoff() throws Exception {
		ConnectionPool pool = getPool();
		if(null == pool)
			return;
		addMessage("Use tracing for pool " + pool.getID() + " disabled.");
		pool.dbgSetStacktrace(false);
	}

	public void actionErrorson() throws Exception {
		ConnectionPool pool = getPool();
		if(null == pool)
			return;
		addMessage("Error logging for pool " + pool.getID() + " enabled.");
		pool.setSaveErrors(true);
	}

	public void actionErrorsoff() throws Exception {
		ConnectionPool pool = getPool();
		if(null == pool)
			return;
		addMessage("Error logging for pool " + pool.getID() + " disabled.");
		pool.setSaveErrors(false);
	}

	public void actionSessionon() throws Exception {
		addMessage("Session statistics collection has been ENABLED. Do not forget to disable to prevent out-of-memory errors!!!");
		StatisticsRequestListener.createSessionStats(getRequest());
	}

	public void actionSessionoff() throws Exception {
		addMessage("Session statistics collection has been DISABLED and data has been destroyed.");
		StatisticsRequestListener.destroySessionStats(getRequest());
	}

	public void actionForceexpired() throws Exception {
		ConnectionPool pool = getPool();
		if(null == pool)
			return;
		addError("Forced hanging connection expiry for pool " + pool.getID());
		pool.scanExpiredConnections(120, true);
	}

	public void actionClearglobal() throws Exception {
		if(m_globalStore != null) {
			m_globalStore.clear();
			addMessage("Global statistics cleared");
		} else
			addMessage("?? Statistics not enabled??");
	}

	private String readResource(String name) throws IOException {
		InputStream is = null;
		try {
			File inf = new File("/home/jal/bzr/trunk-newpool/shared/to.etc.db/src/to/etc/dbpool/info/" + name);
			if(inf.exists()) {
				is = new FileInputStream(inf);
			} else {
				is = getClass().getResourceAsStream(name);
			}
			if(null == is)
				throw new IllegalStateException("Unknown classpath-resource " + name);
			InputStreamReader isr = new InputStreamReader(is, "utf-8");
			char[] data = new char[1024];
			StringBuilder sb = new StringBuilder();

			int szrd;
			while(0 < (szrd = isr.read(data))) {
				sb.append(data, 0, szrd);
			}
			return sb.toString();
		} finally {
			try {
				if(null != is)
					is.close();
			} catch(Exception x) {}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Template expansion.									*/
	/*--------------------------------------------------------------*/
	private ScriptEngine m_scripter;

	private ScriptEngine getScripter() throws Exception {
		if(m_scripter == null) {
			ScriptEngineManager em = new ScriptEngineManager();
			m_scripter = em.getEngineByName("JavaScript");
			Bindings	b = m_scripter.getBindings(ScriptContext.ENGINE_SCOPE);
			b.put("this", this);
			b.put("out", getOut());
			b.put("pool", m_pool);
			b.put("self", this);

			m_scripter.eval("load(\"nashorn:mozilla_compat.js\")");
			m_scripter.eval("importPackage(Packages.to.etc.dbpool)");
		}
		return m_scripter;
	}

	/**
	 * Expand a fragment template.
	 * @param name
	 * @param values
	 * @throws IOException
	 */
	public void expandTemplate(String name, Object... values) throws Exception {
		name = name + ".html";
		String resource = readResource(name);
		getScripter().getBindings(ScriptContext.ENGINE_SCOPE).put(ScriptEngine.FILENAME, name);
		expandTemplateString(resource, values);
	}

	/**
	 * Expand a template. Javascript code is between <% and %> delimiters.
	 * @param resource
	 * @param values
	 * @throws Exception
	 */
	private void expandTemplateString(String resource, Object[] values) throws Exception {
		boolean hascode = false;
		int ix = 0;
		int len = resource.length();
		while(ix < len) {
			int mark = resource.indexOf("<%", ix);
			if(mark == -1) {
				//-- Just append the last segment, then be done
				getOut().append(resource, ix, len);
				return;
			}

			//-- New replacement. Copy current part;
			getOut().append(resource, ix, mark);
			mark += 2;

			int emark = resource.indexOf("%>", mark);
			if(emark == -1) {
				getOut().append("<%"); // Missing terminator- just copy input
				ix = mark;
			} else {
				ix = emark + 2;
				boolean asexpr = false;
				if(resource.charAt(mark) == '=') {
					asexpr = true;
					mark++;
				}

				String code = resource.substring(mark, emark);

				//-- Initialize the engine if needed
				ScriptEngine se = getScripter();
				if(!hascode) {
					for(int i = 0; i < values.length; i += 2) {
						String name = (String) values[i];
						Object value = values[i + 1];
						se.put(name, value);
					}
					hascode = true;
				}

				//-- Make the thingy evaluate this
				Object res = se.eval(code);
				if(asexpr && res != null) {
					getOut().append(res.toString());
				}
			}
		}
	}

	public void expandTemplate2(String name, Object... values) throws Exception {
		name = name + ".html";
		String resource = readResource(name);
		ScriptEngine se = getScripter();
		se.getBindings(ScriptContext.ENGINE_SCOPE).put(ScriptEngine.FILENAME, name);
		for(int i = 0; i < values.length; i += 2) {
			String vname = (String) values[i];
			Object value = values[i + 1];
			se.put(vname, value);
		}
		JsTemplater tp = new JsTemplater();
		String exp = tp.replaceTemplate(resource); // Translate to javascript
		try {
			se.eval(exp);
		} catch(Exception x) {
			System.out.println("**** Javascript Template error ****\nJavascript:\n");
			System.out.println(exp);
			System.out.println("Error: " + x);
			throw x;
		}
	}

	public void write(String s) throws IOException {
		getOut().append(s);
	}

	public void writeExpr(Object o) throws IOException {
		if(null == o)
			return;
		write(String.valueOf(o));
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Show commands - providing actual output.			*/
	/*--------------------------------------------------------------*/

	/**
	 * Root page code- show all available pools.
	 * @throws IOException
	 */
	public void showIndex() throws Exception {
		ConnectionPool[] par = PoolManager.getInstance().getPoolList();
		if(null != m_globalStore) {
			tagText("a", "Show global statistics", "href", urlBuilder("show", "globalstats"));
			text("\u00a0\u00a0\u00a0");
			if(null == StatisticsRequestListener.getSessionStatistics(getRequest())) {
				atag("Enable session tracing", "action", "sessionon");
			} else {
				atag("Show session trace", "show", "session");
				text("\u00a0\u00a0\u00a0");
				atag("Disable session tracing", "action", "sessionoff");
				text("\u00a0\u00a0\u00a0");
			}
			text("\u00a0\u00a0\u00a0");

			atag("Disable statistics", "__statistics", "f");
		} else {
			text("Statistics disabled in database pool config. Click ");
			atag("here", "__statistics", "t");
			text(" to enable them.");
		}

		full("br");

		h1("All defined database pools - overview");
		displayPoolOverview(par);
	}

	/**
	 * Shows the pool overview fragment with pool counters.
	 * @param poolar
	 * @throws Exception
	 */
	private void displayPoolOverview(ConnectionPool[] poolar) throws Exception {
		expandTemplate("jspPoolOverview");
		boolean odd = false;
		for(ConnectionPool pool : poolar) {
			odd = !odd;
			String css = odd ? "oddpool" : "evenpool";

			//-- Calculate the dangles and get pool stats info
			PoolStats ps = pool.getPoolStatistics(); // Snapshot this pool.

			//-- Do we know about current dangles?
			int udangles = 0;
			int pdangles = 0;
			for(ConnectionProxy px : ps.getCurrentlyHangingConnections()) {
				if(px.isUnpooled())
					udangles++;
				else
					pdangles++;
			}

			expandTemplate("jspPoolEntry", "pool", pool, "udangles", udangles, "pdangles", pdangles, "ps", ps, "css", css);
		}
		etag("table");
	}

	private void displayPoolOverview(ConnectionPool pool) throws Exception {
		displayPoolOverview(new ConnectionPool[]{pool});
	}

	/**
	 * Displays all currently hanging connections as an expandable thingerydoo
	 * @throws Exception
	 */
	public void showHanging() throws Exception {
		ConnectionPool pool = getPool();
		if(pool == null)
			return;
		backlink();
		h1("Hanging connections for pool " + pool.getID());
		displayPoolOverview(pool);
		PoolStats ps = pool.getPoolStatistics();

		//-- Get a new list of still hanging conns
		List<ConnectionProxy> res = new ArrayList<ConnectionProxy>();
		for(ConnectionProxy px : ps.getCurrentlyHangingConnections()) {
			if(px.getState() != ConnState.OPEN)
				continue;
			res.add(px);
		}
		expandTemplate("jspHangingOverview", "pool", pool, "ps", ps);
		for(ConnectionProxy px : res) {
			expandTemplate2("jspHangingEntry", "pool", pool, "ps", ps, "px", px);
		}
		etag("table");
	}

	/**
	 * Displays all currently used connections as an expandable thingerydoo
	 * @throws Exception
	 */
	public void showUsed() throws Exception {
		ConnectionPool pool = getPool();
		if(pool == null)
			return;
		backlink();
		h1("Used connections for pool " + pool.getID());
		displayPoolOverview(pool);
		PoolStats ps = pool.getPoolStatistics();

		//-- Get a new list of still hanging conns
		List<ConnectionProxy> res = new ArrayList<ConnectionProxy>();
		for(ConnectionProxy px : pool.getUsedConnections()) {
			if(px.getState() != ConnState.OPEN)
				continue;
			res.add(px);
		}
		expandTemplate("jspUsedOverview", "pool", pool, "ps", ps);
		for(ConnectionProxy px : res) {
			expandTemplate2("jspUsedEntry", "pool", pool, "ps", ps, "px", px);
		}
		etag("table");
	}

	/**
	 * Show statistics page displaying the list of all available statistics.
	 * @throws Exception
	 */
	public void showGlobalstats() throws Exception {
		backlink();
		expandTemplate2("jspGlobalOverview", "list", m_globalStore.getLists());
	}

	public void showGloballist() throws Exception {
		backlink();
		full("br");
		atag("<--- Back to list of indicators", "show", "globalstats");
		full("br");
		String name = getParam("list");
		PerfList pl = m_globalStore.getList(name);
		List<PerfItem> iteml = m_globalStore.getItems(name);
		expandTemplate2("jspPerfList", "plist", pl, "items", iteml); // Header.

		expandTemplate2("perf-stmt-stmtcount", "plist", pl, "items", iteml);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Session-based tracing...							*/
	/*--------------------------------------------------------------*/
	/**
	 * Show the list of request traces in the session structure.
	 * @throws Exception
	 */
	public void showSession() throws Exception {
		if(m_sessionStats == null) {
			addError("No session data can be found.");
			return;
		}

		backlink();
		full("br");

		//-- Get template data
		List<SessionStatisticsEntry> entries = m_sessionStats.getRequests();
		expandTemplate2("jspSessionList", "list", entries, "session", m_sessionStats);
	}

	public void showSessionstat() throws Exception {
		if(m_sessionStats == null) {
			addError("No session data can be found.");
			return;
		}
		String id = getParam("id");
		if(null == id) {
			addError("Missing id");
			return;
		}
		SessionStatisticsEntry e = m_sessionStats.getEntry(id);
		if(null == e) {
			addError("Unknown request ID");
			return;
		}

		backlink();
		text("\u00a0\u00a0\u00a0");
		atag("<--- Back to request list", "show", "session");
		full("br");

		String key = getParam("list");
		PerfList plist = e.getStore().getList(key);
		expandTemplate2("jspListHeader", "id", id, "plist", plist, "item", e, "key", key);

		List<PerfItem> pitems = e.getStore().getItems(key);
		expandTemplate2("jspSessionEntry", "entry", e, "session", m_sessionStats, "plist", plist, "pitems", pitems);
	}
}
