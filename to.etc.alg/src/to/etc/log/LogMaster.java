package to.etc.log;

import java.io.*;
import java.util.*;

import to.etc.util.*;


/**
 *	<p>This is the log singleton which is created only once for the entire session.
 *  It manipulates all log categories and contains the global logging entities
 *  like the system debug logfile (using the JLOG environment variable) and the
 *  Telnet server entrypoint.</p>
 *	<p>Code using the logging system create static entries of Categories. Each
 *  category has an unique name: the <i>log event name</i>. When different
 *  modules use the same log event name in creating a Category these refer to
 *  the same event(!). An example of such an event is the "msg" event.</p>
 *
 *	<h1>Using the logging classes</h1>
 *	<p>Using these classes is very easy. You generate output by calling functions
 *  from your code. To view the output you have several options, described in
 *  <i>enabling and viewing logging</i>, below.</p>
 *
 *	<h2>Generating messages from code</h2>
 *	To write debug logging a user class typically contains something like:
 * 	<pre>
 *  	public class myClass {
 *			static private final Category MSG = LogMaster.category("myclass", "msg");
 *          static private final Category DB = LogMaster.category("myclass", "db");
 *	</pre>
 *  and somewhere in the code, when a message must be written, one uses:
 *  <pre>
 *  	MSG.msg("Now opening...");
 *      DB.exception("Exception while opening database", x);
 *	</pre>
 *
 *	<p>The Category class will only log the message if it is enabled for a given
 *  output source. If a category has no output sources then it returns ASAP. All
 *  category message functions are thread-safe, and messages are logged in the
 *  order they are received in.</p>
 *
 *	<h2>Enabling logging</h2>
 *  <p>To enable logging you set an environment variable JLOG to the debug
 *  properties file. The debug properties file contains settings for the
 *  debug classes, and gets loaded as soon as the LogMaster class is first
 *  accessed. Without a JLOG environment variable logging can never be enabled.</p>
 *
 *  <p>The debug log file can contain the following commands:
 *  <ul><li>telnet=yes: when present, this enables the Telnet server. You can
 *  		access a host of commands and logging output by telnetting to the
 *      	machine running your program.</li>
 *     	<li>telnet.port={integer}: the port number to use for the telnet server.
 *      	The default value is 7777.</li>
 *     	<li>logfile={file-and-pathname}: creates a logfile with the spec'd name.</li>
 *      <li>logfile.wrap={true | false}: when true, a new logfile is generated
 *      	every day. The daynumber is appended to the logfile's name. This
 *          generates a max. of 7 files (monday to sunday); after seven days
 *          the first file is reused.</li>
 *    	<li>logfile.over={true | false}: when true, the logfile will be
 *      	overwritten as soon as it is created; else it will be appended to.</li>
 *      <li>logfile.filter={filter item}*: see filter item below; enables (+) or
 *      	disables (-) a given category in the log. Disabled items are not
 *          logged</li>
 *		<li>screen.filter={filter item}*: when present the items specified are
 *      	logged to the System.out thingy.</li>
 *  </ul>
 *  </p>
 *
 *	<h3>Filter items</h3>
 *  <p>Each category has a dotted name like <b>nema.resource.init</b>. You
 *  prevent messages from being logged by filtering on this name. A filter item
 *  starts with + or -, followed by a name pattern. The name pattern is something
 *  like *.msg to recognise all that ends on msg, or nema.* to recognise all
 *  that starts with nema.
 *  For + filters the category gets logged when the name matches. For - items the
 *  name gets NOT logged when matched.
 *	</p>
 *
 *	<h2>Using telnet to access your program</h2>
 *  <p>If you have started your program with a JLOG= environment variable pointing
 *  to a proper log.properties, and if that file contained telnet=yes, then you
 *  can access your program from anywhere by telnetting to it. For instance to
 *  access your running program on your machine you would enter the following
 *  command: <b>telnet localhost 7777</b>.</p>
 *
 * 	<p>The 7777 is the port number; you can change the default port number by
 * 	adding the telnet.port=xxx to the log.properties file.</p>
 *
 * 	<p>When in telnet you can issue commands. Just type and you get the "what?"
 *  prompt. While entering a command all output to the session is suspended, but
 *  the last 20 lines or so are cached while you type. When you press ENTER
 *  your command gets executed, then input mode is left and all cached lines are
 *  output. If you entered INPUT mode but you forget to press ENTER the system
 *  leaves INPUT mode after about 10 seconds. Your input can be resumed just by
 *  continuing typing where you left off.</p>
 *
 *	<p>To get a list of possible commands just type ?. You can easily add your
 *  own commands to this set to extend usability. @see to.mumble.log.iTelnetCommandHandler
 *  </p>
 *
 *
 *	<h2>Dumping messages to (a file)(telnet)(your option here)</h2>
 *  <p>Each Category has an attached list of <i>log event listeners</i>. When a
 *  category receives a message this message is sent to all it's event listeners.
 *  If a category has no registered event listeners then the category is
 *  <i>disabled</i>: no one is interested in it. This is the normal case when
 *  the system is not being debugged.</p>
 *
 *	<p>A log listener is a class implementing a special interface. Some special
 *  log listeners are the "global file logger", which reads the log.properties
 *  file denoted by the JLOG environment variable, and the Telnet session class
 *  which is used to interactively check logging information.</p>
 *
 *	<p>To optimize the flow of messages the system tries to prevent having to
 *  do a lot of work for messages that are not logged. This is done by having
 *  a high-level, per-category "enabled" flag that is set ONLY if there is a
 *  listener for a category. If you want the Telnet server to receive all
 *  messages you need to set the parameter telnet=all in the log.properties file,
 *  or you need to issue the TELNET command <b>LOG telnet *</b>. This
 *  causes all categories to have their enabled flag set to T, and causes all
 *  log messages to be buffered in the telnet server in a circular list. This
 *  allows you to see the last messages.</p>
 *
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 *
 */
