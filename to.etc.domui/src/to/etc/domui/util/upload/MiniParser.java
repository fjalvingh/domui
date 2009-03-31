package to.etc.domui.util.upload;

import java.util.*;

/**
 * Small parser which can decode the "parameter=value; param=value" format used
 * by the upload format.
 *
 * <p>Created on Nov 21, 2005
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 */
public class MiniParser {
	private String m_str;

	private int m_ix;

	private int m_len;

	private String m_property;

	private String m_value;

	public MiniParser() {}

	public final String getProperty() {
		return m_property;
	}

	public final void setProperty(String property) {
		m_property = property;
	}

	public final String getValue() {
		return m_value;
	}

	public final void setValue(String value) {
		m_value = value;
	}

	public void init(String in) {
		m_str = in;
		m_ix = 0;
		m_len = in.length();
	}

	public Map<String, String> parse(String in, boolean lcnames) {
		m_str = in;
		m_ix = 0;
		m_len = in.length();
		Map<String, String> m = new HashMap<String, String>();
		while(parseNext())
			m.put(lcnames ? getProperty().toLowerCase() : getProperty(), getValue());
		return m;
	}

	private String getWord() {
		int sp = m_ix; // Start pos of word
		while(m_ix < m_len) {
			char c = m_str.charAt(m_ix);
			if(Character.isWhitespace(c) || c == ';' || c == '=')
				break;
			m_ix++;
		}
		return m_str.substring(sp, m_ix); // Return the fragment read
	}

	private String getQuoted() {
		char qc = m_str.charAt(m_ix++); // Get quote
		int ss = m_ix;
		while(m_ix < m_len) {
			char c = m_str.charAt(m_ix++);
			if(c == qc)
				return m_str.substring(ss, m_ix - 1);
		}
		//-- Missing end quote-> just return what's left.
		return m_str.substring(ss);
	}

	/**
	 * Parses the next item at the location. It skips any separator then looks for
	 * a property=value part.
	 * @return
	 */
	public boolean parseNext() {
		//-- 1. Skip whitespace and separators
		char c = 0;
		while(m_ix < m_len) {
			c = m_str.charAt(m_ix);
			if(!Character.isWhitespace(c) && c != ';')
				break;
			m_ix++;
		}
		if(m_ix >= m_len)
			return false;

		//-- Must be at word boundary. Get possibly quoted word.
		if(c == '\'' || c == '\"')
			m_property = getQuoted();
		else
			m_property = getWord();
		m_value = null;

		//-- Next must be '=' or the value is not present
		while(m_ix < m_len) {
			c = m_str.charAt(m_ix);
			if(!Character.isWhitespace(c))
				break;
			m_ix++;
		}
		if(m_ix >= m_len)
			return true;

		if(c != '=')
			return true;
		m_ix++;

		while(m_ix < m_len) {
			c = m_str.charAt(m_ix);
			if(!Character.isWhitespace(c))
				break;
			m_ix++;
		}
		if(m_ix >= m_len)
			return true;

		if(c == '\'' || c == '\"')
			m_value = getQuoted();
		else
			m_value = getWord();
		return true;
	}
}
