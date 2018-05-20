package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import to.etc.lexer.ReaderTokenizerBase;

import java.io.Reader;
import java.lang.reflect.Type;

public class JsonReader extends ReaderTokenizerBase {
	@NonNull
	final private JsonTypeRegistry m_registry;

	public JsonReader(Object source, Reader r, @NonNull JsonTypeRegistry registry) {
		super(source, r);
		m_registry = registry;
	}

	@Nullable
	public <T> T parse(@NonNull Class<T> typeClass, @Nullable Type type) throws Exception {
		ITypeMapping mapping = m_registry.createMapping(typeClass, type);
		if(null == mapping)
			throw new IllegalStateException("Could not find a json mapping for " + typeClass);
		nextToken();
		return (T) mapping.parse(this);
	}

}
