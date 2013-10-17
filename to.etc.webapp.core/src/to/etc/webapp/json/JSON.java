package to.etc.webapp.json;

import java.io.*;

import javax.annotation.*;

public class JSON {
	private JsonTypeRegistry m_registry = new JsonTypeRegistry();

	public <T> T decode(@Nonnull Class<T> typeClass, @Nonnull Reader input) throws Exception {
		JsonReader reader = new JsonReader("input", input, m_registry);

		return reader.parse(typeClass);
	}

}
