package to.etc.syntaxer.tests;

import to.etc.syntaxer.HighlightTokenType;
import to.etc.syntaxer.IHighlightRenderer;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 10-05-22.
 */
final class CopyRenderer implements IHighlightRenderer {
	private final StringBuilder m_sb = new StringBuilder();

	private final StringBuilder m_detailed = new StringBuilder();

	public CopyRenderer() {
	}

	@Override
	public void renderToken(HighlightTokenType tokenType, String token, int characterIndex) {
		if(tokenType == HighlightTokenType.newline) {
			m_sb.append("\n");
			m_detailed.append("\n");
		} else {
			m_sb.append(token);
			m_detailed.append(tokenType.name()).append(":").append(token).append("\n");
		}
	}

	public String getLiteral() {
		return m_sb.toString();
	}

	public String getDetailed() {
		return m_detailed.toString();
	}
}
