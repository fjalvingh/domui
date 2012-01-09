package to.etc.server.syslogger;

import java.io.*;

import to.etc.util.*;

/**
 *
 *
 * @author jal
 * Created on Jan 21, 2005
 */
public class Panicker {
	static private final String	FN1	= "panic.properties";

	static private final String	FN2	= "broker.properties";

	static private PanicHandler	m_inst;

	private Panicker() {
	}

	static public synchronized PanicHandler getInstance() {
		if(m_inst == null) {
			PanicHandler ph = new PanicHandler();
			try {
				File f = StringTool.findFileOnEnv(FN1, "java.class.path");
				if(f == null)
					f = StringTool.findFileOnEnv(FN2, "java.class.path");
				//				if(f == null)
				//					f = findFileUpwards(FN1);
				//				if(f == null)
				//					f = findFileUpwards(FN2);
				ConfigFile cf;
				ConfigSource cs = null;
				if(f != null) {
					File bf = new File(f.toString() + ".local");
					ConfigFile bcf = null;
					if(bf.exists()) {
						bcf = new ConfigFile();
						bcf.setFile(bf);
					}
					cf = new ConfigFile(bcf);
					cf.setFile(f);
					cs = cf;
				} else {
					cs = new ConfigSource() {
						public Object getSourceObject() {
							return "no-file";
						}

						public String getOption(String key) {
							return null;
						}

						public ConfigSource getSubSource(String key) {
							return this;
						}
					};
				}
				ph.init(cs);
				m_inst = ph;
			} catch(Exception x) {
				x.printStackTrace();
			}
		}
		return m_inst;
	}


	/**
	 *	Sends a panic email. The message is sent only when
	 */
	static public void panic(Class cl, String subject, String body) {
		panic(subject, body);
	}

	/**
	 *	Sends a panic email. If too many panics have been sent within a given
	 *  time the message is silently ignored and just written to the log.
	 */
	static public void panic(String subject, String body, boolean email) {
		getInstance().panic(subject, body, email);
	}

	static public void mailPanic(String subj, String body) {
		getInstance().mailPanic(subj, body);
	}

	/**
	 *	Sends a panic email. If too many panics have been sent within a given
	 *  time the message is silently ignored and just written to the log.
	 */
	static public void panic(String subject, String body) {
		getInstance().panic(subject, body);
	}

	static public void panic(String subject, Throwable t) {
		getInstance().panic(subject, t);
	}

	static public void panic(String subject, Throwable t, boolean email) {
		getInstance().panic(subject, t, email);
	}

	static public void logUnexpected(String s) {
		getInstance().logUnexpected(s);
	}

	static public void logUnexpected(Throwable t, String s) {
		getInstance().logUnexpected(t, s, null);
	}
}
