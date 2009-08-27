package to.etc.util;

/**
 *
 * Created on May 6, 2003
 * @author jal
 */
public class StringOutputBuffer implements iOutput {
	private StringBuffer	m_sb;

	private int				m_il;

	public StringOutputBuffer() {
		m_il = 20;
	}

	public StringOutputBuffer(StringBuffer sb) {
		m_sb = sb;
	}

	public StringOutputBuffer(int il) {
		m_il = il;
	}

	public void output(String s) {
		if(s == null)
			return;
		if(s.length() == 0)
			return;
		if(m_sb == null)
			m_sb = new StringBuffer(m_il);
		m_sb.append(s);
	}

	public String getValue() {
		return m_sb == null ? "" : m_sb.toString();
	}
}
