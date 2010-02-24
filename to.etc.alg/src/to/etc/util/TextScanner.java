package to.etc.util;

/**
 * Small helper class to scan text strings for expected tokens.
 * @author jal
 * Created on May 21, 2004
 */
public class TextScanner {
	/** The string being scanned */
	private String	m_text;

	/** The length of the input string */
	private int		m_len;

	/** The current position within the string */
	private int		m_ix;

	private long	m_lastint;

	private StringBuilder	m_sb	= null;

	public TextScanner() {
	}
	public TextScanner(String s) {
		setString(s);
	}

	public long getLastInt() {
		return m_lastint;
	}

	public void setString(String s) {
		m_text = s;
		m_ix = 0;
		m_len = s.length();
		clear();
	}

	public void clear() {
		if(m_sb != null)
			m_sb.setLength(0);
	}

	public String getCopied() {
		return m_sb == null ? "" : m_sb.toString();
	}

	public StringBuilder getBuffer() {
		return sb();
	}

	public long getInt() {
		return m_lastint;
	}

	/**
	 * Skips whitespace at the current position. Returns TRUE if whitespace was
	 * found.
	 * @return
	 */
	public boolean skipWS() {
		int six = m_ix;
		while(m_ix < m_len) {
			char c = m_text.charAt(m_ix);
			if(!Character.isWhitespace(c))
				break;
			m_ix++;
		}
		return m_ix != six;
	}

	public boolean eof() {
		return m_ix >= m_len;
	}

	/**
	 * Scans for an integer number at the current location. Returns T if a
	 * number was found. The value for that number can be obtained with a
	 * call to getLastInt()
	 * @return
	 */
	public boolean scanInt() {
		long res = 0;
		int ndig = 0;
		while(m_ix < m_len) {
			char c = m_text.charAt(m_ix);
			if(!Character.isDigit(c))
				break;
			res = res * 10 + (c - '0');
			ndig++;
			m_ix++;
		}
		if(ndig == 0)
			return false; // No digits -> no number
		m_lastint = res;
		return true;
	}

	/**
	 * Scans for a generic "word", which is defined as a sequence of
	 * characters starting with a letter, followed by letters and/or
	 * digits and the special characters '$' and '_'.
	 * @return The scanned string, or null if no string found here.
	 */
	public String scanWord() {
		int six = m_ix; // Start index for word
		while(m_ix < m_len) {
			char c = m_text.charAt(m_ix);
			if(six == m_ix) // First letter?
			{
				if(!Character.isLetter(c))
					return null;
			} else {
				if(!Character.isLetterOrDigit(c) && c != '$' && c != '_')
					break;
			}
			m_ix++;
		}
		return m_text.substring(six, m_ix);
	}

	public String scanDelimited(String delimiters, int quote1, int quote2) {
		int inq = -1;
		if(m_ix >= m_len)
			return null; // eof

		int c = (m_text.charAt(m_ix) & 0xffff);
		if(c == quote1 || c == quote2) {
			inq = c;
			m_ix++;
		}
		int six = m_ix; // Start index for word
		while(m_ix < m_len) {
			c = (m_text.charAt(m_ix++) & 0xffff);
			if(inq != -1) {
				if(c == inq) {
					return m_text.substring(six, m_ix - 1);
				}
			} else {
				for(int i = delimiters.length(); --i >= 0;) {
					if(delimiters.charAt(i) == c) {
						m_ix--;
						return m_text.substring(six, m_ix);
					}
				}
			}
		}
		return m_text.substring(six, m_ix);
	}

	/**
	 * Scans for a word consisting only of letters.
	 * @return
	 */
	public String scanLetters() {
		int six = m_ix; // Start index for word
		while(m_ix < m_len) {
			char c = m_text.charAt(m_ix);
			if(!Character.isLetterOrDigit(c) && c != '$' && c != '_')
				break;
			m_ix++;
		}
		if(six == m_ix)
			return null;
		return m_text.substring(six, m_ix);
	}

	/**
	 * Return the next char and advance the ptr.
	 * @return
	 */
	@Deprecated
	public int nextChar() {
		if(m_ix >= m_len)
			return -1;
		return m_text.charAt(m_ix++) & 0xffff;
	}

	@Deprecated
	public boolean skip(char c) {
		if(m_ix >= m_len)
			return false;
		if((c & 0xffff) != m_text.charAt(m_ix))
			return false;
		m_ix++;
		return true;
	}

	@Deprecated
	public char currentChar() {
		return m_ix >= m_len ? 0 : m_text.charAt(m_ix);
	}

	@Deprecated
	public void inc() {
		m_ix++;
	}

	public int LA() {
		return m_ix >= m_len ? -1 : (0xffff & m_text.charAt(m_ix));
	}

	public int LA(int x) {
		return m_ix + x >= m_len ? -1 : (0xffff & m_text.charAt(m_ix + x));
	}

	public void accept() {
		m_ix++;
	}

	public StringBuilder sb() {
		if(m_sb == null)
			m_sb = new StringBuilder();
		return m_sb;
	}

	public int length() {
		return m_len;
	}

	public int index() {
		return m_ix;
	}

	public void setIndex(int ix) {
		m_ix = ix;
	}

	public void accept(int ct) {
		m_ix += ct;
	}

	public void copy() {
		if(m_ix < m_len) {
			sb().append(m_text.charAt(m_ix++));
		}
	}

	public void copy(StringBuilder sb) {
		if(m_ix < m_len) {
			sb.append(m_text.charAt(m_ix++));
		}
	}

	public void copy(int n) {
		while(--n > 0)
			copy();
	}

	public void append(String s) {
		sb().append(s);
	}

	public void append(long i) {
		sb().append(Long.valueOf(i));
	}

	public void append(char c) {
		sb().append(c);
	}
}
