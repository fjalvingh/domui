package to.etc.domui.util;

import javax.annotation.concurrent.*;

@Immutable
final public class IntPoint {
	private int m_x, m_y;

	public IntPoint(int x, int y) {
		m_x = x;
		m_y = y;
	}

	public int x() {
		return m_x;
	}

	public int y() {
		return m_y;
	}
}
