package to.etc.syntaxer.tests;

import to.etc.syntaxer.HighlightTokenType;
import to.etc.syntaxer.IHighlightRenderer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-05-22.
 */
final class CopyRenderer implements IHighlightRenderer {
	private final StringBuilder m_sb;

	public CopyRenderer(StringBuilder sb) {
		m_sb = sb;
	}

	@Override
	public void renderToken(HighlightTokenType tokenType, String token, int characterIndex) {
		if(tokenType == HighlightTokenType.newline)
			m_sb.append("\n");
		else
			m_sb.append(token);
	}
}
