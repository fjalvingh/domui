package to.etc.syntaxer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public class LineContext {
	private final HighlightTokenType m_lexerState;

	public LineContext(HighlightTokenType lexerState) {
		m_lexerState = lexerState;
	}

	public HighlightTokenType getLexerState() {
		return m_lexerState;
	}
}