public class LogMaster implements Runnable {
	static private final String	LOGMASTER_KEY	= "LogMaster";

	//	/// False when logging is not active.
	//	private boolean 	m_logging	= false;
	//
	//	/// T if the MSG category was explicitly set.
	//	private boolean		m_msg_set	= false;

	/// True if an exception has been reported and logging has failed already.
	private boolean				m_failed		= false;

	/// The list of log writers
	protected iLogWriter		m_lw;

	//	private Category		m_msg_cat;

	private LogMaster() {
		//initFromEnv();
	}

	//	static public LogMaster	get()
	//	{
	//		return m_ref;
	//	}

	//	static public boolean isActive()
	//	{
	//		return m_ref.m_logging;
	//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Panic logger...										*/
	/*--------------------------------------------------------------*/
	/**
	 *	Logs a message to a file "logmaster.panic".
	 */
	static public synchronized void panic(String eh, Exception x) {
		OutputStream os = null;
		PrintWriter pw = null;
		try {
			os = new FileOutputStream("logmaster.panic", true);
			pw = new PrintWriter(os);

			pw.println("-- to.mumble.log.LogMaster panic -------------------");
			pw.println("At " + (new Date()).toString());
			pw.println("Reason: " + eh);
			if(x != null) {
				pw.println("Exception: " + x.toString());
				x.printStackTrace(pw);
			}
		} catch(Exception xyz) {} finally {
			try {
				if(pw != null)
					pw.close();
			} catch(Exception x1) {}
			try {
				if(os != null)
					os.close();
			} catch(Exception x2) {}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Log event listeners.								*/
	/*--------------------------------------------------------------*/
	/// The list of registered event listeners.
	static private Vector	m_listeners_v	= new Vector(10);


	/**
	 *	Registers a listener. After registration all categories are presented
	 *  to the listener so that it can specify which categories it requires.
	 */
	static public void addListener(iLogEventListener lel) {
		synchronized(m_cat_ht) {
			if(m_listeners_v.contains(lel))
				return; // Already registered
			m_listeners_v.add(lel); // Add to all registered thingies.

			//-- Ask the listener whether it is interested in any of the categories.
			Enumeration e = m_cat_ht.elements();
			while(e.hasMoreElements()) {
				Category c = (Category) e.nextElement();
				if(lel.isInterestedIn(c)) // Is interested?
					c.addListener(lel); // Then add!!
			}
		}
	}


	/**
	 *	Removes a listener. No categories will call the listener after this call
	 *  completes.
	 */
	static public void removeListener(iLogEventListener lel) {
		synchronized(m_cat_ht) {
			m_listeners_v.remove(lel); // Remove from list,

			//-- Now remove from all categories.
			Enumeration e = m_cat_ht.elements();
			while(e.hasMoreElements()) {
				Category c = (Category) e.nextElement();
				c.removeListener(lel);
			}
		}
	}


	/**
	 *	Called when a listener wants to reassess its filter. This call causes
	 *  all existing links between the listener and it's categories to be
	 *  removed, and calls isInterested again for all known categories.
	 */
	static public void refreshListener(iLogEventListener lel) {
		synchronized(m_cat_ht) {
			//-- Now remove from all categories.
			Enumeration e = m_cat_ht.elements();
			while(e.hasMoreElements()) {
				Category c = (Category) e.nextElement();
				if(lel.isInterestedIn(c))
					c.addListener(lel);
				else
					c.removeListener(lel);
			}
		}
	}


	/**
	 *	Presents a new category to all registered listeners, so that they can
	 *  specify an interest in the event.
	 */
	static protected void presentToListeners(Category c) {
		//		System.out.println("LogMaster: presenting new category "+c.getName()+" to Event Listeners");

		synchronized(m_cat_ht) {
			//-- Ask the listener whether it is interested in any of the categories.
			Enumeration e = m_listeners_v.elements();
			while(e.hasMoreElements()) {
				iLogEventListener lel = (iLogEventListener) e.nextElement();
				if(lel.isInterestedIn(c)) // Is interested?
					c.addListener(lel); // Then add!!
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Categories..										*/
	/*--------------------------------------------------------------*/
	/// The hash table containing all categories,
	static private Hashtable	m_cat_ht	= new Hashtable();

	//	// The CURRENT value of the category counter.
	//	static private int			m_new_category = 0;

	/**
	 *	Defines and returns the category with the given name, If this category
	 *  already exists then it gets returned; else a new one is created.
	 */
	static public Category category(String module, String cat) {
		Category c;
		synchronized(m_cat_ht) {
			String name = module.toLowerCase() + "." + cat.toLowerCase();
			c = (Category) m_cat_ht.get(name);// Get category number,
			if(c != null)
				return c; // Category known-> return,

			//-- New one! Create it, then ask all event listeners if they want to know it,
			c = new Category(name); // Create a new'un,
			m_cat_ht.put(name, c); // Add to table,
		}
		presentToListeners(c);
		return c;
	}


	/**
	 *	Defines and returns the category with the given name, Please replace with
	 *  category(). This function does exactly the same!
	 *  deprecated
	 *  @see category(String module, String cat)
	 */
	static public Category getCategory(String module, String cat) {
		return category(module, cat);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Initializing from the properties file.				*/
	/*--------------------------------------------------------------*/
	//	/// The path for the log properties file, or NULL
	//	static private File			m_logprop_fn = null;

	/// The logging properties file's contents.
	static private Properties	m_log_p;

	/**
	 *	Reads the properties file and stores it into the m_log_p field.
	 */
	static private void readProperties(File f) throws Exception {
		//		m_logprop_fn	= f;
		InputStream is = new FileInputStream(f);
		try {
			m_log_p = new Properties();
			m_log_p.load(is);
		} finally {
			try {
				is.close();
			} catch(Exception x) {}
		}
	}


	/**
	 *	Called to initialize the log classes, this reads the file specified for
	 *  logging settings, initializes the log etc. If the file cannot be read
	 *  then an error is printed and we're done.
	 */
	static public void initialize(File f) {
		if(f == null)
			return;

		System.out.println("LogMaster: Initializing from file " + f);
		try {
			readProperties(f); // Read properties
			handleProperties(); // Do everything for them,
		} catch(Exception x) {
			System.err.println("LogMaster: error reading log properties: " + x.toString());
			System.err.println("LogMaster: file was " + f.toString());
		}
	}


	/**
	 *	Returns a boolean property's value.
	 */
	static private boolean getBoolProp(String name, boolean dflt) throws Exception {
		String v = m_log_p.getProperty(name);
		if(v == null)
			return dflt;
		if(v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equalsIgnoreCase("on"))
			return true;
		if(v.equalsIgnoreCase("false") || v.equalsIgnoreCase("no") || v.equalsIgnoreCase("off"))
			return false;
		throw new Exception("parameter " + name + " expects a boolean value");
	}

	/**
	 *	Returns an integer property's value.
	 */
	static private int getIntProp(String name, int dflt) throws Exception {
		String v = m_log_p.getProperty(name);
		if(v == null)
			return dflt;
		try {
			int rv = Integer.parseInt(v);
			return rv;
		} catch(Exception x) {
			throw new Exception("Parameter " + name + " expects an integer value");
		}
	}


	/**
	 *	Returns a list from a single property, as item,item,item etc. Typically
	 *  used in a Filter.
	 */
	static private LinkedList getListProp(String name) throws Exception {
		String v = m_log_p.getProperty(name);
		if(v == null)
			return null;

		//-- Tokenize & parse,
		StringTokenizer st = new StringTokenizer(v, ",\t ");

		LinkedList l = new LinkedList();
		while(st.hasMoreTokens()) {
			String t = st.nextToken();
			l.add(t);
		}
		return l;
	}


	/**
	 *	Handles the log.properties file.
	 */
	static synchronized public void handleProperties() throws Exception {
		//-- Is TELNET requested?
		if(getBoolProp("telnet", false)) // Telnet requested?
		{
			int tsp = getIntProp("telnet.port", 7777); // Telnet port,
			startTelnetServer(tsp); // Start, register and whatnot the server,
		}

		//-- Do we need logging to a file?
		String fn = m_log_p.getProperty("logfile"); // Get logfile name.
		if(fn != null) {
			//-- Get logfile mode (append, overwrite)
			boolean lmover = getBoolProp("logfile.overwrite", false);
			boolean lmwrap = getBoolProp("logfile.wrap", false);

			//-- And set the log output file.
			startLogFile(fn, lmover, lmwrap);
			LinkedList l = getListProp("screen.filter");
			if(l != null) {
				addListener(new ScreenEventWriterThing(l));
			}

		}

	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Telnet interface stuff...							*/
	/*--------------------------------------------------------------*/
	/// The Telnet server for the logger.
	static private TelnetServer			m_telnet_server;

	/// The logfile writer class which writes the global logfile.
	static private iLogEventListener	m_logfile_lel;


	/**
	 *	Called to start the telnet server. If the server has already
	 *  started this returns false.
	 */
	static public void startTelnetServer(int port) {
		synchronized(m_cat_ht) {
			if(m_telnet_server != null)
				return;
			System.out.println("LogMaster: starting telnet thingy on port " + port);
			try {
				m_telnet_server = TelnetServer.createServer(port);

				//-- Add the logmaster command handler.
				iTelnetCommandHandler lh = new iTelnetCommandHandler() {
					public boolean executeTelnetCommand(TelnetPrintWriter tpw, CmdStringDecoder commandline) throws Exception {
						return LogMaster.executeTelnetCommand(tpw, commandline);
					}
				};
				m_telnet_server.addCommandHandler(lh);

				//-- Add system.out handler...
				TelnetSysoutMirrorStream tsms = new TelnetSysoutMirrorStream(m_telnet_server, System.out);
				PrintStream ps = new PrintStream(tsms);
				System.setOut(ps);
				System.setErr(ps);
			} catch(Exception x) {
				System.err.println("LogMaster: startTelnet failed, " + x.toString());
				x.printStackTrace();
			}
		}
	}

	static public void registerTelnetCommand(iTelnetCommandHandler tch) {
		synchronized(m_cat_ht) {
			if(m_telnet_server == null)
				return;
			m_telnet_server.addCommandHandler(tch);
		}
	}


	/**
	 *	Retrieves or creates the telnetwriterthing for the session issuing a
	 *  command...
	 */
	static protected TelnetEventWriterThing getTnThing(TelnetPrintWriter ts) {
		TelnetEventWriterThing tet = (TelnetEventWriterThing) ts.getSession().get(LOGMASTER_KEY);
		if(tet == null) {
			tet = new TelnetEventWriterThing(ts);
			ts.getSession().put(LOGMASTER_KEY, tet);
			addListener(tet);
		}
		return tet;
	}


	static public void telnetLoggingEnable(TelnetPrintWriter ts, String cat, boolean remove) {
		TelnetEventWriterThing tet = getTnThing(ts);
		if(remove)
			tet.removeFilter(cat);
		else
			tet.addFilter(cat);
		refreshListener(tet);
	}


	static private final String	USAGE	= "log filter add (pattern): to add a category filter\n" + "log filter remove (pattern): removes a category filter\n" + "log filter list: lists all filters\n"
											+ "log cat: lists all currently known log categories\n" + "log tail file pagenr: Lists the last page from the file\n"
											+ "log off: disables all logging to this session";

	/**
	 *	Called to execute a single command for a Telnet client.
	 */
	static protected boolean executeTelnetCommand(TelnetPrintWriter ts, CmdStringDecoder cmd) throws Exception {

		if(cmd.currIs("?")) {
			ts.println(USAGE);
			return true;
		}
		if(!cmd.currIs("log"))
			return false;

		if(!cmd.hasMore() || cmd.currIs("?")) {
			ts.println(USAGE);
			return true;
		} else if(cmd.currIs("fil*")) {
			TelnetEventWriterThing tet = getTnThing(ts);
			if(cmd.currIs("add")) {
				while(cmd.hasMore()) {
					tet.addFilter(cmd.getNext());
				}
				refreshListener(tet);
				return true;
			} else if(cmd.currIs("rem*")) {
				while(cmd.hasMore()) {
					tet.removeFilter(cmd.getNext());
				}
				refreshListener(tet);
				return true;
			} else if(cmd.currIs("?") || cmd.currIs("list")) {
				tet.printFilters();
				return true;
			}
			return false;
		} else if(cmd.currIs("off")) {
			//-- Remove all filters.
			TelnetEventWriterThing tet = getTnThing(ts);
			tet.removeFilters();
			refreshListener(tet);
			ts.println("LogMaster: ALL logging disabled.");
			return true;
		} else if(cmd.currIs("cat")) {
			synchronized(m_cat_ht) {
				Enumeration e = m_cat_ht.elements();
				ts.println("Currently registered categories are:");
				while(e.hasMoreElements()) {
					Category cn = (Category) e.nextElement();
					ts.print(cn.getName());
					ts.print(", ");
				}
				ts.println("...");
			}
			return true;
		} else if(cmd.currIs("tail")) {
			String fn = cmd.getNext();
			String pnr = cmd.getNext();
			File f = new File(fn);
			if(!f.exists() || !f.isFile()) {
				ts.println(fn + ": doesn't exist/no file");
				return true;
			}

			int pagenr = 0;
			try {
				pagenr = Integer.parseInt(pnr);
			} catch(Exception x) {}

			filePageReverse(ts, f, pagenr, 15);
			return true;

		}

		return false;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Logfile stuff...									*/
	/*--------------------------------------------------------------*/
	static private boolean	m_log_all;

	static synchronized public boolean logAllEnabled() {
		return m_log_all;
	}

	/**
	 *	Starts the output logfile writer, if not already started.
	 */
	static synchronized private void startLogFile(String fn, boolean overwrite, boolean wrap) throws Exception {
		if(m_logfile_lel != null)
			throw new Exception("Logfile already defined.");

		//-- Get all filters for this & add to list,
		LinkedList l = getListProp("logfile.filter");
		m_logfile_lel = new LogFileEventWriterThing(fn, overwrite, wrap, l);
		addListener(m_logfile_lel);
	}


	static synchronized public void enableAll(boolean onoff) {
		if(m_log_all == onoff)
			return;

		m_log_all = onoff;
		if(m_logfile_lel == null)
			return;

		refreshListener(m_logfile_lel);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Loading the properties file & applying it's info	*/
	/*--------------------------------------------------------------*/

	//	private void dump()
	//	{
	//		Enumeration	e	= m_cat_ht.elements();
	//		while(e.hasMoreElements())
	//		{
	//			Category	c	= (Category) e.nextElement();
	//			System.out.println("log: Category "+c.getName()+" is "+c.m_on+", screen="+c.m_screen);
	//		}
	//	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Initialization.										*/
	/*--------------------------------------------------------------*/
	/**
	 *	Called when constructed. It checks if a logging environment variable
	 *  exists; if not it disables all logging.
	 */
	static private void initFromEnv() {
		String fn = System.getProperty("JLOG");
		if(fn == null) {
			fn = System.getProperty("JLOGPROP");
			if(fn == null)
				return;
		}

		//-- Envvar existed: use the filename to initialize further.
		initialize(new File(fn));
	}


	/**
	 *	Handles exceptions in the logging class. When called it disables all
	 *	logging and writes a message to stderr.
	 */
	synchronized public void doException(Exception x) {
		if(m_failed)
			return;
		m_failed = true;
		//		m_logging	= false;

		System.err.println("-- nl.nccwcasa.log ----------------------------------------------------------------");
		System.err.println("LogMaster: exception during log operation. Logging suspended");
		System.err.println("Exception: " + x.toString());
		x.printStackTrace(System.err);
		System.err.println("-----------------------------------------------------------------------------------");
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Handling log messages.								*/
	/*--------------------------------------------------------------*/
	/// The logger thread, if created,
	static private Thread		m_logger_thread;

	/// The instance of this class used as a Thread runnable interface (logger thread)
	static private LogMaster	m_logmaster_thingy;

	/// The LogRecord queue, if logging. This is the LOCK for all write/queue ops.
	static private LinkedList	m_pending_queue	= new LinkedList();

	/// T when some process is currently WRITING log messages.
	static private boolean		m_am_writing;


	/**
	 *	Accepts a LogRecord and causes it to be output. This function obeys the
	 *  ACTUAL order in which log records arrive, and this order is preserved
	 *  over all registered things for the log and the categories.
	 *  To prevent us from having to lock the entire logging process during all
	 *  message handling we spawn a Logger thread as soon as the #of messages
	 *  to log is too big.
	 *
	 *  Usually, if only one message needs to be generated this will be done
	 *  by the calling thread; if more than one message is pending then the
	 *  logger thread will do it.
	 */
	static public void log(LogRecord lr) {
		int qs = 0;
		synchronized(m_pending_queue) {
			qs = m_pending_queue.size();
			if(m_am_writing || qs > 0)
				m_pending_queue.addLast(lr);
			else {
				m_am_writing = true; // Ok: no-one is writing and queue's empty: I will write myself..
				qs = 0; // Signal "we write"
			}
		}

		//-- Outside the critical section: what do we have to do?
		if(qs > 500) // Too many queued messages?
		{
			System.out.println("LogMaster: SEVERE ERROR - Too many queued messages; blocking!!!");
			try {
				Thread.sleep(5000);
			} catch(Exception x) {}

			return;
		}
		if(qs != 0)
			return; // Be done if queued.

		//-- THIS THREAD has to Write a single record thru all layers,
		try {
			logRecordReally(lr);
		} catch(Throwable t) {
			System.err.println("LogMaster: UNEXPECTED throwable: " + t.getMessage());
			t.printStackTrace();
		}

		//-- Now: is there more to do? If so signal the logger thread & exit!
		synchronized(m_pending_queue) {
			m_am_writing = false; // We're done anyway!!!

			if(m_pending_queue.size() > 0) // Meanwhile more was queued?
				kickLoggerThread(); // Awake/start logger!!
		}
	}


	/**
	 *	Really logs a record. When called we're already certain that we're the
	 *  only one writing, so locking is NO LONGER NEEDED!!
	 */
	static private void logRecordReally(LogRecord lr) {
		synchronized(m_cat_ht) // Make sure no more listeners are added,
		{
			lr.m_cat.sendThruChain(lr);
		}
		logSink(lr);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Logger thread functions...							*/
	/*--------------------------------------------------------------*/
	/**
	 *	Called when a logger thread is needed to write messages. The Logger
	 *  thread is created if it's not already there, and it is awakened if it's
	 *  already there by signalling m_pending_queue.
	 */
	static private void kickLoggerThread() {
		synchronized(m_pending_queue) {
			if(m_logmaster_thingy != null) // Thread created?
			{
				m_pending_queue.notifyAll();
				return;
			}

			//-- Sheet! We need a logger thread!
			m_logmaster_thingy = new LogMaster(); // Create instance for run()
		}

		/*
		 *	We create the thread outside the critical region to prevent trouble
		 *	and long locks. We already know here's the only thread creating this.
		 */
		System.out.println("LogMaster: Creating logger thread.");
		m_logger_thread = new Thread(m_logmaster_thingy);
		m_logger_thread.setDaemon(true);
		m_logger_thread.setName("LogMasterDaemon");
		m_logger_thread.setPriority(Thread.MAX_PRIORITY);

		m_logger_thread.start();
	}


	/**
	 *	The logger thread's code. It waits will stuff becomes available in the
	 *  pending queue, and logs it sequentially. If the queue becomes empty it
	 *  will wait() on it.
	 */
	public void run() {
		System.out.println("LogMaster: logger thread started.");

		for(;;) // .. Forever..
		{
			LogRecord lr = null;

			//-- 1. Wait till a message arrives.
			synchronized(m_pending_queue) // Wait @ while...
			{
				while(m_pending_queue.isEmpty() || m_am_writing) {
					try {
						m_pending_queue.wait();
					} catch(Exception x) {}
				}

				//-- We have something at least and we have to handle it!
				m_am_writing = true;
				lr = (LogRecord) m_pending_queue.removeFirst();
			}

			//-- 2. Loop and write till the queue is empty.
			while(lr != null) // While record to do,
			{
				logRecordReally(lr); // Log the record,
				lr = null;

				synchronized(m_pending_queue) {
					//-- More to do?
					if(m_pending_queue.isEmpty())
						m_am_writing = false; // No: we're done!
					else
						lr = (LogRecord) m_pending_queue.removeFirst();
				}
			}
		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Small helper functions...							*/
	/*--------------------------------------------------------------*/

	/**
	 * Splits a name set into a set of strings, one string per component.
	 */
	static private String[] spliceName(String s) {
		int p, pp;

		pp = 0;

		Vector v = new Vector(10);
		while(pp < s.length()) {
			p = s.indexOf('.', pp);
			if(p == -1)
				p = s.length();
			v.add(s.substring(pp, p));
			pp = p + 1;
		}
		return (String[]) v.toArray(new String[v.size()]);
	}

	static private boolean subMatch(String wild, String name) {
		if(wild.length() == 0 || wild.charAt(0) == '*')
			return true;
		return wild.equalsIgnoreCase(name);
	}

	static private boolean matchNames(String wild, String act) {
		//		if(act.equalsIgnoreCase("nema.res.usage") && wild.equalsIgnoreCase("*.usage"))
		//			System.out.println("********* FOUND ************");
		if(wild.length() == 0 || act.length() == 0)
			return false;

		String[] vwild = spliceName(wild);
		String[] vname = spliceName(act);

		if(vname.length < vwild.length)
			return false; // Wild has more components than name

		return matchNames(vwild, 0, vname, 0); // Check names recursively
	}

	static private boolean matchNames(String[] vwild, int wix, String[] vact, int aix) {
		if(wix >= vwild.length) {
			return vact.length >= aix; // Matches if both wild and actual exhausted
		}

		if(aix >= vact.length)
			return false;

		//-- There's wild stuff left,
		if(!subMatch(vwild[wix], vact[aix])) // Do these DIRECT elements match?
			return false; // No - exit.

		//-- Do the rest of the slices match?
		if(vwild[wix].startsWith("*")) // This was a wild match?
		{
			//-- Try by reusing the wildcard,
			if(matchNames(vwild, wix, vact, aix + 1))
				return true;

			//-- And try by skipping the wildcard
			return matchNames(vwild, wix + 1, vact, aix + 1);

		}

		//-- This part was not wild. Skip both.
		return matchNames(vwild, wix + 1, vact, aix + 1);
	}


	/**
	 *	Called by listeners to determine interest by wildcard.
	 */
	static public boolean checkInterestedIn(Collection filter, String name) {
		if(filter == null)
			return true;

		//		Iterator i = filter.iterator();
		//		System.out.print("f("+name+") on ");
		//		while(i.hasNext())
		//		{
		//			String	f	= (String) i.next();
		//			System.out.print(f+" ");
		//		}
		//		System.out.println("...");
		//
		//
		Iterator i = filter.iterator();
		while(i.hasNext()) {
			String f = (String) i.next();
			if(f.length() > 0) {
				String wild;
				boolean invert = false;
				if(f.charAt(0) == '-') {
					invert = true;
					wild = f.substring(1);
				} else if(f.charAt(0) == '+')
					wild = f.substring(1);
				else
					wild = f;

				boolean matched = matchNames(wild, name);
				//				System.out.println("MATCH: wild="+wild+" to="+name+" is "+matched);
				if(matched) {
					return !invert;
				}
			}
		}
		return false;
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	text file dumper.									*/
	/*--------------------------------------------------------------*/
	static private long findLocationOfRevLine(RandomAccessFile raf, int lnr, byte[] buf) throws IOException {
		long pos = raf.length();
		int linenr = 0;

		while(pos > 0) {
			//-- Move to the PREVIOUS block,
			pos -= buf.length;
			if(pos < 0)
				pos = 0;

			//-- Move there and read an array full
			raf.seek(pos);
			int szrd = raf.read(buf);

			//-- Walk backwards thru the buffer and count lf's...
			while(szrd > 0) {
				szrd--;
				byte ch = buf[szrd];
				if(ch == '\n') {
					linenr++;
					if(linenr > lnr) {
						//-- We're there!
						return pos + szrd + 1;
					}
				}
			}

			//-- Shit. Move back more.
		}
		return 0;
	}


	/**
	 *	Reverse pages a file. Page 0 is the last lines of the file.
	 */
	static public void filePageReverse(PrintWriter pw, File fn, int pnr, int npp) throws Exception {
		//-- 1. Open a random file, and read backwards in 4K blocks...
		byte[] buf = new byte[4096];
		RandomAccessFile raf = new RandomAccessFile(fn, "r");
		try {
			int lnr = (pnr + 1) * npp;
			long pos = findLocationOfRevLine(raf, lnr, buf);
			if(pos == 0)
				pw.println("<<< beginning of file >>>");

			//-- Position at the thing then print,
			raf.seek(pos);

			while(npp > 0) {
				String l = raf.readLine();
				if(l == null) {
					pw.println("<<< eof >>>");
					break;
				}
				pw.println(l);
				npp--;
			}
		} finally {
			try {
				raf.close();
			} catch(Exception x) {}

		}
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Log/info records sink.								*/
	/*--------------------------------------------------------------*/
	/**
	 *	Called with any type of log message, this call will send the log or
	 *  message event to all global log handlers. The most important of this is
	 *  the Telnet server and the global debugging log file.
	 */
	static public void logSink(LogRecord lr) {
	}

	static {
		initFromEnv();
	}

}
