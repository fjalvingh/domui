package to.etc.lexer;

/**
 * Base type for lexer tokens. For performance reasons this is a MUTABLE
 * object; users of the tokenizers are supposed to provide instances to be
 * filled.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 28, 2009
 */
public class LexerToken {
	static public final int	T_EOF		= -1;

	static public final int	T_STRING	= -2;

	static public final int	T_NUMBER	= -3;

	static public final int	T_IPADDR	= -4;

	static public final int	T_IDENT		= -5;

	static public final int	T_COMMENT	= -6;

	static public final int	T_BASE_LAST	= -7;

	private Object	m_src;

	private int		m_line;

	private int		m_column;

	private String	m_text;

	/**
	 * When +ve this is a literal character code; when -ve this is a TOKEN code. -1 is EOF by definition.
	 */
	private int		m_tokenCode;

	public Object getSrc() {
		return m_src;
	}

	public void setSrc(Object src) {
		m_src = src;
	}

	public int getLine() {
		return m_line;
	}

	public void setLine(int line) {
		m_line = line;
	}

	public int getColumn() {
		return m_column;
	}

	public void setColumn(int column) {
		m_column = column;
	}

	public String getText() {
		return m_text;
	}

	public void setText(String text) {
		m_text = text;
	}

	public int getTokenCode() {
		return m_tokenCode;
	}

	public void setTokenCode(int tokenCode) {
		m_tokenCode = tokenCode;
	}

	public void assignFrom(LexerToken t) {
		m_column = t.m_column;
		m_line = t.m_line;
		m_src = t.m_src;
		m_text = t.m_text;
		m_tokenCode = t.m_tokenCode;
	}

	public LexerToken dup() {
		try {
			LexerToken t = getClass().newInstance();
			t.assignFrom(this);
			return t;
		} catch(Exception x) {
			throw new RuntimeException(x);
		}
	}
}
