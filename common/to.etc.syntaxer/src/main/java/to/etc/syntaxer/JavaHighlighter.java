package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public class JavaHighlighter extends HiParser implements IHighlighter {
	public JavaHighlighter() {
		//-- Empty
	}

	@Override
	protected void tokenFound(HighlightTokenType type, String text, int characterIndex) {

	}

	@NonNull
	@Override
	public LineContext highlightLine(IHighlightRenderer renderer, @Nullable LineContext previous, @NonNull String line) {
		renderer.renderToken(HighlightTokenType.text, line, 0);
		renderer.renderToken(HighlightTokenType.newline, "", line.length());
		return new LineContext(HighlightTokenType.whitespace);
	}
}
