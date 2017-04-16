package to.etc.util;

public class Pair<A, B> {
	final private A	m_1;

	final private B	m_2;

	public Pair(A one, B two) {
		m_1 = one;
		m_2 = two;
	}

	public A get1() {
		return m_1;
	}

	public B get2() {
		return m_2;
	}
}
