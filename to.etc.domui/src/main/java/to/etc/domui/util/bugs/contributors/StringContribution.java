package to.etc.domui.util.bugs.contributors;

import to.etc.domui.util.bugs.IBugContribution;

/**
 * A Bug contribution that is just a text string.
 *
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 21-8-18.
 */
final public class StringContribution implements IBugContribution {
	private final StringBuilder m_buffer = new StringBuilder();

	@Override public void appendTo(Appendable a) throws Exception {
		a.append(m_buffer);
	}

	public StringBuilder sb() {
		return m_buffer;
	}

	public StringBuilder append(String s) {
		m_buffer.append(s);
		return m_buffer;
	}
}
