package to.etc.syntaxer;

/**
 * Renders a given token.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public interface IHighlightRenderer {
	void renderToken(HighlightTokenType tokenType, String token, int characterIndex);
}
