package to.etc.lexer;

/**
 * This defines a source location for some parsed construct. It can
 * be obtained for a token from the lexer if needed.
 *
 * Created on Sep 13, 2004
 * @author jal
 */
public class SourceLocation {
	private Object	m_src;

	private int		m_line;

	private int		m_col;

	public SourceLocation(Object src, int line, int col) {
		m_src = src;
		m_line = line;
		m_col = col;
	}

	public SourceLocation(ReaderScannerBase rt) {
		m_src = rt.getSource();
		m_line = rt.getTokenLine();
		m_col = rt.getTokenColumn();
	}

	/**
	 * @return Returns the col.
	 */
	public int getCol() {
		return m_col;
	}

	/**
	 * @return Returns the line.
	 */
	public int getLine() {
		return m_line;
	}

	/**
	 * @return Returns the src.
	 */
	public Object getSrc() {
		return m_src;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(m_src != null) {
			sb.append(m_src.toString());
			sb.append('(');
		} else
			sb.append("Line ");
		sb.append(m_line);
		if(m_col >= 0) {
			sb.append(':');
			sb.append(m_col + 1);
		}
		if(m_src != null)
			sb.append(')');
		return sb.toString();
	}
}
