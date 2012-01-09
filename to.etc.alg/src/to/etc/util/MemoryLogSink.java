package to.etc.util;

import java.io.*;

public class MemoryLogSink implements LogSink {
	private Appendable	m_sb;

	public MemoryLogSink(Appendable a) {
		m_sb = a;
	}

	public void exception(Throwable t, String msg) {
		try {
			m_sb.append("Exception: ");
			m_sb.append(msg);
			m_sb.append("\n");
			m_sb.append(": ");
			StringTool.strStacktrace(m_sb, t);
		} catch(IOException x) {
			//-- Never happens really but James Gosling is an Idiot.
			throw new WrappedException(x);
		}
	}

	public void log(String msg) {
		try {
			m_sb.append(msg);
			m_sb.append("\n");
		} catch(IOException x) {
			//-- Never happens really but James Gosling is an Idiot.
			throw new WrappedException(x);
		}
	}
}
