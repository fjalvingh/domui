package to.etc.server.servlet.cmd;

import java.io.*;

import javax.servlet.http.*;

import to.etc.xml.*;

/**
 * The context of a command being executed.
 *
 * Created on Aug 23, 2005
 * @author jal
 */
abstract public class CommandContext {
	/** The servlet context for the command */
	private CommandServletContext	m_ctx;

	private XmlWriter				m_xw;

	private String					m_name;

	public CommandContext(CommandServletContext cc, String name) throws IOException {
		m_ctx = cc;
		m_xw = cc.getXmlWriter();
		m_name = name;
	}

	final public String getName() {
		return m_name;
	}

	public CommandServletContext getContext() {
		return m_ctx;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Reply generation helper functions.					*/
	/*--------------------------------------------------------------*/
	public XmlWriter getXmlWriter() {
		return m_xw;
	}

	public void xml(String s) throws IOException {
		getXmlWriter().cdata(s);
	}

	/**
	 * Writes a new XML tag. The attrs field must at least contain a '>' to close
	 * the tag. If you want the tag to be the only one on the line then add a
	 * linefeed to the attrs field also.
	 * @param tn			The tag name without braces
	 * @param attrs			attributes.
	 */
	public final void tag(String tn, String attrs) throws IOException {
		getXmlWriter().tag(tn, attrs);
	}

	public final void tagendnl() throws IOException {
		getXmlWriter().tagendnl();
	}

	public final void fulltag(String tag, String value) throws IOException {
		System.out.println("xml: fulltag=" + tag + ", value=" + value);
		getXmlWriter().tagfull(tag, value);
	}

	public final void fulltag(String tag, int value) throws IOException {
		getXmlWriter().tagfull(tag, value);
	}

	public final void fulltag(String tag, boolean val) throws Exception {
		getXmlWriter().tagfull(tag, val);
	}

	public final void fulltag(String tag, java.util.Date val) throws Exception {
		getXmlWriter().tagfull(tag, val);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Abstract to get parameters.							*/
	/*--------------------------------------------------------------*/
	abstract public String getStringParam(String name, String def) throws Exception;

	public long getLongParam(String name) throws Exception {
		String s = getStringParam(name, null);
		if(s != null) {
			try {
				return Long.parseLong(s);
			} catch(Exception x) {}
		}
		throw new Exception("Missing or invalid '" + name + "' parameter: it should be a long");
	}

	public int getIntParam(String name) throws Exception {
		String s = getStringParam(name, null);
		if(s != null) {
			try {
				return Integer.parseInt(s);
			} catch(Exception x) {}
		}
		throw new Exception("Missing or invalid '" + name + "' parameter: it should be an integer");
	}

	public long getLongParam(String name, long def) throws Exception {
		String s = getStringParam(name, null);
		if(s == null || s.trim().length() == 0)
			return def;
		try {
			return Long.parseLong(s);
		} catch(Exception x) {}
		throw new Exception("Missing or invalid '" + name + "' parameter: it should be a long or empty");
	}

	public int getIntParam(String name, int def) throws Exception {
		String s = getStringParam(name, null);
		if(s == null || s.trim().length() == 0)
			return def;
		try {
			return Integer.parseInt(s);
		} catch(Exception x) {}
		throw new Exception("Missing or invalid '" + name + "' parameter: it should be an integer or empty");
	}

	/**
	 * Used when a non-xml response is needed.
	 * @return
	 */
	public HttpServletResponse getResponse() {
		m_ctx.signalBinaryOutput();
		return m_ctx.getResponse();
	}
}
