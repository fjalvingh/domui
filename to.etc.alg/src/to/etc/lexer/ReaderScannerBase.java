package to.etc.lexer;

import java.io.*;

public class ReaderScannerBase {
	static public final int		T_EOF				= -1;

	static public final int		T_STRING			= -2;

	static public final int		T_NUMBER			= -3;

	static public final int		T_IPADDR			= -4;

	static public final int		T_IDENT				= -5;

	static public final int		T_COMMENT			= -6;

	static public final int		T_BASE_LAST			= -7;

	static private final int	MAX_QUEUE_LENGTH	= 10;

	/** An opague source object used for reporting the source "file" or whatever */
	private Object				m_src;

	private Reader				m_r;

	private boolean				m_eof;

	private int					m_lnr;

	private int					m_cnr;

	private int					m_token_lnr;

	private int					m_token_cnr;

	/** The round-robin lookahead buffer */
	private int[]				m_la_ar				= new int[MAX_QUEUE_LENGTH];

	private int					m_put_ix;

	private int					m_get_ix;

	private int					m_qlen;

	/** The literal token representation buffer. */
	private StringBuilder		m_sb				= new StringBuilder();

	//	/** The value representation buffer (for interpreted strings) */
	//	private StringBuilder	m_value_sb = new StringBuilder();

