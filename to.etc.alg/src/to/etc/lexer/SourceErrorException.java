package to.etc.lexer;

/**
 * Some kind of error found while parsing a source. Contains an error with associated
 * source (file) location.
 *
 * Created on Sep 13, 2004
 * @author jal
 */
public class SourceErrorException extends Exception {
	private SourceLocation	m_sl;

	public SourceErrorException(SourceLocation sl, String msg) {
		super(msg);
		m_sl = sl;
	}

	@Override
	public String toString() {
		return m_sl + ": " + getMessage();
	}

	public SourceLocation getLocation() {
		return m_sl;
	}

}
