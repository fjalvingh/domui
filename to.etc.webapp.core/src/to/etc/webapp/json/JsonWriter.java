package to.etc.webapp.json;

import java.io.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonWriter extends Writer {
	@Nonnull
	final private Writer m_writer;

	@Nonnull
	final private JsonTypeRegistry m_registry;

	public JsonWriter(@Nonnull Writer writer, @Nonnull JsonTypeRegistry registry) {
		m_writer = writer;
		m_registry = registry;
	}

	@Override
	public void close() throws IOException {
		m_writer.close();
	}

	@Override
	public void flush() throws IOException {
		m_writer.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		m_writer.write(cbuf, off, len);
	}

	public void writeNumber(@Nonnull Number value) throws Exception {
		m_writer.append(value.toString());
	}

	public void writeString(@Nullable String string) throws Exception {
		if(null == string)
			write("null");
		else {
			StringTool.strToJavascriptString(m_writer, string, true);
		}
	}

	public void render(@Nullable Object instance) throws Exception {
		if(null == instance)
			return;

		ITypeMapping mapping = m_registry.createMapping(instance.getClass(), null);
		if(null == mapping)
			throw new IllegalStateException("Could not find a json mapping for " + instance.getClass());
		mapping.render(this, instance);
	}
}
