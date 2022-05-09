package to.etc.syntaxer;

import to.etc.function.IExecute;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public class LineContext {
	private final IExecute m_lexerState;

	public LineContext(IExecute lexerState) {
		m_lexerState = lexerState;
	}

	public IExecute getLexerState() {
		return m_lexerState;
	}
}
