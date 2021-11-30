package to.etc.domui.server;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A StringBuffer like class that collects string data but instead
 * of packing everything in a sing char[] this collects it as a
 * list of char[], so that we do not have to reallocate for large
 * outputs.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 30-11-21.
 */
public class StringBufferDataFactory implements IDataFactory, Appendable {
	private final static int BUFFER_SIZE = 8192;

	private final String m_contentType;

	private List<char[]> m_bufferList = new ArrayList<>();

	private int m_bufferIndex;

	private int m_bufferOffset;

	public StringBufferDataFactory(String contentType) {
		m_contentType = contentType;
		m_bufferList.add(new char[BUFFER_SIZE]);
	}

	public void clear() {
		while(m_bufferList.size() > 1) {
			m_bufferList.remove(m_bufferList.size() - 1);
		}
		m_bufferIndex = 0;
		m_bufferOffset = 0;
	}

	public int length() {
		return (m_bufferIndex * BUFFER_SIZE) + m_bufferOffset;
	}

	@Override
	public void renderOutput(RequestContextImpl ctx) throws Exception {
		Writer outputWriter = ctx.getOutputWriter(m_contentType, "utf-8");
		for(int i = 0; i < m_bufferList.size() - 1; i++) {
			char[] chars = m_bufferList.get(i);
			outputWriter.write(chars);
		}
		outputWriter.write(m_bufferList.get(m_bufferList.size() - 1), 0, m_bufferOffset);
	}

	@Override
	public StringBufferDataFactory append(CharSequence cs) {
		return append(cs, 0, cs.length());
	}

	@Override
	public StringBufferDataFactory append(CharSequence cs, int soff, int send) {
		while(soff < send) {
			int len = send - soff;
			int remaining = BUFFER_SIZE - m_bufferOffset;
			if(remaining == 0) {
				m_bufferList.add(new char[BUFFER_SIZE]);
				m_bufferIndex++;
				m_bufferOffset = 0;
				remaining = BUFFER_SIZE;
			}

			if(remaining > len) {
				remaining = len;
			}
			char[] buf = m_bufferList.get(m_bufferIndex);
			int off = m_bufferOffset;
			int end = off + remaining;
			while(off < end) {
				buf[off++] = cs.charAt(soff++);
			}
			m_bufferOffset = off;
		}
		return this;
	}

	@Override
	public Appendable append(char c) {
		if(m_bufferOffset >= BUFFER_SIZE) {
			m_bufferList.add(new char[BUFFER_SIZE]);
			m_bufferIndex++;
			m_bufferOffset = 0;
		}
		m_bufferList.get(m_bufferIndex)[m_bufferOffset++] = c;
		return this;
	}

	public String asString() {
		StringBuilder sb = new StringBuilder(length());
		for(int i = 0; i < m_bufferList.size() - 1; i++) {
			char[] chars = m_bufferList.get(i);
			sb.append(chars);
		}
		sb.append(m_bufferList.get(m_bufferList.size() - 1), 0, m_bufferOffset);
		return sb.toString();
	}
}
