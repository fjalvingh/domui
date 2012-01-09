package to.etc.util;

import java.util.*;


/**
 * This class helps decoding command lines where a command line is a string. It
 * parses the string and generates a string array from all it's components, then
 * it is able to ask all kinds of questions about the string.
 * It accepts quoted arguments and presents them as a whole as one argument.
 * The principal use of this class is from within the TelnetServer where it is
 * used to parse Telnet commands.
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 *
 */
public class CmdStringDecoder {
	/// The input string variable.
	private String	m_cmd;

	/// The vector containing all strings.
	private Vector	m_argv;

	private int		m_ix;

	private int		m_sl;

	public CmdStringDecoder(String cmd) {
		decode(cmd);
	}

	//	private void skipSpaces()
	//	{
	//		while(m_ix < m_sl)
	//		{
	//			char c = m_cmd.charAt(m_ix);
	//			if(c == ' ' || c == '\t')
	//				m_ix++;
	//			else
	//				return;
	//		}
	//	}

	private String getWord() {
		while(m_ix < m_sl) {
			char c = m_cmd.charAt(m_ix);
			if(c == ' ' || c == '\t')
				m_ix++;
			else
				break;
		}

		char sc = m_cmd.charAt(m_ix);
		if(sc == '"' || sc == '\'') {
			//-- Collect a string.
			m_ix++;
			int sp = m_ix;

			while(m_ix < m_sl) {
				if(m_cmd.charAt(m_ix) == sc)
					break;

				m_ix++;
			}
			String rv = m_cmd.substring(sp, m_ix);
			m_ix++;
			return rv;
		}

		//-- Collect a word.
		int sp = m_ix;
		while(m_ix < m_sl) {
			char c = m_cmd.charAt(m_ix);
			if(c == ' ' || c == '\t')
				break;
			m_ix++;
		}
		return m_cmd.substring(sp, m_ix);
	}

	private void initSlicer(String cmd) {
		m_cmd = cmd;
		m_ix = 0;
		m_sl = cmd.length();
	}


	private void decode(String cmd) {
		m_argv = new Vector();
		initSlicer(cmd);
		while(m_ix < m_sl) {
			String c = getWord();
			m_argv.add(c.trim());
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Accessing the command. 								*/
	/*--------------------------------------------------------------*/
	/// The current argument..
	private int	m_curr_ix;


	public boolean hasMore() {
		return m_curr_ix < m_argv.size();
	}

	public void reset() {
		m_curr_ix = 0;
	}

	public String getCurr() {
		if(!hasMore())
			return "";
		return (String) m_argv.elementAt(m_curr_ix);
	}

	public String getNext() {
		if(!hasMore())
			return "";
		String v = getCurr();
		m_curr_ix++;
		return v;
	}


	/**
	 *	Returns T if the current part is...
	 */
	public boolean currIs(String txt) {
		if(!hasMore())
			return false; // Past end.

		int ix = 0;
		int len = txt.length();
		while(ix < len) {
			String name;
			int pos = txt.indexOf('|', ix);
			if(pos == -1) {
				name = txt.substring(ix);
				ix = len;
			} else {
				name = txt.substring(ix, pos);
				ix = pos + 1;
			}

			String a = getCurr();
			if(name.endsWith("*")) {
				String m = name.substring(0, name.length() - 1);
				if(a.length() > m.length())
					a = a.substring(0, m.length());
				if(a.equalsIgnoreCase(m)) {
					m_curr_ix++;
					return true;
				}
			}
			if(name.equalsIgnoreCase(a)) {
				m_curr_ix++;
				return true;
			}
		}
		return false;
	}

	public int currInt() {
		if(!hasMore())
			throw new IllegalArgumentException("Missing integer argument");
		try {
			return Integer.parseInt(getCurr().trim());
		} catch(Exception x) {
			throw new IllegalArgumentException("Missing or bad integer argument");
		}
	}

	public boolean currIsInt() {
		if(!hasMore())
			return false;
		try {
			Integer.parseInt(getCurr().trim());
			return true;
		} catch(Exception x) {
			return false;
		}
	}


}
