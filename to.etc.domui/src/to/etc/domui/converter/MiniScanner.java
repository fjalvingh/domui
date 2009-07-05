package to.etc.domui.converter;

public class MiniScanner {
	private String m_in;

	private int m_len;

	private int m_ix;

	private int m_val;

	public void init(String in) {
		m_in = in.trim();
		m_ix = 0;
		m_len = m_in.length();
	}

	/**
	 * Scan a number-delimiter pair.
	 * @return
	 */
	public char next() {
		skipWs();
		if(m_ix >= m_len)
			return 0;

		//-- Scan the number
		m_val = 0;
		char c = ' ';
		while(m_ix < m_len) {
			c = m_in.charAt(m_ix);
			if(!Character.isDigit(c))
				break;
			m_val = m_val * 10 + (c - '0'); // Implement in #
			c = ' ';
			m_ix++;
		}
		skipWs();
		if(m_ix < m_len) {
			c = m_in.charAt(m_ix);
			if(Character.isDigit(c))
				throw new IllegalStateException("invalid: # without separators.");
		} else
			c = 1;
		return c;
	}

	private void skipWs() {
		while(m_ix < m_len) {
			char c = m_in.charAt(m_ix);
			if(!Character.isWhitespace(c))
				return;
			m_ix++;
		}
	}

	public int val() {
		return m_val;
	}

	public long scanDuration(String in) {
		init(in);

		long res = 0;
		char c = next();
		if(c == 1) {
			//-- Lone #: is time in minutes,
			return val() * 60;
		}
		if(c == 'D' || c == 'd') {
			res = (long) val() * 24 * 60 * 60l;
			c = next();
		}
		if(c == ':') {
			res = val(); // Prime with 1st #
			c = next(); // Must be followed by AT LEAST ONE more
			if(c != ':' && c != 'D' && c != 'd' && c != 1)
				throw new IllegalStateException("Missing 2nd fraction after xx:");
			res = res * 60 + val();
			if(c == ':') {
				//-- It is hh:mm:ss format. Get seconds
				c = next();
				res = res * 60 + val();
				if(c == 1)
					return res;
				if(c != ' ')
					throw new IllegalStateException("Unexpected character " + c);
				c = next();
			} else if(c == ' ' || c == 'd' || c == 'D' || c == 1)
				res *= 60;
			return res;
		}
		throw new IllegalStateException("Unexpected character " + c);
	}

}
