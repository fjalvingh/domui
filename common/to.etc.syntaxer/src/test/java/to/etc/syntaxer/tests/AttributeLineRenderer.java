package to.etc.syntaxer.tests;

import to.etc.syntaxer.AttributedLine;
import to.etc.syntaxer.HighlightTokenType;
import to.etc.syntaxer.IHighlightRenderer;

public class AttributeLineRenderer implements IHighlightRenderer {
	private final AttributedLine m_line;

	public AttributeLineRenderer(AttributedLine line) {
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
