package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
@NonNullByDefault
final public class HighlighterFactory {
	static private final Map<String, Function<IHighlightRenderer, IHighlighter>> m_factoryByExtMap = new ConcurrentHashMap<>();

	static public IHighlighter getHighlighter(String ext, @NonNull IHighlightRenderer renderer) {
		Function<IHighlightRenderer, IHighlighter> fact = m_factoryByExtMap.get(ext.toLowerCase());
		if(null == fact)
			return new NullHighlighter(renderer);

		return fact.apply(renderer);
	}

	static public void register(String ext, Function<IHighlightRenderer, IHighlighter> factory) {
		m_factoryByExtMap.put(ext.toLowerCase(), factory);
	}

	static {
		register("sql", SqlHighlighter::new);
	}
}
