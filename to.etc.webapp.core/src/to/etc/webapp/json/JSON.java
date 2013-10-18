package to.etc.webapp.json;

import java.io.*;

import javax.annotation.*;

/**
 * Shared accessor to handle JSON marshalling/unmarshalling.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
final public class JSON {
	@Nonnull
	static private JsonTypeRegistry m_registry = new JsonTypeRegistry();

	private JSON() {}

	@Nonnull
	public static JsonTypeRegistry getRegistry() {
		return m_registry;
	}

	static public <T> T decode(@Nonnull Class<T> typeClass, @Nonnull Reader input) throws Exception {
		JsonReader reader = new JsonReader("input", input, m_registry);
		return reader.parse(typeClass);
	}

	static public <T> void render(@Nonnull Writer writer, @Nullable T instance) throws Exception {
		JsonWriter w = (writer instanceof JsonWriter ? (JsonWriter) writer : new JsonWriter(writer, m_registry));
		w.render(instance);
	}
}
