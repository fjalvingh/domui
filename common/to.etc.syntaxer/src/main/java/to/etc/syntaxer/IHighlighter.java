package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
public interface IHighlighter {
	@NonNull
	LineContext highlightLine(@Nullable LineContext previous, @NonNull String line);
}
