package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 05-05-22.
 */
final public class NullHighlighter implements IHighlighter {
	private final IHighlightRenderer m_renderer;

	public NullHighlighter(@NonNull IHighlightRenderer renderer) {
		m_renderer = renderer;
	}

	@NonNull
	@Override
	public LineContext highlightLine(@Nullable LineContext previous, @NonNull String line) {
		m_renderer.renderToken(HighlightTokenType.text, line, 0);
		m_renderer.renderToken(HighlightTokenType.newline, "", line.length());

		return new LineContext(this::dummy);
	}

	private void dummy() {}
}
