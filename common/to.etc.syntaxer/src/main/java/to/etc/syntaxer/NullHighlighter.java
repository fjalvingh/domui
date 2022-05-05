package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 05-05-22.
 */
final public class NullHighlighter implements IHighlighter {
	@NonNull
	@Override
	public LineContext highlightLine(@Nullable LineContext previous, @NonNull String line, @NonNull IHighlightRenderer renderer) {
		renderer.renderToken(HighlightTokenType.text, line, 0);
		renderer.renderToken(HighlightTokenType.newline, "", line.length());

		return new LineContext(this::dummy);
	}

	private void dummy() {}
}
