package to.etc.webapp.testsupport;

import java.io.*;

import javax.annotation.*;

import to.etc.util.*;

/**
 * This is a log sink for test classes. It will log it's output into a local stringbuilder that
 * is accessible to tests after logging. If so asked it will also log the output to the test log
 * file, preceded by an optional test case header.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on Nov 30, 2011
 */
public class TestLogSink implements ILogSink {
	final private StringBuilder m_sb = new StringBuilder(512);

	final private Appendable m_link;

	public TestLogSink(Appendable link) {
		m_link = link;
	}

	public TestLogSink() {
		m_link = null;
	}

	public StringBuilder getBuffer() {
		return m_sb;
	}

	public String getOutput() {
		return m_sb.toString();
	}

	@Override
	public void log(@Nonnull String msg) {
		m_sb.append(msg).append("\n");
		if(m_link != null) {
			try {
				m_link.append(msg);
				m_link.append("\n");
			} catch(IOException e) {
				throw WrappedException.wrap(e);
			}
		}
	}

	@Override
	public void exception(@Nonnull Throwable t, @Nonnull String msg) {
		m_sb.append(msg).append("\n");
		StringTool.strStacktrace(m_sb, t);
		if(m_link != null) {
			try {
				m_link.append(msg);
				m_link.append("\n");
				StringTool.strStacktrace(m_link, t);
			} catch(IOException e) {
				throw WrappedException.wrap(e);
			}
		}
	}
}
