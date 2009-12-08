package to.etc.domui.fts;

import java.io.*;

/**
 * Tokenizer using reusable buffers and reusable tokens.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 11, 2008
 */
public class BufferedTokenizer {
	protected int				m_fullOffset;
	protected int				m_sentenceNumber;
	protected int				m_tokenNumber;
	private Reader				m_reader;
	protected char[]			m_buffer;
	private boolean				m_eof;
	protected int				m_bend;
	protected int				m_bix;
//	private CharBufPool			m_bufPool = new CharBufPool();

	public BufferedTokenizer(Reader reader, int blocksz) {
		m_reader = reader;
		m_buffer = new char[blocksz];
	}
	public BufferedTokenizer(Reader r) {
		this(r, 8192);
	}
	final public boolean	atEof() {
		return m_eof;
	}

	final protected boolean nextBuffer() throws IOException {
//		System.out.println("\n:NEXTBUFFER:");
		if(m_eof)
			return false;
		if(m_bend > 0)
			m_fullOffset += m_bend;					// Append previous buffer size,
		m_bend = m_reader.read(m_buffer);
		m_bix	= 0;
		if(m_bend <= 0) {
			m_eof = true;
			return false;
		}
		return true;
	}

	protected boolean	isWsChar(char c) {
		if(Character.isWhitespace(c))
			return true;
		int type = Character.getType(c);
		return type == Character.CONNECTOR_PUNCTUATION
		|| type == Character.CONNECTOR_PUNCTUATION
		|| type == Character.CONTROL
		|| type == Character.DASH_PUNCTUATION
		|| type == Character.ENCLOSING_MARK
		|| type == Character.END_PUNCTUATION
		|| type == Character.FINAL_QUOTE_PUNCTUATION
		|| type == Character.FORMAT
		|| type == Character.INITIAL_QUOTE_PUNCTUATION
		|| type == Character.LINE_SEPARATOR
		|| type == Character.NON_SPACING_MARK
		|| type == Character.OTHER_PUNCTUATION
		|| type == Character.OTHER_SYMBOL
		|| type == Character.PARAGRAPH_SEPARATOR
		|| type == Character.SPACE_SEPARATOR
		|| type == Character.START_PUNCTUATION
		;
	}

	/**
	 * Default, slow impl for a next char.
	 * @return -1 at EOF, character otherwise.
	 */
	public int nextChar() throws IOException {
		if(m_bix < m_bend)
			return m_buffer[m_bix++] & 0xffff;
		if(! nextBuffer())
			return -1;
		return m_buffer[m_bix++] & 0xffff;
	}

	protected void		skipWhitespace() throws IOException {
		if(m_eof)
			return;
		for(;;) {
			while(m_bix < m_bend) {
				char c = m_buffer[m_bix];
				if(! isWsChar(c)) {
					//-- Handle punctuation here
					if(c == '.') {
						//-- Next sentence;
						m_sentenceNumber++;
					} else {
						return;
					}
				} else {
					if(c == '.')
						m_sentenceNumber++;
					m_bix++;
				}
			}

			//-- Buffer exhausted- try another one
			if(! nextBuffer())
				return;
		}
	}


}
