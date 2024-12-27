package to.etc.syntaxer;

public class AttributedLineRenderer implements IHighlightRenderer {
	private final AttributedLine m_line;

	public AttributedLineRenderer(AttributedLine line) {
		m_line = line;
	}

	@Override
	public void renderToken(HighlightTokenType tokenType, String token, int characterIndex) {
		if(tokenType == HighlightTokenType.newline) {
			//-- Do not care
		} else {
			m_line.append(token, tokenType.ordinal());					// Append with token type as lower bits.
		}
	}
}
