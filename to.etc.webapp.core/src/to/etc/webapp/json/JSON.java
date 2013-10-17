package to.etc.webapp.json;

import java.io.*;

import javax.annotation.*;

public class JSON {
	private JsonTypeRegistry m_registry = new JsonTypeRegistry();

	public <T> T decode(@Nonnull Class<T> typeClass, @Nonnull Reader input) throws Exception {
		JsonReader reader = new JsonReader("input", input, m_registry);

		return reader.parse(typeClass);
	}

	public <T> void render(@Nonnull Writer writer, @Nullable T instance) throws Exception {
		JsonWriter w = (writer instanceof JsonWriter ? (JsonWriter) writer : new JsonWriter(writer, m_registry));
		w.render(instance);
	}

}
