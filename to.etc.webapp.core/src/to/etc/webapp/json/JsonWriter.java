package to.etc.webapp.json;

import java.io.*;

import javax.annotation.*;

import to.etc.util.*;

public class JsonWriter extends Writer {
	@Nonnull
	final private Writer m_writer;

	public JsonWriter(@Nonnull Writer writer) {
		m_writer = writer;
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
}
