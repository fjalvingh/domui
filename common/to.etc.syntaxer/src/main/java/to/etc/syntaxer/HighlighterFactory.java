package to.etc.syntaxer;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 02-05-22.
 */
@NonNullByDefault
final public class HighlighterFactory {
	static private final Map<String, Supplier<IHighlighter>> m_factoryByExtMap = new ConcurrentHashMap<>();

	static public IHighlighter getHighlighter(String ext) {
		Supplier<IHighlighter> fact = m_factoryByExtMap.get(ext.toLowerCase());
		if(null == fact)
			return new NullHighlighter();

		return fact.get();
	}

	static public void register(String ext, Supplier<IHighlighter> factory) {
		m_factoryByExtMap.put(ext.toLowerCase(), factory);
	}

	static {
		register("sql", SqlHighlighter::new);
	}
}