	public ReaderScannerBase(Object source, Reader r) {
		m_r = r;
		m_src = source;
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Current token data.									*/
	/*--------------------------------------------------------------*/
	public String getText() {
		return m_sb.toString();
	}

	public int getTokenLine() {
		return m_token_lnr;
	}

	public int getTokenColumn() {
		return m_token_cnr;
	}

	public Object getSource() {
		return m_src;
	}

	public SourceLocation getSourceLocation() {
		return new SourceLocation(this);
	}

	public void error(String msg) throws SourceErrorException {
		throw new SourceErrorException(getSourceLocation(), msg);
	}


	/*--------------------------------------------------------------*/
	/*	CODING:	Character read primitives.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Gets the next char from the reader.
	 * @return the next char, or -1 for eof.
	 * @throws IOException
	 */
	private int getc() throws IOException {
		if(m_eof)
			return -1;

		int c = m_r.read();
		if(c == -1)
			m_eof = true;
		//		System.out.println("  getc()="+(int)c);
		return c;
	}

	/**
	 * Adds the character to the lookahead queue at the current PUT position.
	 * @param ch
	 * @return
	 */
	private void putq(int ch) {
		if(m_qlen >= MAX_QUEUE_LENGTH)
			throw new IllegalStateException("Lookahead queue overflow");
		if(m_put_ix >= MAX_QUEUE_LENGTH)
			m_put_ix = 0;
		m_la_ar[m_put_ix++] = ch;
		m_qlen++;
	}

	/**
	 * Returns the "current" character in the queue. 
	 * @return
	 */
	final public int LA() throws IOException {
		if(m_qlen == 0)
			putq(getc());
		int c = m_la_ar[m_get_ix];
		//		System.out.println("  LA()="+c+" ("+(char)c+")");
		return c;
	}

	/**
	 * Returns the ixth character for lookahead. ix cannot exceed
	 * the max queue length.
	 * @param ix
	 * @return
	 * @throws IOException
	 */
	final public int LA(int ix) throws IOException {
		while(ix >= m_qlen)
			putq(getc());
		ix = m_get_ix + ix;
		if(ix >= MAX_QUEUE_LENGTH)
			ix -= MAX_QUEUE_LENGTH;
		return m_la_ar[ix];
	}

	/**
	 * Called to advance the character. Consumes the current character, causing the 
	 * next one to become the current one. Accept increments line numbers and column
	 * numbers.
	 * @throws IOException
	 */
	public void accept() {
		if(m_qlen == 0)
			throw new IllegalStateException("accept on empty lookahead queue");
		int ch = m_la_ar[m_get_ix]; // Get current char
		if(ch == '\n') {
			m_lnr++;
			m_cnr = 0;
		} else if(ch != -1) {
			m_cnr++;
		}

		//-- Remove from queue.
		m_qlen--;
		m_get_ix++;
		if(m_get_ix >= MAX_QUEUE_LENGTH)
			m_get_ix = 0;
	}

	/**
	 * Accept ct characters.
	 * @param ct
	 */
	public void accept(int ct) {
		while(ct-- > 0)
			accept();
	}

	public void copy() throws IOException {
		int ch = LA();
		m_sb.append((char) ch);
		accept();
	}

	public void copy(int count) throws IOException {
		while(count-- > 0) {
			int ch = LA();
			m_sb.append((char) ch);
			accept();
		}
	}

	/*--------------------------------------------------------------*/
	/*	CODING:	Lexical Construct handlers.							*/
	/*--------------------------------------------------------------*/
	/**
	 * Skips whitespace until current character is either EOF or non-ws.
	 * @throws IOException
	 */
	protected void skipWs() throws IOException {
		for(;;) {
			int c = LA();
			if(c == -1 || !Character.isWhitespace((char) c))
				return;
			accept();
		}
	}

	/**
	 * Skips whitespace until current character is either EOF or non-ws.
	 * @throws IOException
	 */
	protected void skipWsNoNL() throws IOException {
		for(;;) {
			int c = LA();
			if(c == -1 || c == '\n' || !Character.isWhitespace((char) c))
				return;
			accept();
		}
	}

	/**
	 * Scans a very simple string: something starting with something, terminating
	 * with the same something and not allowing anything ugly in between.
	 * @throws IOException
	 */
	protected void scanSimpleString(boolean keepquotes) throws IOException {
		int qc = LA(); // Get quote start
		accept();
		if(!keepquotes)
			m_sb.setLength(0);
		else
			m_sb.append((char) qc);
		for(;;) {
			int c = LA();
			if(c == qc)
				break;
			else if(c == -1)
				throw new IllegalStateException("Unexpected EOF in string constant started at line " + m_token_lnr + ":" + m_token_cnr);
			else if(c == '\n')
				throw new IllegalStateException("Unexpected newline in string constant started at line " + m_token_lnr + ":" + m_token_cnr);
			m_sb.append((char) c);
			accept();
		}
		if(keepquotes)
			append(qc);
		accept();
	}

	protected int scanNumber() throws IOException {
		int c = LA(); // Get current numeral
		append(c); // Add 1st digit
		accept();
		int c2 = LA();
		int base = 10;
		if(c == '0') {
			if(c2 == 'x' || c2 == 'X') // Hex #?
			{
				base = 16;
				append(c2);
				accept();
			} else
				base = 8;
		}

		int ndots = 0;
		for(;;) {
			c = LA();
			if(c == -1)
				break;
			if(c == '.') {
				if(LA(1) == '.') {
					return T_NUMBER;
				} else {
					ndots++;
					accept();
					append(c);
				}
			} else {
				if(c >= '0' && c <= '9') {
					copy();
				} else {
					if(base <= 10)
						break;
				}

				if(base > 10) {
					if((c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))
						copy();
					else
						break;
				}
			}
		}
		if(ndots <= 1)
			return T_NUMBER;
		else if(ndots == 3)
			return T_IPADDR;
		else
			throw new IllegalStateException("Odd number or IP address started at line " + m_token_lnr + ":" + m_token_cnr);
	}

	protected int scanIdentifier() throws IOException {
		for(;;) {
			int c = LA();
			if(!isIdChar((char) c))
				return T_IDENT;
			accept();
			append(c);
		}
	}

	protected boolean isIdStart(char c) {
		return Character.isLetter(c) || c == '_' || c == '$';
	}

	protected boolean isIdChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '$';
	}

	public void append(int c) {
		m_sb.append((char) c);
	}

	/**
	 * Called when a new token retrieve is started. This saves the current
	 * position within the file as the start location for the token and
	 * resets the token collection buffer.
	 */
	public void startToken() {
		m_token_cnr = m_cnr;
		m_token_lnr = m_lnr;
		m_sb.setLength(0);
	}
}
