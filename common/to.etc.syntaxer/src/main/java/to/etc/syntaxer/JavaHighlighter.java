package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public class JavaHighlighter extends HiParser implements IHighlighter {
	private final IHighlightRenderer m_renderer;

	public JavaHighlighter(@NonNull IHighlightRenderer renderer) {
		m_renderer = renderer;
	}

	@Override
	protected void tokenFound(HighlightTokenType type, String text, int characterIndex) {

	}

	@NonNull
	@Override
	public LineContext highlightLine(@Nullable LineContext previous, @NonNull String line) {
		m_renderer.renderToken(HighlightTokenType.text, line, 0);
		m_renderer.renderToken(HighlightTokenType.newline, "", line.length());
		return new LineContext(HighlightTokenType.whitespace);
	}
}
