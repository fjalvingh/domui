package to.etc.domui.component.misc;

import java.util.*;

/**
 * @author <a href="mailto:jal@etc.to">Frits Jalvingh</a>
 * Created on 2/5/16.
 */
final public class MiniLogger {
	final private List<String> m_lines = new ArrayList<>();

	private final int m_max;

	public MiniLogger(int max) {
		m_max = max;
	}

	public void add(String what) {
		m_lines.add(what);
		while(m_lines.size() > m_max)
			m_lines.remove(0);
	}

	public String getData() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for(String l : m_lines) {
			sb.append(count++).append(": ").append(l).append("\n");
		}
		return sb.toString();
	}
}
