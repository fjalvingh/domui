package to.etc.json;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

/**
 * Shared accessor to handle JSON marshalling/unmarshalling.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Oct 18, 2013
 */
final public class JSON {
	@NonNull
	static private JsonTypeRegistry m_registry = new JsonTypeRegistry();

	private JSON() {}

	@NonNull
	public static JsonTypeRegistry getRegistry() {
		return m_registry;
	}

	static public <T> T decode(@NonNull Class<T> typeClass, @NonNull Reader input) throws Exception {
		JsonReader reader = new JsonReader("input", input, m_registry);
		return reader.parse(typeClass, null);
	}

	static public <T> T decode(@NonNull Class<T> typeClass, @NonNull Type type, @NonNull Reader input) throws Exception {
		JsonReader reader = new JsonReader("input", input, m_registry);
		return reader.parse(typeClass, type);
	}

	static public <T> void render(@NonNull Writer writer, @Nullable T instance) throws Exception {
		render(writer, instance, null);
	}

	static public <T> void render(@NonNull Writer writer, @Nullable T instance, @Nullable Type fullType) throws Exception {
		JsonWriter w = (writer instanceof JsonWriter ? (JsonWriter) writer : new JsonWriter(writer, m_registry));
		w.render(instance, fullType);
	}

	@NonNull
	static public <T> String render(@Nullable T instance, @Nullable Type fullType) throws Exception {
		StringWriter sw = new StringWriter();
		JsonWriter w = new JsonWriter(sw, m_registry);
		w.render(instance, fullType);
		return sw.getBuffer().toString();
	}

	@NonNull
	static public <T> String render(@Nullable T instance) throws Exception {
		return render(instance, null);
	}
}
