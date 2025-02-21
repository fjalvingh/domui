package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 05-05-22.
 */
final public class NullHighlighter implements IHighlighter {
	public NullHighlighter() {
		//-- Empty
	}

	@NonNull
	@Override
	public LineContext highlightLine(IHighlightRenderer renderer, @Nullable LineContext previous, @NonNull String line) {
		renderer.renderToken(HighlightTokenType.text, line, 0);
		renderer.renderToken(HighlightTokenType.newline, "", line.length());
		return new LineContext(HighlightTokenType.whitespace);
	}
}
