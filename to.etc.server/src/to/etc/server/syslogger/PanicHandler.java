package to.etc.server.syslogger;

import java.io.*;
import java.util.*;

import to.etc.log.*;
import to.etc.smtp.*;
import to.etc.util.*;

/**
 * Handler for panics and log messages.
 *
 * @author jal
 * Created on Jan 22, 2005
 */
public class PanicHandler {
	static final Category	MSG	= LogMaster.getCategory("nema", "msg");

	static private boolean	m_onunix;

	/** The SMTP gateway for panic messages, */
	private String			m_panic_smtp;

	/** The source user line for SMTP messages. */
	private String			m_panic_from;

	/** The reply-to and from email address */
	private String			m_panic_src;

	/** The destination address list for panic SMTP messages. */
	private String			m_panic_addr;

	private SystemLogger	m_deflogger;

	public PanicHandler() {
		m_deflogger = getSystemLogger(SYSLOG_ID);
	}

	public PanicHandler(String def) {
		m_deflogger = getSystemLogger(def);
	}

	public void init(ConfigSource cs) throws Exception {
		ConfigSourceWrapper w = new ConfigSourceWrapper(cs);
		String logbase = w.getOption("broker.logdir", m_onunix ? "/tmp" : "c:/tmp/");
		m_syslog_dir_f = new File(logbase);
		m_panic_smtp = w.getOption("panic.smtp", "localhost");
		m_panic_from = w.getOption("panic.from", "NEMA Template server");
		m_panic_src = w.getOption("panic.replyaddr", "dontreply@asp-services.net");
		m_panic_addr = w.getOption("panic.to", "jal@mumble.to");
	}

	/**
	 *	Sends a panic email. The message is sent only when
	 */
	public void panic(Class cl, String subject, String body) {
		panic(subject, body);
	}


	/**
	 *	Sends a panic email. If too many panics have been sent within a given
	 *  time the message is silently ignored and just written to the log.
	 */
	public void panic(String subject, String body, boolean email) {
		logUnexpected(subject + "\n" + body);
		if(m_panic_smtp == null || m_panic_smtp.length() == 0)
			return;

		if(email)
			mailAnyone(m_panic_addr, subject, body);
	}

	public void mailPanic(String subj, String body) {
		mailAnyone(m_panic_addr, subj, body);
	}

	/**
	 *	Sends a panic email. If too many panics have been sent within a given
	 *  time the message is silently ignored and just written to the log.
	 */
	public void panic(String subject, String body) {
		panic(subject, body, true);
	}

	public void panic(String subject, Throwable t) {
		panic(subject, "Stack trace: " + StringTool.strStacktrace(t));
	}

	public void panic(String subject, Throwable t, boolean email) {
		panic(subject, "Stack trace: " + StringTool.strStacktrace(t), email);
	}


	/**
	 *	Takes a list of email adresses separated by spaces and adds all of 'm
	 *  to the message's recipient list.
	 */
	public void addMailRecipients(Message m, String list) {
		StringTokenizer st = new StringTokenizer(list, " \t");
		while(st.hasMoreTokens()) {
			String a = st.nextToken().trim();
			if(a.length() > 0) {
				m.addTo(new Address(a));
			}
		}
	}

	/**
	 *	Sends an EMAIL to anyone...
	 */
	public void mailAnyone(String recipientlist, String subject, String body) {
		try {
			MSG.msg("Mailing msg: " + subject);
			Message msg = new Message();
			msg.setFrom(new Address(m_panic_from, m_panic_src));
			addMailRecipients(msg, recipientlist);
			msg.setSubject("Panic: " + subject);
			msg.setBody(body == null ? subject : body);
			sendMessage(msg);
		} catch(Exception e) {
			logUnexpected("mailing failed BECAUSE " + e.toString() + ", message was " + subject);
		}
	}

	/**
	 *	Sends the message thru the SMTP server.
	 */
	public void sendMessage(Message m) throws Exception {
		SmtpTransport tr = new SmtpTransport(m_panic_smtp, 25);
		tr.send(m);
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Helpers for logging exceptions and the like...		*/
	/*--------------------------------------------------------------*/
	static private final String	SYSLOG_ID	= "system";

	private Hashtable			m_syslog_ht	= new Hashtable();

	private File				m_syslog_dir_f;

	public SystemLogger getSystemLogger(String name) {
		name = name.toLowerCase();
		SystemLogger sl = null;
		synchronized(m_syslog_ht) {
			sl = (SystemLogger) m_syslog_ht.get(name);
			if(sl == null) {
				sl = new SystemLogger(m_syslog_dir_f, name);
				m_syslog_ht.put(name, sl);
			}
		}
		sl.open();
		return sl;
	}

	/**
	 *	This logs an unexpected exception to the broker log. This can be used
	 *  to centrally track exceptions.
	 */
	public void logUnexpected(Throwable x, String what, Object oo) {
		m_deflogger.logUnexpected(x, what, oo);
	}

	/**
	 *	This logs an unexpected exception to the broker log. This can be used
	 *  to centrally track exceptions.
	 */
	public void logUnexpected(String what) {
		m_deflogger.logUnexpected(what);
	}

	static public boolean isUnix() {
		return m_onunix;
	}

	static {
		m_onunix = File.separatorChar == '/';
	}
}
