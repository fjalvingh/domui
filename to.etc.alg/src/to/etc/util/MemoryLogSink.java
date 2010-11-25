package to.etc.util;

import java.io.*;

/**
 * A logsink which logs into an Appendable.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 25, 2010
 */
public class MemoryLogSink implements ILogSink {
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
