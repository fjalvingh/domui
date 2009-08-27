package to.etc.template;

/**
 * This class reads the input stream and returns substrings for the tokens
 * found...
 *
 * <p>Title: Mumble Global Libraries - Non-database tools</p>
 * <p>Description: Small tools for Java programs</p>
 * <p>Copyright: Copyright (c) 2002 Frits Jalvingh; released under the LGPL licence.</p>
 * <p>Website <a href="http://www.mumble.to/">Mumble</a></p>
 * @author <a href="mailto:jal@mumble.to">Frits Jalvingh</a>
 * @version 1.0
 */
public class ExpReader {
	static public final int	tvEof		= -1;

	static public final int	tvLit		= 1;

	static public final int	tvExpand	= 2;

	static public final int	tvLoop		= 3;

	static public final int	tvEndLoop	= 4;

	static public final int	tvIfNot		= 5;

	static public final int	tvEndIfNot	= 6;

	static public final int	tvIf		= 7;

	static public final int	tvEndIf		= 8;


	/// The string being parsed,
	private String			m_buf;

	/// The current position in the thing,
	private int				m_pos;

	/// The current token's value.
	private String			m_tokval;

	/// The current token: tvLit, tvExpand tvLoop.
	private int				m_token;

	private ExpReader() {
	}

	public ExpReader(String s) {
		m_pos = 0;
		m_buf = s;
	}

	public boolean atend() {
		return m_pos >= m_buf.length();
	}

	private int getc() {
		if(atend())
			return -1;
		return m_buf.charAt(m_pos++);
	}

	public int currToken() {
		return m_token;
	}

	static private boolean isIdStartChar(int c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '/' || c == '_';
	}

	static private boolean isIdChar(int c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_';
	}

	/**
	 *	Skips spaces.
	 */
	private void skipSpaces() {
		//-- Skip spaces,
		while(true) {
			int c = getc();
			if(c == -1)
				return;
			if(c != '\n' && c != ' ' && c != '\t')
				break;
		}
		m_pos--;
	}

	static private int getTokenCode(String name) {
		if(name.equalsIgnoreCase("loop"))
			return tvLoop;
		else if(name.equalsIgnoreCase("/loop"))
			return tvEndLoop;
		else if(name.equalsIgnoreCase("if"))
			return tvIf;
		else if(name.equalsIgnoreCase("/if"))
			return tvEndIf;
		else if(name.equalsIgnoreCase("ifnot"))
			return tvIfNot;
		else if(name.equalsIgnoreCase("/ifnot"))
			return tvEndIfNot;
		else
			return -1;
	}


	/**
	 *	Moves to the 1st non-space char and tries to collect a name. Returns
	 *	the name, of the null string if no name was found. In that case the
	 *	read position is restored to the initial position.
	 */
	private String scanName() {
		int sp = m_pos; // Save start position.
		int c;

		//-- Now c contains something meaningful one would guess...
		c = getc();
		if(!isIdStartChar(c)) {
			m_pos = sp;
			return null;
		}

		int isp = m_pos - 1;
		while(true) {
			c = getc();
			if(!isIdChar(c)) {
				//-- Done. Return the completed thingo.
				m_pos--;
				String rv = m_buf.substring(isp, m_pos);
				return rv;
			}
		}
	}

	/**
	 *	Collects the next token.
	 */
	public int nextToken() {
		m_token = getNext();

		//		System.out.println("token: ->"+m_tokval+"<-");
		return m_token;
	}

	public String getValue() {
		return m_tokval;
	}

	/**
	 *	Collects the next token.
	 */
	private int getNext() {
		int c;

		c = getc(); // Get current character,
		if(c == '$') // Expand token?
			return getExpandToken();
		else if(c == '<') // Possible LOOP token?
			return checkForTag();
		else if(c == -1)
			return tvEof;
		m_pos--;
		return getLiteral();

	}

	public int getPos() {
		return m_pos;
	}

	private int getLiteral() {
		//-- Literal. Collect till marker found;
		int spos = m_pos;
		int c;
		while(true) {
			c = getc();
			if(c == '$') {
				int nc = getc();
				if(nc == '(') {
					//-- Expand token found. Return current token;
					m_pos -= 2; // Back to dollar sign;
					m_tokval = m_buf.substring(spos, m_pos);
					return tvLit;
				}
				m_pos--; // Backspace,
			} else if(c == '<') // Tag?
			{
				int sp = m_pos; // Save current pos,
				skipSpaces();
				String n = scanName();
				if(n != null) {
					//-- Is this the LOOP keywd?
					int tc = getTokenCode(n);
					if(tc != -1) {
						//-- End found!
						m_pos = sp - 1; // Back to <
						m_tokval = m_buf.substring(spos, m_pos);
						return tvLit;
					}
				}

				//-- Not a token. Just continue.
			} else if(c == -1) {
				m_tokval = m_buf.substring(spos, m_pos);
				return tvLit;
			}
		}
	}


	private int getExpandToken() {
		//-- Get the name directly after the token;
		int cpos = m_pos - 1; // Point to dollar,
		int c = getc();
		if(c == '(') {
			//-- Scan all till closing ).
			for(;;) {
				c = getc();
				if(c == ')') {
					//-- Done!!
					m_tokval = m_buf.substring(cpos + 2, m_pos - 1);
					return tvExpand;
				} else if(c == -1)
					break;
			}
		}

		//-- Just return as a literal.
		m_pos = cpos + 2;
		m_tokval = m_buf.substring(cpos, cpos + 2);
		return tvLit;
	}

	private int checkForTag() {
		//-- Get the name directly after the token..
		int spos = m_pos;
		String name = scanName();
		if(name != null) {
			int tc = getTokenCode(name);
			if(tc != -1) {
				//-- This is a tag. Get everything till > or eof.
				int ts = m_pos;
				while(true) {
					int c = getc();
					if(c == -1 || c == '>')
						break;
				}

				m_tokval = m_buf.substring(ts, m_pos - 1);
				return tc;
			}
		}

		//-- Not anything special.
		m_tokval = m_buf.substring(spos - 1, m_pos);
		return tvLit;
	}


}
