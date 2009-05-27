package to.etc.webapp.ajax.renderer;

import java.io.*;

abstract public class StructuredWriter extends Writer {
	private Writer m_w;

	private int m_size;

	public StructuredWriter(Writer w) {
		m_w = w;
	}

	@Override
	final public void write(char[] cbuf, int off, int len) throws IOException {
		m_w.write(cbuf, off, len);
		m_size += len;
	}

	final public int size() {
		return m_size;
	}

	abstract public void end() throws Exception;

	abstract public void list(String name) throws Exception;

	abstract public void record(String name) throws Exception;

	abstract public void field(String name, String value) throws Exception;

	abstract public void field(String name, boolean value) throws Exception;

	abstract public void field(String name, java.util.Date value) throws Exception;

	abstract public void field(String name, Number value) throws Exception;
}
