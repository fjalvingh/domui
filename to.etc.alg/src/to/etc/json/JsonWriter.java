package to.etc.json;

import java.io.*;
import java.lang.reflect.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonWriter extends Writer {
	@Nonnull
	final private Writer m_writer;

	@Nonnull
	final private JsonTypeRegistry m_registry;

	@Nullable
	private IndentWriter m_iw;

	public JsonWriter(@Nonnull Writer writer, @Nonnull JsonTypeRegistry registry) {
		m_writer = writer;
		m_registry = registry;

		if(writer instanceof IndentWriter) {
			m_iw = (IndentWriter) writer;
		}
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

	public void writeLong(long value) throws Exception {
		m_writer.append(Long.toString(value));
	}

	public void writeString(@Nullable String string) throws Exception {
		if(null == string)
			write("null");
		else {
			StringTool.strToJavascriptString(m_writer, string, false);
		}
	}

	public void render(@Nullable Object instance) throws Exception {
		render(instance, null);
	}

	public void render(@Nullable Object instance, @Nullable Type fullType) throws Exception {
		if(null == instance) {
			write("null");
			return;
		}

		ITypeMapping mapping = m_registry.createMapping(instance.getClass(), fullType);
		if(null == mapping)
			throw new IllegalStateException("Could not find a json mapping for " + instance.getClass());
		mapping.render(this, instance);
	}

	public void inc() {
		IndentWriter iw = m_iw;
		if(null == iw)
			return;
		iw.inc();
	}

	public void dec() throws IOException {
		IndentWriter iw = m_iw;
		if(null == iw)
			return;
		iw.dec();
//		iw.println();
	}

	public void nl() throws IOException {
		if(m_iw != null)
			write("\n");
	}
}
