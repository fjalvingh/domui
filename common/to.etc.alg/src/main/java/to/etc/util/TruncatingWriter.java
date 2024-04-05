package to.etc.util;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 07-09-2023.
 */
final public class TruncatingWriter extends Writer {
	private final int m_maxPrefix;

	private final int m_maxSuffix;

	private final char[] m_one = new char[1];

	private boolean m_inSuffix;

	private List<String> m_prefix = new ArrayList<>();

	private List<String> m_suffix = new ArrayList<>();

	private List<String> m_current = new ArrayList<>();

	private int m_currentMax;

	private boolean m_overflowed;

	private StringBuilder m_sb = new StringBuilder();

	public TruncatingWriter(int maxPrefix, int maxSuffix) {
		m_maxPrefix = maxPrefix;
		m_maxSuffix = maxSuffix;
		m_current = m_prefix;
		m_currentMax = m_maxPrefix;
	}

	public TruncatingWriter() {
		this(30, 50);
	}

	@Override
	public void write(int c) throws IOException {
		m_one[0] = (char) c;
		write(m_one, 0, 1);
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		//-- Write line by line
		while(off < len) {
			char c = cbuf[off++];
			m_sb.append(c);
			if(c == '\n' || m_sb.length() > 1024) {
				//-- Flush a line
				m_current.add(m_sb.toString());
				handleOverflow();
			}
		}
	}

	private void handleOverflow() {
		m_sb.setLength(0);
		while(m_current.size() > m_currentMax) {
			m_current.remove(0);
			if(m_current == m_prefix) {
				m_currentMax = m_maxSuffix;
				m_current = m_suffix;
			} else {
				m_overflowed = true;
			}
		}
	}

	@Override
	public void flush() throws IOException {

	}

	@Override
	public void close() throws IOException {
		if(m_sb.length() > 0) {
			m_current.add(m_sb.toString());
			handleOverflow();
		}
	}

	public boolean isOverflowed() {
		return m_overflowed;
	}

	public List<String> getPrefix() {
		return m_prefix;
	}

	public List<String> getSuffix() {
		return m_suffix;
	}

	public void append(StringBuilder sb) {
		for(String prefix : m_prefix) {
			sb.append(prefix);
		}
		if(m_overflowed) {
			sb.append("\n<...truncated...>\n");
		}
		for(String suffix : m_suffix) {
			sb.append(suffix);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(sb);
		return sb.toString();
	}
}
