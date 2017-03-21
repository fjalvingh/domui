package to.etc.json;

import java.io.*;
import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.lexer.*;

public class JsonReader extends ReaderTokenizerBase {
	@Nonnull
	final private JsonTypeRegistry m_registry;

	public JsonReader(Object source, Reader r, @Nonnull JsonTypeRegistry registry) {
		super(source, r);
		m_registry = registry;
	}

	@Nullable
	public <T> T parse(@Nonnull Class<T> typeClass, @Nullable Type type) throws Exception {
		ITypeMapping mapping = m_registry.createMapping(typeClass, type);
		if(null == mapping)
			throw new IllegalStateException("Could not find a json mapping for " + typeClass);
		nextToken();
		return (T) mapping.parse(this);
	}

}
