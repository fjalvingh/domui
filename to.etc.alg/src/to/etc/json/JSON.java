package to.etc.json;

import java.io.*;
import java.lang.reflect.*;

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
		return reader.parse(typeClass, null);
	}

	static public <T> T decode(@Nonnull Class<T> typeClass, @Nonnull Type type, @Nonnull Reader input) throws Exception {
		JsonReader reader = new JsonReader("input", input, m_registry);
		return reader.parse(typeClass, type);
	}

	static public <T> void render(@Nonnull Writer writer, @Nullable T instance) throws Exception {
		render(writer, instance, null);
	}

	static public <T> void render(@Nonnull Writer writer, @Nullable T instance, @Nullable Type fullType) throws Exception {
		JsonWriter w = (writer instanceof JsonWriter ? (JsonWriter) writer : new JsonWriter(writer, m_registry));
		w.render(instance, fullType);
	}

	@Nonnull
	static public <T> String render(@Nullable T instance, @Nullable Type fullType) throws Exception {
		StringWriter sw = new StringWriter();
		JsonWriter w = new JsonWriter(sw, m_registry);
		w.render(instance, fullType);
		return sw.getBuffer().toString();
	}

	@Nonnull
	static public <T> String render(@Nullable T instance) throws Exception {
		return render(instance, null);
	}
}
